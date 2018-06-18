package com.example.android.feelthetour;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {
//    private Button mlogoutfb;
//    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //if the user is not logged in
        //starting the login activity
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        findViewById(R.id.buttonLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                SharedPrefManager.getInstance(getApplicationContext()).logout();
            }
        });
//        mlogoutfb=(Button)findViewById(R.id.button_logout_fb);
//        mAuth = FirebaseAuth.getInstance();
//        mlogoutfb.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                    mAuth.signOut();
//            }
//    });

    }


//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            updateUI();
//        }
//    }
//    private void updateUI(){
//        Toast.makeText(WelcomeActivity.this,"You are logged out ",Toast.LENGTH_LONG).show();
//        Intent newIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
//        startActivity(newIntent);
//        finish();
//    }

}
