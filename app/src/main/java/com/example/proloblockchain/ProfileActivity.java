package com.example.proloblockchain;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.prolovest.R;

import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvBalance;
    private TextView tvBalanceCents;
    private TextView tvCurrencySymbol;
    private RequestQueue requestQueue;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialisation des vues
        tvWelcome = findViewById(R.id.textView10);
        tvBalance = findViewById(R.id.textView12);
        tvBalanceCents = findViewById(R.id.textView13);
        // tvCurrencySymbol = findViewById(R.id.textViewCurrencySymbol);

        // Configuration du symbole €
        tvCurrencySymbol.setText("€");

        // Initialisation de Volley
        requestQueue = Volley.newRequestQueue(this);

        // Récupération de l'email depuis l'Intent
        String email = getIntent().getStringExtra("email");
        if (email != null) {
            fetchProloBalance(email);
        } else {
            Log.e("ProfileActivity", "Email non reçu dans l'intent");
        }
    }

    private void fetchProloBalance(String email) {
        String url = "http://192.168.1.190:8080/api/v1/user/balance/" + email;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        BigDecimal balance = new BigDecimal(response.getString("balance"));
                        formatAndDisplayBalance(balance);
                    } catch (JSONException e) {
                        Log.e("BalanceError", "Erreur de parsing JSON", e);
                        showBalanceError();
                    } catch (NumberFormatException e) {
                        Log.e("BalanceError", "Format de balance invalide", e);
                        showBalanceError();
                    }
                },
                error -> {
                    Log.e("BalanceError", "Erreur réseau: " + error.getMessage());
                    showNetworkError();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer VOTRE_JWT_TOKEN");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void formatAndDisplayBalance(BigDecimal balance) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);

        String formatted = format.format(balance);
        // Supprimer le symbole € déjà présent dans le layout
        String amount = formatted.replace("€", "").trim();
        String[] parts = amount.split(",");

        runOnUiThread(() -> {
            tvBalance.setText(parts[0]);
            if (parts.length > 1) {
                tvBalanceCents.setText("," + parts[1]);
            } else {
                tvBalanceCents.setText(",00");
            }
        });
    }

    private void showBalanceError() {
        runOnUiThread(() -> {
            tvBalance.setText("--");
            tvBalanceCents.setText(",--");
        });
    }

    private void showNetworkError() {
        runOnUiThread(() -> {
            tvBalance.setText("Hors");
            tvBalanceCents.setText(",ligne");
        });
    }

    public void signUserOut() {
        startActivity(new Intent(this, com.example.proloblockchain.MainActivity.class));
        finish();
    }
}