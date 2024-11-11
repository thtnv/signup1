package com.example.signup;

import androidx.appcompat.app.AppCompatActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.android.gms.common.AccountPicker;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.facebook.FacebookSdk;
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_ACCOUNT = 1001;
    private EditText emailEditText;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.setClientToken("YOUR_CLIENT_TOKEN");
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this.getApplication());
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.emailEditText);
        callbackManager = CallbackManager.Factory.create();

        // Bấm vào Google Icon
        findViewById(R.id.googleIcon).setOnClickListener(v -> chooseAccount());

        // Bấm vào Facebook Icon
        findViewById(R.id.facebookIcon).setOnClickListener(v -> signInWithFacebook());

        findViewById(R.id.continueButton).setOnClickListener(v -> openNextActivity());

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Dữ liệu mẫu
        List<WordItem> wordList = new ArrayList<>();
        wordList.add(new WordItem("Elated", "Phấn khích, hân hoan, rất vui mừng."));
        wordList.add(new WordItem("Ecstatic", "Cực kỳ vui sướng, ngây ngất."));

        WordAdapter adapter = new WordAdapter(this, wordList);
        recyclerView.setAdapter(adapter);
    }

    private void chooseAccount() {
        Intent intent = AccountPicker.newChooseAccountIntent(
                null, null, new String[]{"com.google"}, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }


    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "Facebook login canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this, "Facebook login failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            String email = user.getEmail();
                            emailEditText.setText(email);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getFacebookEmail(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    if (object.has("email")) {
                        String email = object.getString("email");
                        emailEditText.setText(email);
                    } else {
                        Toast.makeText(MainActivity.this, "This Facebook account does not have an associated email", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        request.setParameters(parameters);
        request.executeAsync();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Xử lý phản hồi của AccountPicker cho Google
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT && resultCode == RESULT_OK && data != null) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            emailEditText.setText(accountName);  // Điền email vào EditText
        }

        // Xử lý phản hồi của Facebook
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void openNextActivity() {
        Intent intent = new Intent(MainActivity.this, NextActivity.class);
        startActivity(intent);
    }
}