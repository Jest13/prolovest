package com.example.proloblockchain;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class cryptoInformation extends AppCompatActivity {
    TextView live_price, crypto_logo, crypto_name;
    Button back_to_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        live_price = findViewById(R.id.live_price);
        crypto_logo = findViewById(R.id.crypto_logo);
        crypto_name = findViewById(R.id.crypto_name);

        String live_price = getIntent().getStringExtra("live_price");
        String crypto_logo = getIntent().getStringExtra("crypto_logo");
        String crypto_name = getIntent().getStringExtra("crypto_name");

        tv_first_name.setText(first_name);
        tv_last_name.setText(last_name);
        tv_email.setText(email);



    }
