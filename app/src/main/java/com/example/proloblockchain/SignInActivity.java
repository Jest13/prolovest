package com.example.proloblockchain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
    }

    public void goToHome(View view) {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    public void goToSigUpAct(View view) {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }
}