package com.example.proloblockchain;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prolovest.R;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etFirstName, etLastName;
    private TextView tvEmailDisplay;
    private Button btnSaveProfile, btnLogout;

    private String userEmail;
    private String userFirstName;
    private String userLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userEmail = getIntent().getStringExtra("email");
        userFirstName = getIntent().getStringExtra("first_name");
        userLastName = getIntent().getStringExtra("last_name");

        initViews();
        prefillData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        tvEmailDisplay = findViewById(R.id.tv_email_display);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void prefillData() {
        if (userEmail != null) tvEmailDisplay.setText(userEmail);
        if (userFirstName != null) etFirstName.setText(userFirstName);
        if (userLastName != null) etLastName.setText(userLastName);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSaveProfile.setOnClickListener(v -> {
            String newFirstName = etFirstName.getText().toString().trim();
            String newLastName = etLastName.getText().toString().trim();

            if (newFirstName.isEmpty() || newLastName.isEmpty()) {
                Toast.makeText(this, "Les champs prénom et nom ne peuvent pas être vides", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Ajouter l'appel API pour mettre à jour les infos utilisateur sur le serveur
            // Pour l'instant, on simule une sauvegarde locale réussie
            userFirstName = newFirstName;
            userLastName = newLastName;
            Toast.makeText(this, "✅ Profil mis à jour avec succès !", Toast.LENGTH_SHORT).show();

            // Renvoyer les nouvelles données à l'activité précédente
            Intent resultIntent = new Intent();
            resultIntent.putExtra("first_name", userFirstName);
            resultIntent.putExtra("last_name", userLastName);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        btnLogout.setOnClickListener(v -> {
            // Déconnexion : on vide tout et on renvoie vers MainActivity
            Toast.makeText(this, "👋 Déconnexion réussie", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            // Empêcher le retour en arrière vers le profil une fois déconnecté
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}