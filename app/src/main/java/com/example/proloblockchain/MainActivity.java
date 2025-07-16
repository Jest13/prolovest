package com.example.proloblockchain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.prolovest.R;
public class MainActivity extends AppCompatActivity {

    Button sign_in, sign_up;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // End Of On Create Method

    public void goToSignUp(View view){
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    // End Of Go To Sign Up Activity

    public void goToSignIn(View view){
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }



}