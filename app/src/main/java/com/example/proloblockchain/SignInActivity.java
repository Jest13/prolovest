package com.example.proloblockchain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.proloblockchain.helpers.StringHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    Button sign_in_btn;
    EditText et_email, et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        et_email = findViewById(R.id.SignInemail);
        et_password = findViewById(R.id.SignInPassword);
        sign_in_btn = findViewById(R.id.login);

        if (et_email == null || et_password == null || sign_in_btn == null) {
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_LONG).show();
            Log.e("SignInActivity", "Initialization error: et_email, et_password or sign_in_btn is null");
            return;
        }

        sign_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticateUser();
            }
        });
    }

    public void authenticateUser(){
        if(!validateEmail() || !validatePassword()){
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(SignInActivity.this);
        String url = "http://192.168.1.176:8081/api/v1/user/login";

        HashMap<String, String> params = new HashMap<>();
        params.put("email", et_email.getText().toString());
        params.put("password", et_password.getText().toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String first_name = response.getString("first_name");
                    String last_name = response.getString("last_name");
                    String email = response.getString("email");

                    Intent goToProfile = new Intent(SignInActivity.this, ProfileActivity.class);
                    goToProfile.putExtra("first_name", first_name);
                    goToProfile.putExtra("last_namee", last_name);
                    goToProfile.putExtra("email", email);
                    startActivity(goToProfile);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("SignInActivity", "JSON Exception: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("SignInActivity", "Volley Error: " + error.getMessage());
                Toast.makeText(SignInActivity.this, "Login Failed", Toast.LENGTH_LONG).show();
            }
        });

        // Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions. Volley does retry for you if you have specified the policy.
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000, // timeout in milliseconds (10 seconds)
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // number of retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)); // backoff multiplier

        queue.add(jsonObjectRequest);
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

    public boolean validateEmail(){
        String email_e = et_email.getText().toString();
        if (email_e.isEmpty()) {
            et_email.setError("Email cannot be empty!");
            return false;
        } else if (!StringHelper.regexEmailValidationPattern(email_e)) {
            et_email.setError("Please enter a valid email");
            return false;
        } else {
            et_email.setError(null);
            return true;
        }
    }

    public boolean validatePassword(){
        String password_p = et_password.getText().toString();
        if (password_p.isEmpty()) {
            et_password.setError("Password cannot be empty!");
            return false;
        } else {
            et_password.setError(null);
            return true;
        }
    }
}
