package com.example.proloblockchain;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prolovest.R;

public class MainActivity extends AppCompatActivity {

    Button sign_in, sign_up;
    private CryptoPriceManager cryptoPriceManager;

    private TextView tvBtcPrice, tvBtcChange;
    private TextView tvEthPrice, tvEthChange;
    private TextView tvSolPrice, tvSolChange;
    private TextView tvDogePrice, tvDogeChange;
    private TextView tvXchPrice, tvXchChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        View marqueeView = findViewById(R.id.tv_marquee);
        if (marqueeView != null) {
            marqueeView.setSelected(true);
        }

        cryptoPriceManager = new CryptoPriceManager(this);
        loadCryptoPrices();
    }

    private void initViews() {
        tvBtcPrice = findViewById(R.id.tv_btc_price);
        tvBtcChange = findViewById(R.id.tv_btc_change);

        tvEthPrice = findViewById(R.id.tv_eth_price);
        tvEthChange = findViewById(R.id.tv_eth_change);

        tvSolPrice = findViewById(R.id.tv_sol_price);
        tvSolChange = findViewById(R.id.tv_sol_change);

        tvDogePrice = findViewById(R.id.tv_doge_price);
        tvDogeChange = findViewById(R.id.tv_doge_change);

        tvXchPrice = findViewById(R.id.tv_xch_price);
        tvXchChange = findViewById(R.id.tv_xch_change);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCryptoPrices();
    }

    private void loadCryptoPrices() {
        if (cryptoPriceManager != null) {
            cryptoPriceManager.fetchLiveCryptoPrices(
                    tvBtcPrice, tvBtcChange,
                    tvEthPrice, tvEthChange,
                    tvSolPrice, tvSolChange,
                    tvDogePrice, tvDogeChange,
                    tvXchPrice, tvXchChange
            );
        }
    }

    public void goToSignUp(View view) {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToSignIn(View view) {
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
}