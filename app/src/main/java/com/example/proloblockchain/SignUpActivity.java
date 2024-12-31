package com.example.proloblockchain;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.proloblockchain.helpers.StringHelper;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {


    EditText first_name, last_name, email, password, confirm;
    Button sign_up_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        // Hook Edit Text Fields
        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm = findViewById(R.id.confirmPassword);


        // Hook Sign Up Button
        sign_up_btn = findViewById(R.id.sign_up_btn);

        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processFormFields();
            }
        });


    }
    public void goToHome(View view) {
         Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToSigInAct(View view) {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    public void processFormFields(){

        // verification des champs
        if(!validateFirstName() || !validateLastName() || !validateEmail() || !validatePasswordAndConfirm()){
            return;
        }

        // creation de l'instance Volley pour les requetes

        RequestQueue queue = Volley.newRequestQueue(SignUpActivity.this);

        // definition du point de controle api spring
        String url = "http://172.22.240.1:8081/api/v1/user/register";


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                if(response.equalsIgnoreCase("Success")){
                    first_name.setText(null);
                    last_name.setText(null);
                    email.setText(null);
                    password.setError(null);
                    confirm.setText(null);
                    Toast.makeText(SignUpActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                System.out.println(error.getMessage());
                Toast.makeText(SignUpActivity.this, "Registration Un-Successful", Toast.LENGTH_LONG).show();

            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("first_name", first_name.getText().toString());
                params.put("last_name", last_name.getText().toString());
                params.put("email", email.getText().toString());
                params.put("password", password.getText().toString());
                return params;
            }
        };

        queue.add(stringRequest);

    }

    public boolean validateFirstName(){
        String firstName = first_name.getText().toString();
        if(firstName.isEmpty()){
            first_name.setError("First name cannot be empty !");
            return false;
        }else{
            first_name.setError(null);
            return true;
        }
    }

    public boolean validateLastName(){
        String lastName = last_name.getText().toString();
        if(lastName.isEmpty()){
            last_name.setError("last name cannot be empty !");
            return false;
        }else{
            last_name.setError(null);
            return true;
        }
    }

    public boolean validateEmail(){
        String email_e = email.getText().toString();
        if(email_e.isEmpty()){
            email.setError("Email cannot be empty !");
            return false;
        }else if(!StringHelper.regexEmailValidationPattern(email_e)){
            email.setError("Please enter a valid email");
            return false;
        }else{
            email.setError(null);
            return true;
        }
    }

    public boolean validatePasswordAndConfirm(){
        String password_p = password.getText().toString();
        String confirm_p = confirm.getText().toString();


        if(password_p.isEmpty()){
            password.setError("Password cannot be empty !");
            confirm.setError("Confirm name cannot be empty !");
            return false;
        }else if(!password_p.equals(confirm_p)){
            password.setError("Passwords do not match!");
            return false;
        }else if(confirm_p.isEmpty()){
            confirm.setError("Confirm name cannot be empty !");
            return false;
        }else{
            password.setError(null);
            confirm.setError(null);
            return true;
        }
    }



}