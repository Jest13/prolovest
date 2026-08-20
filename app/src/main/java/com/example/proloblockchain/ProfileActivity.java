package com.example.proloblockchain;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.proloblockchain.transactions.FastBuyActivity;
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
    private TextView tvProBalance;
    private TextView tvProDecimals;
    private RequestQueue requestQueue;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialisation des vues avec les NOUVEAUX IDs du design propre
        tvWelcome = findViewById(R.id.textView10);
        tvProBalance = findViewById(R.id.tv_pro_balance);
        tvProDecimals = findViewById(R.id.tv_pro_decimals);

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
        String url = "http://82.230.48.228:32769/api/v1/user/balance/" + email;

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
                // N'oublie pas qu'il faudra remplacer ça par le vrai Token un jour !
                headers.put("Authorization", "Bearer VOTRE_JWT_TOKEN");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void formatAndDisplayBalance(BigDecimal balance) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRANCE);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);

        String formatted = format.format(balance);
        String[] parts = formatted.split(",");

        runOnUiThread(() -> {
            // Affiche la partie entière (ex: 1 250)
            tvProBalance.setText(parts[0]);
            // Affiche la partie décimale avec le point (ex: .50)
            if (parts.length > 1) {
                tvProDecimals.setText("." + parts[1]);
            } else {
                tvProDecimals.setText(".00");
            }
        });
    }

    private void showBalanceError() {
        runOnUiThread(() -> {
            tvProBalance.setText("--");
            tvProDecimals.setText(".--");
        });
    }

    private void showNetworkError() {
        runOnUiThread(() -> {
            tvProBalance.setText("Hors");
            tvProDecimals.setText(" ligne");
        });
    }

    public void goToFastBuy(View view) {
        Intent intent = new Intent(ProfileActivity.this, FastBuyActivity.class);
        startActivity(intent);
    }

    public void signUserOut() {
        startActivity(new Intent(this, com.example.proloblockchain.MainActivity.class));
        finish();
    }
}