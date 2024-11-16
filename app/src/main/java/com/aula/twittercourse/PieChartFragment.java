package com.aula.twittercourse;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PieChartFragment extends Fragment {

    private static final String TAG = "PieChartFragment";
    private PieChart pieChart;
    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_pie_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pieChart = view.findViewById(R.id.pieChart);

        // Recuperar o email das SharedPreferences
        email = getEmailFromPreferences();

        if (email != null) {
            Log.d(TAG, "Fetching data for email: " + email);
            fetchData("https://jupradoai.pythonanywhere.com/dashboard/progress?email=" + email);
        } else {
            Log.e(TAG, "Email not provided");
            Toast.makeText(getContext(), "Email não disponível", Toast.LENGTH_SHORT).show();
        }
    }

    private String getEmailFromPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", getContext().MODE_PRIVATE);
        return sharedPreferences.getString("userEmail", null); // Retorna null se não encontrar
    }

    private void updateChart(List<PieEntry> entries) {
        // Atualiza o gráfico na thread principal
        getActivity().runOnUiThread(() -> {
            if (entries.isEmpty()) {
                Toast.makeText(pieChart.getContext(), "Sem dados para mostrar", Toast.LENGTH_SHORT).show();
            } else {
                PieDataSet dataSet = new PieDataSet(entries, "Categorias");

                // Definir as cores personalizadas
                int[] colors = {
                        getResources().getColor(R.color.colorCultivo, null),
                        getResources().getColor(R.color.colorDistribuicao, null),
                        getResources().getColor(R.color.colorProcessamento, null),
                        getResources().getColor(R.color.colorQualidade, null)
                };
                dataSet.setColors(colors);

                PieData pieData = new PieData(dataSet);
                pieChart.setData(pieData);
                pieChart.invalidate(); // Atualiza o gráfico
            }
        });
    }

    private void fetchData(String urlString) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<PieEntry> entries = new ArrayList<>();
            try {
                URL url = new URL(urlString);
                Log.d(TAG, "Connecting to URL: " + url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(true); // Habilitar seguimento de redirecionamentos

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    Log.d(TAG, "Response: " + response.toString());

                    // Processar a resposta JSON como um JSONObject
                    JSONObject jsonObject = new JSONObject(response.toString());

                    // Adicionar entradas ao gráfico a partir do JSONObject
                    if (jsonObject.has("cultivation")) {
                        entries.add(new PieEntry(jsonObject.getInt("cultivation"), "Cultivo"));
                    }
                    if (jsonObject.has("distribution")) {
                        entries.add(new PieEntry(jsonObject.getInt("distribution"), "Distribuição"));
                    }
                    if (jsonObject.has("processing")) {
                        entries.add(new PieEntry(jsonObject.getInt("processing"), "Processamento"));
                    }
                    if (jsonObject.has("quality")) {
                        entries.add(new PieEntry(jsonObject.getInt("quality"), "Qualidade"));
                    }

                    Log.d(TAG, "Entries added: " + entries.toString());
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                    String redirectUrl = connection.getHeaderField("Location");
                    Log.d(TAG, "Redirected to: " + redirectUrl);
                    // Aqui você pode fazer uma nova requisição para a URL redirecionada, se necessário
                } else {
                    Log.e(TAG, "Failed to fetch data: " + responseCode);
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error fetching data", e);
            } finally {
                updateChart(entries);
            }
        });
    }



}
