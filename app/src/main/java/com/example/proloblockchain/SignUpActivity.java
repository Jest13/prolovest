package com.example.proloblockchain;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.proloblockchain.helpers.StringHelper;
import com.example.prolovest.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    EditText first_name, last_name, email, password, confirm;
    Button sign_up_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm = findViewById(R.id.confirmPassword);
        sign_up_btn = findViewById(R.id.sign_up_btn);

        sign_up_btn.setOnClickListener(v -> processFormFields());
    }

    private void processFormFields() {
        if (!validateFirstName() || !validateLastName() || !validateEmail() || !validatePasswordAndConfirm()) {
            return;
        }

        String url = "http://82.230.48.228:32769/api/v1/user/register";

        JSONObject params = new JSONObject();
        try {
            params.put("first_name", first_name.getText().toString().trim());
            params.put("last_name", last_name.getText().toString().trim());
            params.put("email", email.getText().toString().trim());
            params.put("password", password.getText().toString().trim());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create request", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(SignUpActivity.this);

        String userEmailText = email.getText().toString().trim();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> {
                    String message = response.optString("message", "");
                    if (message.equalsIgnoreCase("Success") || response.toString().contains("Success")) {
                        clearFields();
                        Toast.makeText(SignUpActivity.this, "Compte créé avec succès !", Toast.LENGTH_LONG).show();

                        Intent goToSignIn = new Intent(SignUpActivity.this, SignInActivity.class);
                        goToSignIn.putExtra("email", userEmailText);
                        startActivity(goToSignIn);
                        finish();
                    } else {
                        String error = response.optString("error", "Échec de l'inscription");
                        Toast.makeText(SignUpActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    if (error instanceof ParseError && error.networkResponse != null) {
                        try {
                            String rawBody = new String(error.networkResponse.data, "UTF-8").trim();
                            Log.d("PROLOVEST_DEBUG", "rawBody = [" + rawBody + "]");
                            if (rawBody.equalsIgnoreCase("success")) {
                                clearFields();
                                Toast.makeText(SignUpActivity.this, "Compte créé avec succès !", Toast.LENGTH_LONG).show();

                                Intent goToSignIn = new Intent(SignUpActivity.this, SignInActivity.class);
                                goToSignIn.putExtra("email", userEmailText);
                                startActivity(goToSignIn);
                                finish();
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    String errorMessage = "Échec de l'inscription";

                    if (error instanceof TimeoutError) {
                        errorMessage = "⏳ Le serveur (82.230.48.228) n'a pas répondu à temps (Timeout). Si vous êtes en 4G/5G, vérifiez si votre routeur autorise le port 32769 ou connectez-vous en Wi-Fi.";
                    } else if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;

                        if (statusCode == 409) {
                            errorMessage = "Cette adresse email est déjà utilisée !";
                        } else if (error.networkResponse.data != null) {
                            try {
                                String body = new String(error.networkResponse.data, "UTF-8");
                                Log.d("PROLOVEST_DEBUG", "errorBody = [" + body + "]");
                                JSONObject obj = new JSONObject(body);
                                errorMessage = obj.optString("error", errorMessage);
                            } catch (Exception e) {
                                try {
                                    errorMessage = new String(error.networkResponse.data, "UTF-8").trim();
                                } catch (Exception ex) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        errorMessage = "Impossible de joindre le serveur 82.230.48.228. Vérifiez votre connexion réseau.";
                    }

                    Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Connection", "close");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                25000,
                2,
                1.0f
        ));

        queue.add(request);
    }

    private void clearFields() {
        first_name.setText("");
        last_name.setText("");
        email.setText("");
        password.setText("");
        confirm.setText("");
    }

    public boolean validateFirstName() {
        String firstName = first_name.getText().toString().trim();
        if (firstName.isEmpty()) {
            first_name.setError("First name cannot be empty !");
            return false;
        }
        first_name.setError(null);
        return true;
    }

    public boolean validateLastName() {
        String lastName = last_name.getText().toString().trim();
        if (lastName.isEmpty()) {
            last_name.setError("Last name cannot be empty !");
            return false;
        }
        last_name.setError(null);
        return true;
    }

    public boolean validateEmail() {
        String email_e = email.getText().toString().trim();
        if (email_e.isEmpty()) {
            email.setError("Email cannot be empty !");
            return false;
        } else if (!StringHelper.regexEmailValidationPattern(email_e)) {
            email.setError("Please enter a valid email");
            return false;
        }
        email.setError(null);
        return true;
    }

    public boolean validatePasswordAndConfirm() {
        String password_p = password.getText().toString().trim();
        String confirm_p = confirm.getText().toString().trim();

        if (password_p.isEmpty()) {
            password.setError("Password cannot be empty !");
            confirm.setError("Confirm password cannot be empty !");
            return false;
        } else if (!password_p.equals(confirm_p)) {
            password.setError("Passwords do not match!");
            return false;
        }
        password.setError(null);
        confirm.setError(null);
        return true;
    }
    public void goToHome(View view) {
        // RETIRE le finish() ici pour ne pas tuer l'activité principale en arrière-plan
        Intent intent = new Intent(SignUpActivity.this, com.example.proloblockchain.MainActivity.class);
        startActivity(intent);
    }

    public void goToSigUpAct(View view) {
        // Ici tu peux laisser ou enlever finish() selon si tu veux empiler ou remplacer la page
        Intent intent = new Intent(SignUpActivity.this, com.example.proloblockchain.SignUpActivity.class);
        startActivity(intent);
    }
}