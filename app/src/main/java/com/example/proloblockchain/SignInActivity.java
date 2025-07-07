package com.example.prolovest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.prolovest.helpers.StringHelper;

import org.json.JSONObject;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private Button sign_in_btn;
    private EditText et_email, et_password;
    private ProgressDialog progressDialog; // Ajout d'un indicateur de chargement

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialisation des vues
        et_email = findViewById(R.id.SignInemail);
        et_password = findViewById(R.id.SignInPassword);
        sign_in_btn = findViewById(R.id.login);

        // Vérification des vues
        if (et_email == null || et_password == null || sign_in_btn == null) {
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_LONG).show();
            Log.e("SignInActivity", "Initialization error: et_email, et_password or sign_in_btn is null");
            return;
        }

        // Ajout de l'indicateur de chargement
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        // Gestion du clic sur le bouton
        sign_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticateUser();
            }
        });
    }

    private void authenticateUser() {
        if (!validateEmail() || !validatePassword()) {
            return;
        }

        // Afficher l'indicateur de chargement
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(SignInActivity.this);
        String url = "http://192.168.1.82:8080/api/v1/user/login"; // IP virtuelle pour emulateur virtuelle
     //   String url = "http://10.188.222.200:8081/api/v1/user/login"; // IP WIFI pour pour emulateur physique


        HashMap<String, String> params = new HashMap<>();
        params.put("email", et_email.getText().toString().trim());
        params.put("password", et_password.getText().toString().trim());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        progressDialog.dismiss(); // Masquer l'indicateur de chargement
                        handleLoginSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss(); // Masquer l'indicateur de chargement
                        handleLoginError(error);
                    }
                });

        // Configuration de la politique de réessai
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000, // Timeout en millisecondes
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // Nombre de tentatives
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT // Facteur de backoff
        ));

        queue.add(jsonObjectRequest);
    }

    private void handleLoginSuccess(JSONObject response) {
        String first_name = response.optString("first_name", "N/A");
        String last_name = response.optString("last_name", "N/A");
        String email = response.optString("email", "N/A");

        Intent goToProfile = new Intent(SignInActivity.this, com.example.prolovest.ProfileActivity.class);
        goToProfile.putExtra("first_name", first_name);
        goToProfile.putExtra("last_name", last_name);
        goToProfile.putExtra("email", email);
        startActivity(goToProfile);
        finish();
    }


    private void handleLoginError(VolleyError error) {
        error.printStackTrace();
        String errorMessage = "Login failed. Please try again.";

        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            if (statusCode == 401) {
                errorMessage = "Invalid credentials. Please check your email and password.";
            } else if (statusCode >= 500) {
                errorMessage = "Server error. Please try again later.";
            }
        } else if (error.getCause() instanceof java.net.UnknownHostException) {
            errorMessage = "No internet connection. Please check your network.";
        }

        Log.e("SignInActivity", "Volley Error: " + error.getMessage());
        Toast.makeText(SignInActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    public void goToHome(View view) {
        Intent intent = new Intent(SignInActivity.this, com.example.prolovest.MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToSigUpAct(View view) {
        Intent intent = new Intent(SignInActivity.this, com.example.prolovest.SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean validateEmail() {
        String email = et_email.getText().toString().trim();
        if (email.isEmpty()) {
            et_email.setError("Email cannot be empty!");
            return false;
        } else if (!StringHelper.regexEmailValidationPattern(email)) {
            et_email.setError("Please enter a valid email");
            return false;
        } else {
            et_email.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = et_password.getText().toString().trim();
        if (password.isEmpty()) {
            et_password.setError("Password cannot be empty!");
            return false;
        } else {
            et_password.setError(null);
            return true;
        }
    }
}
