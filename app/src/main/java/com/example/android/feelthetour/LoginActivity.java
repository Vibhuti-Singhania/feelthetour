package com.example.android.feelthetour;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import static android.R.attr.name;


public class LoginActivity extends AppCompatActivity {

    private Button mLoginButton;
    private TextView mForgotPasswordTextView, mRegisterTextView;
    private EditText mUsernameOrEmailEditText, mPasswordEditText;
//    private Button mFacebookbtn;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private static final String TAG = "FacebookLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginButton = (Button) findViewById(R.id.login_button);
        mForgotPasswordTextView = (TextView) findViewById(R.id.forgot_password_text);
        mRegisterTextView = (TextView) findViewById(R.id.register_text);
        mUsernameOrEmailEditText = (EditText) findViewById(R.id.username_email_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);

        mRegisterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginButton.setEnabled(false);
                userLogin();
            }
        });

//
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();

        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });

    }
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
            super.onActivityResult(requestCode, resultCode, data);

            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if(currentUser!=null){
//            updateUI();
//        }
//        mAuth.addAuthStateListener(firebaseAuthListener);
    }

//    protected void onStop() {
//            super.onStop();
//            mAuth.removeAuthStateListener(firebaseAuthListener);
//    }
    private void updateUI(){
        Toast.makeText(LoginActivity.this,"You are logged in",Toast.LENGTH_LONG).show();
        Intent newIntent = new Intent(LoginActivity.this, WelcomeActivity.class);
        startActivity(newIntent);
        finish();
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
//                            mFacebookbtn.setEnabled(true);
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            mFacebookbtn.setEnabled(true);
//                            updateUI();
                        }
                    }
                });
    }

private void userLogin() {

    final String usernameOrEmail = mUsernameOrEmailEditText.getText().toString(); //.trim()
    final String password = mPasswordEditText.getText().toString();
    //Toast.makeText(getApplicationContext(), "Username or email = " + usernameOrEmail + "\nPassword = " + password, Toast.LENGTH_SHORT).show();
//    if (!TextUtils.isEmpty(usernameOrEmail) && !TextUtils.isEmpty(password)) {
////        UserLogin ul = new UserLogin();
////        ul.execute();
     if (TextUtils.isEmpty(usernameOrEmail)) {
        mUsernameOrEmailEditText.setText("");
        mUsernameOrEmailEditText.setHint("Username of email must not be empty");
        mUsernameOrEmailEditText.setHintTextColor(Color.RED);
    }
    if(TextUtils.isEmpty(password)) {
        mPasswordEditText.setHint("Password must not be empty");
        mPasswordEditText.setHintTextColor(Color.RED);
    }


    class UserLogin extends AsyncTask<Void, Void, String> {

        ProgressBar progressBar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressBar = (ProgressBar) findViewById(R.id.progressBar);
//            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            progressBar.setVisibility(View.GONE);


            try {
                //converting response to json object
                JSONObject obj = new JSONObject(s);

                //if no error in response
                if (!obj.getBoolean("error")) {
                    Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();

                    //getting the user from the response
                    JSONObject userJson = obj.getJSONObject("user");

                    //creating a new user object
                    Users user = new Users(
                            userJson.getInt("id"),
                            userJson.getString("username"),
                            userJson.getString("useremail")
                    );

                    //storing the user in shared preferences
                    SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);

                    //starting the profile activity
                    finish();
                    startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            //creating request handler object
            RequestHandler requestHandler = new RequestHandler();

            //creating request parameters
            HashMap<String, String> params = new HashMap<>();
            params.put("username", usernameOrEmail);
            params.put("password", password);

            //returing the response
            return requestHandler.sendPostRequest(URLs.URL_LOGIN, params);
        }
    }

    UserLogin ul = new UserLogin();
    ul.execute();

  }
}





