package com.example.proloblockchain;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.prolovest.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MiningActivity extends AppCompatActivity implements MiningManager.MiningListener {

    private ImageView btnBack;
    private TextView tvStatusBadge;
    private TextView tvHashrateValue, tvMinedReward, tvBlocksFound, tvTotalNonces;
    private Button btnToggleMining;
    private RadioGroup rgThreads;
    private RadioButton rbThread1, rbThread2, rbThread4;
    private SwitchCompat switchAutoContinue;
    private TextView tvChallengeInfo;
    private TextView tvConsoleLogs;
    private ScrollView scrollLogs;

    private MiningManager miningManager;
    private String userEmail;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mining);

        userEmail = getIntent().getStringExtra("email");
        if (userEmail == null || userEmail.trim().isEmpty()) {
            userEmail = "mineur@prolo.org"; // default fallback
        }

        initViews();
        setupListeners();

        miningManager = new MiningManager(this, userEmail);
        miningManager.setListener(this);
        miningManager.setThreadCount(2);

        appendLog("[SYSTEM] Mineur prêt pour l'adresse: " + userEmail);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvStatusBadge = findViewById(R.id.tv_status_badge);
        tvHashrateValue = findViewById(R.id.tv_hashrate_value);
        tvMinedReward = findViewById(R.id.tv_mined_reward);
        tvBlocksFound = findViewById(R.id.tv_blocks_found);
        tvTotalNonces = findViewById(R.id.tv_total_nonces);
        btnToggleMining = findViewById(R.id.btn_toggle_mining);
        rgThreads = findViewById(R.id.rg_threads);
        rbThread1 = findViewById(R.id.rb_thread_1);
        rbThread2 = findViewById(R.id.rb_thread_2);
        rbThread4 = findViewById(R.id.rb_thread_4);
        switchAutoContinue = findViewById(R.id.switch_auto_continue);
        tvChallengeInfo = findViewById(R.id.tv_challenge_info);
        tvConsoleLogs = findViewById(R.id.tv_console_logs);
        scrollLogs = findViewById(R.id.scroll_logs);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnToggleMining.setOnClickListener(v -> {
            if (miningManager.isMining()) {
                miningManager.stopMining();
            } else {
                miningManager.startMining();
            }
        });

        rgThreads.setOnCheckedChangeListener((group, checkedId) -> {
            int threads = 2;
            if (checkedId == R.id.rb_thread_1) {
                threads = 1;
            } else if (checkedId == R.id.rb_thread_4) {
                threads = 4;
            }
            miningManager.setThreadCount(threads);
            appendLog("[CONFIG] Puissance CPU modifiée: " + threads + " Thread(s)");
        });

        switchAutoContinue.setOnCheckedChangeListener((buttonView, isChecked) -> {
            miningManager.setAutoContinue(isChecked);
            appendLog("[CONFIG] Minage automatique: " + (isChecked ? "ACTIF" : "INACTIF"));
        });
    }

    @Override
    public void onStatusChanged(String status) {
        runOnUiThread(() -> {
            if (status != null && status.startsWith("COOLDOWN")) {
                tvStatusBadge.setText("⏳ ATTENTE COOLDOWN");
                tvStatusBadge.setBackgroundColor(Color.parseColor("#FF9800"));
                btnToggleMining.setText("⏹️ ARRETER LE MINAGE");
                tvHashrateValue.setText("0.0 H/s");
                return;
            }
            switch (status != null ? status : "") {
                case "FETCHING_CHALLENGE":
                    tvStatusBadge.setText("CHARGEMENT...");
                    tvStatusBadge.setBackgroundColor(Color.parseColor("#C3891C"));
                    btnToggleMining.setText("⏹️ ARRETER LE MINAGE");
                    break;
                case "MINING_IN_PROGRESS":
                    tvStatusBadge.setText("● MINAGE EN COURS");
                    tvStatusBadge.setBackgroundColor(Color.parseColor("#4CAF50"));
                    btnToggleMining.setText("⏹️ ARRETER LE MINAGE");
                    break;
                case "SUBMITTING_PROOF":
                    tvStatusBadge.setText("ENVOI DE LA PREUVE...");
                    tvStatusBadge.setBackgroundColor(Color.parseColor("#FFD700"));
                    break;
                case "STOPPED":
                default:
                    tvStatusBadge.setText("STOPPE");
                    tvStatusBadge.setBackgroundColor(Color.parseColor("#444444"));
                    btnToggleMining.setText("🚀 DEMARRER LE MINAGE");
                    tvHashrateValue.setText("0.0 H/s");
                    break;
            }
        });
    }

    @Override
    public void onChallengeLoaded(MiningManager.Challenge challenge) {
        runOnUiThread(() -> {
            if (challenge.challengeId == 0) {
                String msg = challenge.message != null && !challenge.message.isEmpty()
                        ? challenge.message
                        : "Merci de patienter avant de demander un nouveau challenge.";
                String info = "Email Mineur: " + userEmail + "\n" +
                        "Statut: ⏳ COOLDOWN SERVEUR ATTEINT\n" +
                        "Message: " + msg + "\n" +
                        "Attente restante: " + challenge.expiresInSeconds + "s";
                tvChallengeInfo.setText(info);
            } else {
                String info = "Email Mineur: " + userEmail + "\n" +
                        "Challenge ID: #" + challenge.challengeId + "\n" +
                        "Mémoire Argon2id: " + (challenge.memoryCostKb / 1024) + " MB (" + challenge.memoryCostKb + " KB)\n" +
                        "Passes (TimeCost): " + challenge.timeCost + " | Difficulté: " + challenge.difficultyBits + " bits\n" +
                        "Expiration: " + challenge.expiresInSeconds + "s";
                tvChallengeInfo.setText(info);
            }
        });
    }

    @Override
    public void onHashRateUpdate(double hashesPerSecond, long totalNonces) {
        runOnUiThread(() -> {
            tvHashrateValue.setText(String.format(Locale.US, "%.1f H/s", hashesPerSecond));
            tvTotalNonces.setText(String.format(Locale.US, "%,d", totalNonces));
        });
    }

    @Override
    public void onBlockMined(long nonce, String hashHex, double reward, String txHash, double newBalance) {
        runOnUiThread(() -> {
            tvBlocksFound.setText(String.valueOf(miningManager.getBlocksMinedCount()));
            tvMinedReward.setText(String.format(Locale.US, "+%.4f PRO", miningManager.getSessionEarnedReward()));
            Toast.makeText(this, "🎉 Bloc miné! Récompense: +" + reward + " PROLO", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Erreur minage: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onLog(String logMessage) {
        runOnUiThread(() -> appendLog(logMessage));
    }

    private void appendLog(String message) {
        String timestamp = timeFormat.format(new Date());
        tvConsoleLogs.append("[" + timestamp + "] " + message + "\n");
        scrollLogs.post(() -> scrollLogs.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (miningManager != null) {
            miningManager.stopMining();
        }
    }
}