package com.aula.twittercourse;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlertActivity extends MenuActivity {

    private static final String API_KEY = "0c774ec745964e25afd223137242310";
    private static final String BASE_URL_CURRENT = "https://api.weatherapi.com/v1/current.json?key=";
    private static final String BASE_URL_FORECAST = "https://api.weatherapi.com/v1/forecast.json?key=";

    private TextView weatherInfo;
    private ImageView weatherIcon;
    private Switch switchRealTimeAlert;
    private FusedLocationProviderClient fusedLocationClient;
    private ExecutorService executorService;
    private LinearLayout forecastContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        weatherInfo = findViewById(R.id.weatherInfo);
        weatherIcon = findViewById(R.id.weatherIcon);
        forecastContainer = findViewById(R.id.forecastContainer);
        switchRealTimeAlert = findViewById(R.id.switchRealTimeAlert);

        // Carregar o estado salvo do Switch
        SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        boolean isSwitchOn = sharedPreferences.getBoolean("switch_state", false);
        switchRealTimeAlert.setChecked(isSwitchOn);

        // Salvar o estado do Switch ao ser alterado
        switchRealTimeAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("switch_state", isChecked);
            editor.apply();
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        executorService = Executors.newSingleThreadExecutor();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        String userLocation = location.getLatitude() + "," + location.getLongitude();
                        fetchWeatherData(userLocation);
                        fetchForecastData(userLocation); // Puxa previsões automaticamente
                    } else {
                        weatherInfo.setText("Não foi possível obter a localização.");
                    }
                });
    }

    private void fetchWeatherData(String location) {
        executorService.execute(() -> {
            String result = null;
            try {
                URL url = new URL(BASE_URL_CURRENT + API_KEY + "&q=" + location + "&lang=pt");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    result = stringBuilder.toString();
                    reader.close();
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String finalResult = result;
            runOnUiThread(() -> {
                if (finalResult != null) {
                    parseWeatherData(finalResult);
                } else {
                    weatherInfo.setText("Erro ao carregar dados do clima.");
                }
            });
        });
    }

    private void fetchForecastData(String location) {
        executorService.execute(() -> {
            String result = null;
            try {
                URL url = new URL(BASE_URL_FORECAST + API_KEY + "&q=" + location + "&days=3&lang=pt");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    result = stringBuilder.toString();
                    reader.close();
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String finalResult = result;
            runOnUiThread(() -> {
                if (finalResult != null) {
                    parseForecastData(finalResult);
                } else {
                    weatherInfo.setText("Erro ao carregar previsões.");
                }
            });
        });
    }

    private void parseWeatherData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            String location = jsonObject.getJSONObject("location").getString("name");
            double temperature = jsonObject.getJSONObject("current").getDouble("temp_c");
            String condition = jsonObject.getJSONObject("current").getJSONObject("condition").getString("text");
            String iconUrl = jsonObject.getJSONObject("current").getJSONObject("condition").getString("icon");

            String weatherDetails = "Local: " + location + "\n" +
                    "Temperatura: " + temperature + "°C\n" +
                    "Condição: " + condition;
            weatherInfo.setText(weatherDetails);
            updateWeatherIcon(iconUrl);
        } catch (Exception e) {
            e.printStackTrace();
            weatherInfo.setText("Erro ao processar dados do clima.");
        }
    }

    private void parseForecastData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray forecastArray = jsonObject.getJSONObject("forecast").getJSONArray("forecastday");
            forecastContainer.removeAllViews(); // Limpa previsões anteriores

            for (int i = 0; i < forecastArray.length(); i++) {
                JSONObject day = forecastArray.getJSONObject(i);
                String date = day.getString("date");
                double maxTemp = day.getJSONObject("day").getDouble("maxtemp_c");
                double minTemp = day.getJSONObject("day").getDouble("mintemp_c");
                String condition = day.getJSONObject("day").getJSONObject("condition").getString("text");
                String iconUrl = day.getJSONObject("day").getJSONObject("condition").getString("icon");

                // Cria um novo layout para a previsão do dia
                LinearLayout forecastItem = new LinearLayout(this);
                forecastItem.setOrientation(LinearLayout.HORIZONTAL);
                forecastItem.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                // Adiciona o ícone
                ImageView forecastIcon = new ImageView(this);
                forecastIcon.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.2f));
                Glide.with(this).load("https:" + iconUrl).into(forecastIcon);
                forecastItem.addView(forecastIcon);

                // Adiciona detalhes da previsão
                TextView forecastDetails = new TextView(this);
                String details = "Data: " + date + "\nMax: " + maxTemp + "°C\nMin: " + minTemp + "°C\nCondição: " + condition;
                forecastDetails.setText(details);
                forecastDetails.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f));
                forecastItem.addView(forecastDetails);

                // Adiciona o item de previsão ao contêiner
                forecastContainer.addView(forecastItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            weatherInfo.setText("Erro ao processar previsões.");
        }
    }

    private void updateWeatherIcon(String iconUrl) {
        Glide.with(this)
                .load("https:" + iconUrl)
                .into(weatherIcon);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                weatherInfo.setText("Permissão de localização negada.");
            }
        }
    }

    private void checkConditions(double temperature, double rain, double humidity, String weatherCondition) {
        if (rain > 50 && weatherCondition.contains("tempestade")) {
            sendNotification("Alerta de Tempestade", "Tempestades severas previstas!");
        }

        if (rain < 5 && (temperature >= 20 && temperature <= 30) && humidity < 80) {
            sendNotification("Condições Ideais", "Condições ideais para colheita!");
        }

        if (rain >= 5 && rain <= 15 && (temperature >= 15 && temperature <= 25) && humidity >= 20 && humidity <= 50) {
            sendNotification("Condições Ideais", "Condições ideais para plantio!");
        }

        if (rain > 0) {
            sendNotification("Alerta de Chuva", "Chuva detectada!");
        }

        if (humidity < 30) {
            sendNotification("Alerta de Umidade", "Umidade baixa detectada!");
        }
    }

    private void sendNotification(String title, String message) {
        String channelId = "weather_alerts";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Alertas de Clima", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification) // Defina seu ícone de notificação
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
