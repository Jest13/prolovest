package com.example.proloblockchain;

import android.os.Bundle;
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
import com.example.prolovest.R;
import com.example.prolovest.helpers.StringHelper;

import org.json.JSONException;
import org.json.JSONObject;

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
        if(!validateFirstName() || !validateLastName() || !validateEmail() || !validatePasswordAndConfirm()){
            return;
        }

        String url = "http://192.168.1.190:8080/api/v1/user/register";

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

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> {
                    String message = response.optString("message", "");
                    if(message.equalsIgnoreCase("Success")){
                        clearFields();
                        Toast.makeText(SignUpActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
                    } else {
                        String error = response.optString("error", "Registration failed");
                        Toast.makeText(SignUpActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    String errorMessage = "Registration failed";
                    if(error.networkResponse != null && error.networkResponse.data != null){
                        try {
                            String body = new String(error.networkResponse.data);
                            JSONObject obj = new JSONObject(body);
                            errorMessage = obj.optString("error", errorMessage);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );

        // Timeout un peu plus long
        request.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }

    private void clearFields(){
        first_name.setText("");
        last_name.setText("");
        email.setText("");
        password.setText("");
        confirm.setText("");
    }

    // Validation
    public boolean validateFirstName(){
        String firstName = first_name.getText().toString().trim();
        if(firstName.isEmpty()){
            first_name.setError("First name cannot be empty !");
            return false;
        }
        first_name.setError(null);
        return true;
    }

    public boolean validateLastName(){
        String lastName = last_name.getText().toString().trim();
        if(lastName.isEmpty()){
            last_name.setError("Last name cannot be empty !");
            return false;
        }
        last_name.setError(null);
        return true;
    }

    public boolean validateEmail(){
        String email_e = email.getText().toString().trim();
        if(email_e.isEmpty()){
            email.setError("Email cannot be empty !");
            return false;
        }else if(!StringHelper.regexEmailValidationPattern(email_e)){
            email.setError("Please enter a valid email");
            return false;
        }
        email.setError(null);
        return true;
    }

    public boolean validatePasswordAndConfirm(){
        String password_p = password.getText().toString().trim();
        String confirm_p = confirm.getText().toString().trim();

        if(password_p.isEmpty()){
            password.setError("Password cannot be empty !");
            confirm.setError("Confirm password cannot be empty !");
            return false;
        } else if(!password_p.equals(confirm_p)){
            password.setError("Passwords do not match!");
            return false;
        }
        password.setError(null);
        confirm.setError(null);
        return true;
    }

}
