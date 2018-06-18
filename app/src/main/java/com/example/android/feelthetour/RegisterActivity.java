package com.example.android.feelthetour;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import static android.R.attr.password;

public class RegisterActivity extends AppCompatActivity {



    private EditText mNameEditText, mEmailEditText,
            mPasswordEditText, mRePasswordEditText ;
    private Button mRegisterButton;
    private TextView mLoginTextView;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //if the user is already logged in we will directly start the profile activity
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, WelcomeActivity.class));
            return;
        }

        mNameEditText = (EditText) findViewById(R.id.name_edit_text);
        mEmailEditText = (EditText) findViewById(R.id.email_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);
        mRePasswordEditText = (EditText) findViewById(R.id.repass_edit_text);
        mRegisterButton = (Button) findViewById(R.id.register_button);
        mLoginTextView = (TextView) findViewById(R.id.login_text);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                registerUser();
            }
        });

        mLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View view){
                    //start the login activity
                    finish();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
        });

    }

    private void registerUser() {

        final String fullName = mNameEditText.getText().toString().trim();
        final String email = mEmailEditText.getText().toString().trim();
        final String password = mPasswordEditText.getText().toString();
        final String rePassword = mRePasswordEditText.getText().toString();

//        if (!TextUtils.isEmpty(fullName) && !TextUtils.isEmpty(email)
//                && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(rePassword)) {
//            if (hasOnlyAlphAndWSpaces(fullName) && isValidEmail(email) && password.equals(rePassword)) {
////                RegisterUser ru = new RegisterUser();
////                ru.execute();
//
//            } else if (!hasOnlyAlphAndWSpaces(fullName)) {
//                setErrorMessage(mNameEditText, "Is this really your name ?");
//            } else if (!isValidEmail(email)) {
//                setErrorMessage(mEmailEditText, "Unsupported email format");
//            } else if (!password.equals(rePassword)) {
//                setErrorMessage(mRePasswordEditText, "Password does not match!!!");
//            } else {
//                Toast.makeText(getApplicationContext(), "Unexpected error occured", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            //check which is empty
//            if (TextUtils.isEmpty(fullName)) {
//                setErrorMessage(mNameEditText, "Please provide your name");
//            } else if (TextUtils.isEmpty(email)) {
//                setErrorMessage(mEmailEditText, "Enter your email address here");
//            } else if (TextUtils.isEmpty(password)) {
//                setErrorMessage(mPasswordEditText, "Password please");
//            } else if (TextUtils.isEmpty(rePassword)) {
//                setErrorMessage(mRePasswordEditText, "Please re-enter your password to verify it");
//            }
//        }
//
//
//    private void setErrorMessage (EditText errorEditText, String errorMessage){
//            errorEditText.setText("");
//            errorEditText.setHint(errorMessage);
//            errorEditText.setHintTextColor(Color.RED);
//        }
//
//    private boolean hasOnlyAlphAndWSpaces(String s) {
//        Pattern p = Pattern.compile("^[ A-z]+$");
//        Matcher m = p.matcher(s);
//        return m.matches();
//    }
//
//    private boolean isValidEmail(String s) {
//        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(s);
//        return matcher.find();
//    }

        if (TextUtils.isEmpty(fullName)) {
            mNameEditText.setError("Please provide your name");
            mNameEditText.requestFocus();
            return;
        }
        if (!hasOnlyAlphAndWSpaces(fullName)) {
            mNameEditText.setError("Is this really your name?");
            mNameEditText.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            mEmailEditText.setError("Please enter your email address");
            mEmailEditText.requestFocus();
            return;
        }

//        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            mEmailEditText.setError("Enter a valid email");
//            mEmailEditText.requestFocus();
//            return;
//        }

         if (!isValidEmail(email)) {
             mEmailEditText.setError("Enter a valid email address");
             mEmailEditText.requestFocus();
                return;
            }

        if (TextUtils.isEmpty(password)) {
            mPasswordEditText.setError("Enter a password");
            mPasswordEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(rePassword)) {
            mRePasswordEditText.setError("Please re-enter your password to verify it");
            mRePasswordEditText.requestFocus();
            return;
        }

        if(!(password.equals(rePassword))){
            mRePasswordEditText.setError("Password does not match!!!");
            mRePasswordEditText.requestFocus();
            return;
        }

    class RegisterUser extends AsyncTask<Void, Void, String> {

        private ProgressBar progressBar;

        @Override
        protected String doInBackground(Void... voids) {

            //creating request handler object
            RequestHandler requestHandler = new RequestHandler();

            //creating request parameters
            HashMap<String, String> params = new HashMap<>();
            params.put("username", fullName);
            params.put("useremail", email);
            params.put("password", password);

            //returing the response
            return requestHandler.sendPostRequest(URLs.URL_REGISTER, params);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           // displaying the progress bar while user registers on the server
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //hiding the progressbar after completion
            progressBar.setVisibility(View.GONE);

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
//                        SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);

                    //starting the profile activity
                    finish();
                    startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Some error occurred", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

//    //executing the async task

            RegisterUser ru = new RegisterUser();
            ru.execute();
  }

    private boolean hasOnlyAlphAndWSpaces(String s) {
        Pattern p = Pattern.compile("^[ A-z]+$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    private boolean isValidEmail(String s) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(s);
        return matcher.find();
    }


}