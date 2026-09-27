package com.example.proloblockchain;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MiningManager {

    private static final String TAG = "MiningManager";
    private static final String BASE_URL = "http://82.230.48.228:32769/api/v1/mining";

    private final Context context;
    private final RequestQueue requestQueue;
    private final Handler mainHandler;

    private ExecutorService miningThreadPool;
    private final AtomicBoolean isMining = new AtomicBoolean(false);
    private final AtomicLong totalNoncesChecked = new AtomicLong(0);

    private String userEmail;
    private Challenge currentChallenge;
    private MiningListener listener;

    private int threadCount = 2;
    private boolean autoContinue = true;
    private double sessionEarnedReward = 0.0;
    private int blocksMinedCount = 0;

    public interface MiningListener {
        void onStatusChanged(String status);
        void onChallengeLoaded(Challenge challenge);
        void onHashRateUpdate(double hashesPerSecond, long totalNonces);
        void onBlockMined(long nonce, String hashHex, double reward, String txHash, double newBalance);
        void onError(String error);
        void onLog(String logMessage);
    }

    public static class Challenge {
        public long challengeId;
        public String saltBase64;
        public byte[] saltBytes;
        public int memoryCostKb;
        public int timeCost;
        public int difficultyBits;
        public int expiresInSeconds;
        public String message;
    }

    public MiningManager(Context context, String email) {
        this.context = context.getApplicationContext();
        this.userEmail = email;
        this.requestQueue = Volley.newRequestQueue(this.context);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setListener(MiningListener listener) {
        this.listener = listener;
    }

    public void setThreadCount(int threads) {
        this.threadCount = Math.max(1, Math.min(threads, 8));
    }

    public void setAutoContinue(boolean autoContinue) {
        this.autoContinue = autoContinue;
    }

    public boolean isMining() {
        return isMining.get();
    }

    public double getSessionEarnedReward() {
        return sessionEarnedReward;
    }

    public int getBlocksMinedCount() {
        return blocksMinedCount;
    }

    public long getTotalNoncesChecked() {
        return totalNoncesChecked.get();
    }

    public void startMining() {
        if (isMining.get()) {
            log("Mining is already in progress.");
            return;
        }

        if (userEmail == null || userEmail.trim().isEmpty()) {
            notifyError("User email is missing for mining.");
            return;
        }

        isMining.set(true);
        totalNoncesChecked.set(0);
        updateStatus("FETCHING_CHALLENGE");
        log("🚀 Requesting new Argon2id mining challenge for " + userEmail + "...");

        fetchChallenge();
    }

    public void stopMining() {
        if (!isMining.get()) return;

        isMining.set(false);
        if (miningThreadPool != null && !miningThreadPool.isShutdown()) {
            miningThreadPool.shutdownNow();
        }
        updateStatus("STOPPED");
        log("⏹️ Mining stopped by user.");
    }

    private void fetchChallenge() {
        if (!isMining.get()) return;

        String url = BASE_URL + "/challenge/" + userEmail;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        log("📥 Challenge raw response: " + response.toString());

                        Challenge challenge = new Challenge();
                        challenge.challengeId = parseLong(response, "challengeId", "challenge_id", "id");
                        challenge.saltBase64 = parseString(response, "saltBase64", "salt_base64", "salt");
                        challenge.memoryCostKb = parseInt(response, 0, "memoryCostKb", "memory_cost_kb", "memory_cost", "memoryCost");
                        challenge.timeCost = parseInt(response, 0, "timeCost", "time_cost", "iterations", "time_cost_iterations");
                        challenge.difficultyBits = parseInt(response, 0, "difficultyBits", "difficulty_bits", "difficulty");
                        challenge.expiresInSeconds = parseInt(response, 0, "expiresInSeconds", "expires_in_seconds", "ttl");
                        challenge.message = parseString(response, "message", "msg");

                        // VERIFICATION SI LE SERVEUR EST EN COOLDOWN / RATE LIMIT
                        if (challenge.challengeId == 0 || challenge.saltBase64 == null || challenge.saltBase64.isEmpty()
                                || "null".equalsIgnoreCase(challenge.saltBase64) || challenge.memoryCostKb == 0) {

                            int waitSec = challenge.expiresInSeconds > 0 ? challenge.expiresInSeconds : 30;
                            String msg = !challenge.message.isEmpty() ? challenge.message : "Merci de patienter avant de demander un nouveau challenge.";

                            log("⏳ Cooldown serveur (" + waitSec + "s) : " + msg);
                            updateStatus("COOLDOWN_" + waitSec);

                            mainHandler.post(() -> {
                                if (listener != null) {
                                    listener.onChallengeLoaded(challenge);
                                }
                            });

                            if (autoContinue && isMining.get()) {
                                log("⏱️ Redemande automatique d'un challenge dans " + (waitSec + 2) + " secondes...");
                                mainHandler.postDelayed(this::fetchChallenge, (waitSec + 2) * 1000L);
                            } else {
                                stopMining();
                            }
                            return;
                        }

                        try {
                            challenge.saltBytes = Base64.decode(challenge.saltBase64, Base64.DEFAULT);
                        } catch (Exception e) {
                            challenge.saltBytes = challenge.saltBase64.getBytes(StandardCharsets.UTF_8);
                        }

                        this.currentChallenge = challenge;

                        mainHandler.post(() -> {
                            if (listener != null) {
                                listener.onChallengeLoaded(challenge);
                            }
                        });

                        log("✅ Challenge #" + challenge.challengeId + " received! Memory: "
                                + (challenge.memoryCostKb / 1024) + "MB | TimeCost: " + challenge.timeCost
                                + " | DiffBits: " + challenge.difficultyBits);

                        startPoWLoop(challenge);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing challenge JSON", e);
                        notifyError("Challenge parsing error: " + e.getMessage());
                        stopMining();
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching challenge", error);
                    String errMsg = "Failed to fetch challenge";
                    if (error.networkResponse != null) {
                        errMsg += " (Code " + error.networkResponse.statusCode + ")";
                    }
                    notifyError(errMsg);
                    stopMining();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Connection", "close");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(12000, 2, 1.0f));
        requestQueue.add(request);
    }

    private void startPoWLoop(Challenge challenge) {
        if (!isMining.get()) return;

        updateStatus("MINING_IN_PROGRESS");
        log("⚡ Mining Argon2id block with " + threadCount + " thread(s)...");

        miningThreadPool = Executors.newFixedThreadPool(threadCount);

        long startTime = System.currentTimeMillis();
        AtomicBoolean blockFound = new AtomicBoolean(false);

        // Timer for Hashrate updates
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isMining.get() || blockFound.get()) return;

                long elapsedMs = System.currentTimeMillis() - startTime;
                if (elapsedMs > 0) {
                    long totalNonces = totalNoncesChecked.get();
                    double hps = (totalNonces * 1000.0) / elapsedMs;
                    if (listener != null) {
                        listener.onHashRateUpdate(hps, totalNonces);
                    }
                }
                mainHandler.postDelayed(this, 1000);
            }
        }, 1000);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            miningThreadPool.execute(() -> {
                long nonce = threadId;

                while (isMining.get() && !blockFound.get()) {
                    byte[] hash = computeArgon2id(challenge, nonce);
                    totalNoncesChecked.incrementAndGet();

                    if (checkDifficulty(hash, challenge.difficultyBits)) {
                        if (blockFound.compareAndSet(false, true)) {
                            String hashHex = bytesToHex(hash);
                            long foundNonce = nonce;
                            double durationSec = (System.currentTimeMillis() - startTime) / 1000.0;

                            log(String.format(Locale.US, "🎉 BLOCK FOUND! Nonce: %d | Hash: %s... (%.2fs)",
                                    foundNonce, hashHex.substring(0, Math.min(16, hashHex.length())), durationSec));

                            mainHandler.post(() -> submitProof(challenge, foundNonce, hashHex));
                        }
                        break;
                    }

                    nonce += threadCount;
                }
            });
        }
    }

    private byte[] computeArgon2id(Challenge challenge, long nonce) {
        try {
            int memoryKb = Math.max(8, challenge.memoryCostKb);
            int iterations = Math.max(1, challenge.timeCost);

            Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                    .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                    .withMemoryAsKB(memoryKb)
                    .withIterations(iterations)
                    .withParallelism(1)
                    .withSalt(challenge.saltBytes != null && challenge.saltBytes.length > 0
                            ? challenge.saltBytes
                            : "prolo_salt".getBytes(StandardCharsets.UTF_8));

            Argon2BytesGenerator generator = new Argon2BytesGenerator();
            generator.init(builder.build());

            String passwordString = challenge.challengeId + ":" + userEmail + ":" + nonce;
            byte[] password = passwordString.getBytes(StandardCharsets.UTF_8);

            byte[] result = new byte[32]; // 256 bits hash
            generator.generateBytes(password, result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Argon2id compute error", e);
            return null;
        }
    }

    public static boolean checkDifficulty(byte[] hash, int difficultyBits) {
        if (hash == null || hash.length == 0 || difficultyBits <= 0) return false;

        int fullBytes = difficultyBits / 8;
        int remainingBits = difficultyBits % 8;

        for (int i = 0; i < fullBytes && i < hash.length; i++) {
            if (hash[i] != 0) return false;
        }

        if (remainingBits > 0 && fullBytes < hash.length) {
            int mask = (0xFF << (8 - remainingBits)) & 0xFF;
            return (hash[fullBytes] & mask) == 0;
        }

        return true;
    }

    private void submitProof(Challenge challenge, long nonce, String hashHex) {
        updateStatus("SUBMITTING_PROOF");
        log("📡 Submitting proof to blockchain server...");

        String url = BASE_URL + "/submit";

        JSONObject body = new JSONObject();
        try {
            body.put("email", userEmail);
            body.put("challengeId", challenge.challengeId);
            body.put("nonce", nonce);
            body.put("hashHex", hashHex);
        } catch (JSONException e) {
            log("Error building JSON submit body");
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, body,
                response -> {
                    boolean accepted = response.optBoolean("accepted", false);
                    String reason = response.optString("reason", "Proof processed");
                    double rewardAmount = response.optDouble("rewardAmount", 0.0);
                    String txHash = response.optString("txHash", "N/A");
                    double newBalance = response.optDouble("newBalance", 0.0);

                    if (accepted) {
                        blocksMinedCount++;
                        sessionEarnedReward += rewardAmount;
                        log("💰 PROOF ACCEPTED! Reward: +" + rewardAmount + " PROLO | Tx: " + txHash);

                        mainHandler.post(() -> {
                            if (listener != null) {
                                listener.onBlockMined(nonce, hashHex, rewardAmount, txHash, newBalance);
                            }
                        });

                        if (autoContinue && isMining.get()) {
                            log("🔄 Auto-mining enabled: Requesting next challenge in 2s...");
                            mainHandler.postDelayed(this::fetchChallenge, 2000);
                        } else {
                            stopMining();
                        }
                    } else {
                        log("❌ Proof rejected by server: " + reason);
                        notifyError("Proof rejected: " + reason);
                        stopMining();
                    }
                },
                error -> {
                    Log.e(TAG, "Submit proof error", error);
                    notifyError("Failed to submit proof to server.");
                    stopMining();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Connection", "close");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(12000, 2, 1.0f));
        requestQueue.add(request);
    }

    private long parseLong(JSONObject json, String... keys) {
        for (String key : keys) {
            if (json.has(key) && !json.isNull(key)) {
                return json.optLong(key, 0);
            }
        }
        return 0;
    }

    private String parseString(JSONObject json, String... keys) {
        for (String key : keys) {
            if (json.has(key) && !json.isNull(key)) {
                return json.optString(key, "");
            }
        }
        return "";
    }

    private int parseInt(JSONObject json, int defaultValue, String... keys) {
        for (String key : keys) {
            if (json.has(key) && !json.isNull(key)) {
                return json.optInt(key, defaultValue);
            }
        }
        return defaultValue;
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void updateStatus(String status) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onStatusChanged(status);
            }
        });
    }

    private void notifyError(String error) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onError(error);
            }
        });
        log("⚠️ Error: " + error);
    }

    private void log(String message) {
        Log.d(TAG, message);
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onLog(message);
            }
        });
    }
}