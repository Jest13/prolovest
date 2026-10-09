package com.example.proloblockchain;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CryptoPriceManager {

    private static final String TAG = "CryptoPriceManager";
    private static final String BINANCE_API_URL = "https://api.binance.com/api/v3/ticker/24hr?symbols=[\"BTCUSDT\",\"ETHUSDT\",\"SOLUSDT\",\"DOGEUSDT\",\"XCHUSDT\"]";
    private static final String COINGECKO_API_URL = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,solana,dogecoin,chia&vs_currencies=usd&include_24hr_change=true";

    private final RequestQueue requestQueue;

    public CryptoPriceManager(Context context) {
        this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void fetchLiveCryptoPrices(
            TextView tvBtcPrice, TextView tvBtcChange,
            TextView tvEthPrice, TextView tvEthChange,
            TextView tvSolPrice, TextView tvSolChange,
            TextView tvDogePrice, TextView tvDogeChange,
            TextView tvXchPrice, TextView tvXchChange
    ) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, BINANCE_API_URL, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject ticker = response.getJSONObject(i);
                            String symbol = ticker.optString("symbol", "");
                            double lastPrice = ticker.optDouble("lastPrice", 0.0);
                            double priceChangePercent = ticker.optDouble("priceChangePercent", 0.0);

                            switch (symbol) {
                                case "BTCUSDT":
                                    updateCryptoUI(tvBtcPrice, tvBtcChange, lastPrice, priceChangePercent);
                                    break;
                                case "ETHUSDT":
                                    updateCryptoUI(tvEthPrice, tvEthChange, lastPrice, priceChangePercent);
                                    break;
                                case "SOLUSDT":
                                    updateCryptoUI(tvSolPrice, tvSolChange, lastPrice, priceChangePercent);
                                    break;
                                case "DOGEUSDT":
                                    updateCryptoUI(tvDogePrice, tvDogeChange, lastPrice, priceChangePercent);
                                    break;
                                case "XCHUSDT":
                                    updateCryptoUI(tvXchPrice, tvXchChange, lastPrice, priceChangePercent);
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing Binance prices", e);
                        fetchCoinGeckoFallback(tvBtcPrice, tvBtcChange, tvEthPrice, tvEthChange, tvSolPrice, tvSolChange, tvDogePrice, tvDogeChange, tvXchPrice, tvXchChange);
                    }
                },
                error -> {
                    Log.w(TAG, "Binance API failed, trying CoinGecko fallback: " + error.getMessage());
                    fetchCoinGeckoFallback(tvBtcPrice, tvBtcChange, tvEthPrice, tvEthChange, tvSolPrice, tvSolChange, tvDogePrice, tvDogeChange, tvXchPrice, tvXchChange);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "ProlovestApp/1.0");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 2, 1.0f));
        requestQueue.add(request);
    }

    private void fetchCoinGeckoFallback(
            TextView tvBtcPrice, TextView tvBtcChange,
            TextView tvEthPrice, TextView tvEthChange,
            TextView tvSolPrice, TextView tvSolChange,
            TextView tvDogePrice, TextView tvDogeChange,
            TextView tvXchPrice, TextView tvXchChange
    ) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, COINGECKO_API_URL, null,
                response -> {
                    try {
                        updateFromCoinGecko(response, "bitcoin", tvBtcPrice, tvBtcChange);
                        updateFromCoinGecko(response, "ethereum", tvEthPrice, tvEthChange);
                        updateFromCoinGecko(response, "solana", tvSolPrice, tvSolChange);
                        updateFromCoinGecko(response, "dogecoin", tvDogePrice, tvDogeChange);
                        updateFromCoinGecko(response, "chia", tvXchPrice, tvXchChange);
                    } catch (Exception e) {
                        Log.e(TAG, "CoinGecko parsing error", e);
                    }
                },
                error -> Log.e(TAG, "CoinGecko API error: " + error.getMessage())
        );

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 2, 1.0f));
        requestQueue.add(request);
    }

    private void updateFromCoinGecko(JSONObject json, String coinId, TextView tvPrice, TextView tvChange) {
        if (json.has(coinId)) {
            JSONObject coin = json.optJSONObject(coinId);
            if (coin != null) {
                double price = coin.optDouble("usd", 0.0);
                double change = coin.optDouble("usd_24h_change", 0.0);
                updateCryptoUI(tvPrice, tvChange, price, change);
            }
        }
    }

    private void updateCryptoUI(TextView tvPrice, TextView tvChange, double price, double changePercent) {
        if (tvPrice != null) {
            String formattedPrice;
            if (price >= 1000) {
                NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
                format.setMaximumFractionDigits(2);
                formattedPrice = format.format(price);
            } else if (price >= 1) {
                formattedPrice = String.format(Locale.US, "$%.2f", price);
            } else {
                formattedPrice = String.format(Locale.US, "$%.4f", price);
            }
            tvPrice.setText(formattedPrice);
        }

        if (tvChange != null) {
            String formattedChange = String.format(Locale.US, "%+.1f%%", changePercent);
            tvChange.setText(formattedChange);

            if (changePercent >= 0) {
                tvChange.setTextColor(Color.parseColor("#4CAF50"));
                tvChange.getBackground().setTint(Color.parseColor("#E8F5E9"));
            } else {
                tvChange.setTextColor(Color.parseColor("#F44336"));
                tvChange.getBackground().setTint(Color.parseColor("#FFEBEE"));
            }
        }
    }
}