package com.example.prolovest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.prolovest.MainActivity;

public class ProfileActivity extends AppCompatActivity {

    TextView tv_first_name, tv_last_name, tv_email;
//    Button sign_out_btn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Hook TextView Objects:
        tv_first_name = findViewById(R.id.first_name);
        tv_last_name = findViewById(R.id.last_name);
        tv_email = findViewById(R.id.email);
     //   sign_out_btn = findViewById(R.id.sign_out_btn); // Assurez-vous que l'ID est correct

        if (tv_first_name == null || tv_last_name == null || tv_email == null ) {
            Log.e("ProfileActivity", "Erreur : Un des éléments UI est null !");
            return;
        }

        String first_name = getIntent().getStringExtra("first_name");
        String last_name = getIntent().getStringExtra("last_name");
        String email = getIntent().getStringExtra("email");

        Log.d("ProfileActivity", "First name: " + first_name);
        Log.d("ProfileActivity", "Last name: " + last_name);
        Log.d("ProfileActivity", "Email: " + email);

        // Vérifier si les valeurs ne sont pas null avant d'affecter
        if (first_name != null) tv_first_name.setText(first_name);
        if (last_name != null) tv_last_name.setText(last_name);
        if (email != null) tv_email.setText(email);

        // Set On Click Listener:
  //      sign_out_btn.setOnClickListener(new View.OnClickListener() {
    //        @Override
      //      public void onClick(View view) {
       //         signUserOut();
        //    }
       // });
   }

    public void signUserOut() {
        tv_first_name.setText(null);
        tv_last_name.setText(null);
        tv_email.setText(null);

        Intent goToHome = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(goToHome);
        finish();
    }
}
