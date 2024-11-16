package com.aula.twittercourse;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends MenuActivity {

    private static final String TAG = "HomeActivity";
    private String email;
    private LinearLayout chartContainer;
    private LinearLayout barChartContainer;
    private TextView totalStockValueTextView;
    private TextView productCountTextView;
    private ExecutorService executor;

    // Adicionando a variável de ambiente
    private String apiUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Obter a URL da API da BuildConfig
        apiUrl = BuildConfig.API_URL;

        initViews();
        email = getEmailFromPreferences();
        Log.d(TAG, "Email retrieved: " + email);

        if (email != null) {
            fetchData("products", "/products_by_email?email=" + email);
            fetchData("progress", "/dashboard/progress?email=" + email);
        } else {
            showToast("Email não disponível");
        }
    }

    private void initViews() {
        chartContainer = findViewById(R.id.chartContainer);
        barChartContainer = findViewById(R.id.barChartContainer);
        totalStockValueTextView = findViewById(R.id.totalStockValue);
        productCountTextView = findViewById(R.id.productCount);
        executor = Executors.newSingleThreadExecutor();
    }

    private String getEmailFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("userEmail", null);
    }

    private void fetchData(String type, String endpoint) {
        String urlString = apiUrl + endpoint; // Usando a variável de ambiente
        Log.d(TAG, "Fetching " + type + " data from: " + urlString);
        executor.execute(() -> {
            try {
                String response = performNetworkRequest(urlString);
                Log.d(TAG, "Response from " + type + " API: " + response);
                runOnUiThread(() -> {
                    if (response != null && !response.isEmpty()) {
                        if (type.equals("products")) {
                            parseProductData(response);
                        } else if (type.equals("progress")) {
                            parseProgressData(response);
                        }
                    } else {
                        showToast("Sem dados retornados da API de " + type);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error fetching " + type + " data", e);
                runOnUiThread(() -> showToast("Erro ao buscar dados de " + type));
            }
        });
    }

    private String performNetworkRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } else {
            Log.e(TAG, "Error response from server: " + conn.getResponseCode());
            return null;
        }
    }

    private void parseProductData(String json) {
        try {
            JSONArray productsArray = new JSONArray(json);
            List<Entry> lineEntries = new ArrayList<>();
            List<BarEntry> barEntries = new ArrayList<>();
            List<String> productNames = new ArrayList<>();
            double totalValue = 0.0;
            int totalCount = 0;

            for (int i = 0; i < productsArray.length(); i++) {
                JSONObject product = productsArray.getJSONObject(i);
                if (!isValidProduct(product)) continue;

                JSONObject info = product.getJSONObject("info");
                double price = info.getDouble("price");
                int quantity = info.getInt("quantity");

                totalValue += price * quantity;
                totalCount += quantity;

                lineEntries.add(new Entry(i, quantity));
                barEntries.add(new BarEntry(i, quantity));
                productNames.add(info.getString("name"));
            }

            Log.d(TAG, "Line chart entries: " + lineEntries.toString());
            Log.d(TAG, "Bar chart entries: " + barEntries.toString());
            Log.d(TAG, "Product names: " + productNames.toString());

            setupLineChart(lineEntries, productNames);
            updateStockInfo(totalValue, totalCount);

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing product JSON", e);
        }
    }

    private void parseProgressData(String json) {
        try {
            JSONObject progressData = new JSONObject(json);
            List<BarEntry> barEntries = new ArrayList<>();
            List<String> phaseNames = new ArrayList<>();

            phaseNames.add("Cultivo");
            phaseNames.add("Distribuição");
            phaseNames.add("Processamento");
            phaseNames.add("Qualidade");

            barEntries.add(new BarEntry(0, progressData.getInt("cultivation")));
            barEntries.add(new BarEntry(1, progressData.getInt("distribution")));
            barEntries.add(new BarEntry(2, progressData.getInt("processing")));
            barEntries.add(new BarEntry(3, progressData.getInt("quality")));

            Log.d(TAG, "Progress bar entries: " + barEntries.toString());
            Log.d(TAG, "Phase names: " + phaseNames.toString());

            setupProgressBarChart(barEntries, phaseNames);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing progress JSON", e);
        }
    }

    private boolean isValidProduct(JSONObject product) {
        try {
            JSONObject info = product.getJSONObject("info");
            return info.has("name") && info.has("price") && info.has("quantity");
        } catch (JSONException e) {
            return false;
        }
    }

    private void setupLineChart(List<Entry> entries, List<String> productNames) {
        Log.d(TAG, "Setting up line chart with entries: " + entries.toString());
        LineChart lineChart = new LineChart(this);
        LineDataSet dataSet = new LineDataSet(entries, "Quantidade de Produtos");
        dataSet.setColor(Color.rgb(255, 165, 0));
        dataSet.setCircleColor(Color.RED);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.animateY(500);
        lineChart.getXAxis().setEnabled(false);

        CustomMarkerView markerView = new CustomMarkerView(this, R.layout.marker_view, productNames);
        lineChart.setMarker(markerView);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.MATCH_PARENT);
        lineChart.setLayoutParams(params);
        chartContainer.addView(lineChart);
    }



    private void setupProgressBarChart(List<BarEntry> entries, List<String> phaseNames) {
        Log.d(TAG, "Setting up progress bar chart with entries: " + entries.toString());
        BarChart barChart = new BarChart(this);
        BarDataSet barDataSet = new BarDataSet(entries, "Progresso das Fases");
        barDataSet.setColor(Color.rgb(255, 165, 0));
        BarData barData = new BarData(barDataSet);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(500);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(phaseNames));
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);

        // Adicionando o MarkerView ao gráfico de barras
        CustomMarkerView markerView = new CustomMarkerView(this, R.layout.marker_view, phaseNames);
        barChart.setMarker(markerView);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300);
        barChart.setLayoutParams(params);
        barChartContainer.addView(barChart);
    }


    private void updateStockInfo(double totalValue, int totalCount) {
        totalStockValueTextView.setText("Valor em Estoque: R$ " + String.format("%.2f", totalValue));
        productCountTextView.setText("Quantidade em Estoque: " + totalCount);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private class CustomMarkerView extends MarkerView {
        private final List<String> productNames;
        private final TextView tvContent;

        public CustomMarkerView(Context context, int layoutResource, List<String> productNames) {
            super(context, layoutResource);
            this.productNames = productNames;
            tvContent = findViewById(R.id.markerText);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            int index = (int) e.getX();
            if (index >= 0 && index < productNames.size()) {
                tvContent.setText(productNames.get(index));
            } else {
                tvContent.setText("");
            }
            super.refreshContent(e, highlight);
        }

        @Override
        public void draw(Canvas canvas, float posX, float posY) {
            // Ajustar a posição do MarkerView para garantir que ele esteja visível
            float minY = 50; // Defina a altura mínima desejada
            posY = Math.max(posY - getHeight(), minY); // Move para cima, mas não abaixo do limite mínimo

            // Mover o MarkerView um pouco para a esquerda
            float offsetX = 30; // Ajuste este valor conforme necessário
            posX -= offsetX; // Move para a esquerda

            super.draw(canvas, posX, posY);
        }
    }


    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}