package com.example.proloblockchain;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.proloblockchain.transactions.FastBuyActivity;
import com.example.prolovest.R;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final String BASE_URL = "http://82.230.48.228:32769/api/v1/user";
    private static final BigDecimal PROLO_TO_USD_RATE = new BigDecimal("1.24");

    private TextView tvWelcome;
    private TextView tvProBalance;
    private TextView tvProDecimals;
    private TextView tvUsdEquivalent;
    private TextView tvBlockchainAddress;

    private RequestQueue requestQueue;
    private String currentUserEmail;
    private String fullProloAddress = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupAddressCopyListener();

        requestQueue = Volley.newRequestQueue(this);

        currentUserEmail = getIntent().getStringExtra("email");
        String firstName = getIntent().getStringExtra("first_name");
        String lastName = getIntent().getStringExtra("last_name");

        if (firstName != null && !firstName.isEmpty()) {
            tvWelcome.setText("Hello " + firstName + " " + (lastName != null ? lastName : ""));
        } else if (currentUserEmail != null) {
            tvWelcome.setText("Hello " + currentUserEmail.split("@")[0]);
        }

        if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
            fetchProloBalance(currentUserEmail);
            fetchProloAddress(currentUserEmail);
        } else {
            Log.e(TAG, "Email non reçu dans l'intent");
            showBalanceError();
        }
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.textView10);
        tvProBalance = findViewById(R.id.tv_pro_balance);
        tvProDecimals = findViewById(R.id.tv_pro_decimals);
        tvUsdEquivalent = findViewById(R.id.tv_usd_equivalent);
        tvBlockchainAddress = findViewById(R.id.tv_blockchain_address);
    }

    private void setupAddressCopyListener() {
        if (tvBlockchainAddress != null) {
            tvBlockchainAddress.setOnClickListener(v -> {
                if (fullProloAddress != null && !fullProloAddress.isEmpty()) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Prolo Wallet Address", fullProloAddress);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, "📋 Adresse Prolo copiée !", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
            fetchProloBalance(currentUserEmail);
            fetchProloAddress(currentUserEmail);
        }
    }

    private void fetchProloBalance(String email) {
        String url = BASE_URL + "/balance/" + email;

        StringRequest request = new StringRequest(
                Request.Method.GET, url,
                response -> {
                    try {
                        String raw = response.trim();
                        BigDecimal balance;

                        if (raw.startsWith("{")) {
                            JSONObject json = new JSONObject(raw);
                            String balStr = json.optString("balance", json.optString("prolo_balance", "0"));
                            balance = new BigDecimal(balStr);
                        } else {
                            balance = new BigDecimal(raw);
                        }

                        formatAndDisplayBalance(balance);

                    } catch (Exception e) {
                        Log.e(TAG, "Erreur de parsing de la balance: " + response, e);
                        showBalanceError();
                    }
                },
                error -> {
                    Log.e(TAG, "Erreur réseau balance: " + error.getMessage());
                    showNetworkError();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Connection", "close");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 2, 1.0f));
        requestQueue.add(request);
    }

    private void fetchProloAddress(String email) {
        String url = BASE_URL + "/prolo-address/" + email;

        StringRequest request = new StringRequest(
                Request.Method.GET, url,
                response -> {
                    String address = response.trim().replace("\"", "");
                    if (!address.isEmpty()) {
                        this.fullProloAddress = address;
                        displayProloAddress(address);
                    }
                },
                error -> Log.e(TAG, "Erreur récupération adresse Prolo", error)
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Connection", "close");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 2, 1.0f));
        requestQueue.add(request);
    }

    private void formatAndDisplayBalance(BigDecimal balance) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRANCE);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);

        String formatted = format.format(balance);
        String[] parts = formatted.split(",");

        BigDecimal usdValue = balance.multiply(PROLO_TO_USD_RATE);
        NumberFormat usdFormat = NumberFormat.getCurrencyInstance(Locale.US);
        String usdFormatted = usdFormat.format(usdValue);

        runOnUiThread(() -> {
            tvProBalance.setText(parts[0]);
            if (parts.length > 1) {
                tvProDecimals.setText("." + parts[1]);
            } else {
                tvProDecimals.setText(".00");
            }
            if (tvUsdEquivalent != null) {
                tvUsdEquivalent.setText("≈ " + usdFormatted + " USD");
            }
        });
    }

    private void displayProloAddress(String address) {
        runOnUiThread(() -> {
            if (tvBlockchainAddress != null) {
                String shortAddr = address;
                if (address.length() > 18) {
                    shortAddr = address.substring(0, 10) + "..." + address.substring(address.length() - 8);
                }
                tvBlockchainAddress.setText("🔗 Adresse Prolo: " + shortAddr);
            }
        });
    }

    private void showBalanceError() {
        runOnUiThread(() -> {
            tvProBalance.setText("--");
            tvProDecimals.setText(".--");
            if (tvUsdEquivalent != null) {
                tvUsdEquivalent.setText("≈ $-- USD");
            }
        });
    }

    private void showNetworkError() {
        runOnUiThread(() -> {
            tvProBalance.setText("Hors");
            tvProDecimals.setText(" ligne");
            if (tvUsdEquivalent != null) {
                tvUsdEquivalent.setText("≈ En attente de réseau");
            }
        });
    }

    public void goToFastBuy(View view) {
        Intent intent = new Intent(ProfileActivity.this, FastBuyActivity.class);
        startActivity(intent);
    }

    public void goToMining(View view) {
        Intent intent = new Intent(ProfileActivity.this, MiningActivity.class);
        intent.putExtra("email", currentUserEmail);
        startActivity(intent);
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
        intent.putExtra("email", currentUserEmail);
        
        // Extraction du prénom et du nom actuels de la vue tvWelcome
        String welcomeText = tvWelcome.getText().toString();
        if (welcomeText.startsWith("Hello ")) {
            String[] nameParts = welcomeText.substring(6).split(" ", 2);
            if (nameParts.length > 0) intent.putExtra("first_name", nameParts[0]);
            if (nameParts.length > 1) intent.putExtra("last_name", nameParts[1]);
        }
        
        startActivity(intent);
    }

    public void signUserOut() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}