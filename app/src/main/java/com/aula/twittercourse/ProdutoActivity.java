package com.aula.twittercourse;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aula.twittercourse.model.Produto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;



public class ProdutoActivity extends AppCompatActivity {

    private String email;
    private static final String TAG = "ProdutoActivity";
    private List<Produto> productList;
    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private EditText editTextProductName, editTextProductDescription, editTextProductPrice, editTextProductQuantity;
    private Button buttonAddProduct;
    private static final String CHANNEL_ID = "ProductChannel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_produto);

        // Initialize UI elements
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductDescription = findViewById(R.id.editTextProductDescription);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        editTextProductQuantity = findViewById(R.id.editTextProductQuantity);
        buttonAddProduct = findViewById(R.id.buttonAddProduct);

        // Initialize the list and RecyclerView
        productList = new ArrayList<>();
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        productAdapter = new ProductAdapter(productList);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProducts.setAdapter(productAdapter);

        email = getEmailFromPreferences();
        Log.d(TAG, "Email retrieved: " + email);

        if (email != null) {
            Log.d(TAG, "Fetching data for email: " + email);
            fetchData(BuildConfig.API_URL + "/products_by_email?email=" + email);
        } else {
            Log.e(TAG, "Email not provided");
            Toast.makeText(this, "Email não disponível", Toast.LENGTH_SHORT).show();
        }

        buttonAddProduct.setOnClickListener(v -> addProduct());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createNotificationChannel(); // Criação do canal de notificações
    }

    private String getEmailFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String email = sharedPreferences.getString("userEmail", null);
        Log.d(TAG, "Email from SharedPreferences: " + email);
        return email;
    }

    private void fetchData(String urlString) {
        Log.d(TAG, "Starting network request to: " + urlString);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String response = "";
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setInstanceFollowRedirects(false); // Disable automatic redirection

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                    response = sb.toString();
                    Log.d(TAG, "Response received: " + response);
                } else {
                    Log.e(TAG, "Error response from server: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching data", e);
            }

            // Update the UI after the API call
            String finalResponse = response;
            runOnUiThread(() -> {
                if (finalResponse != null && !finalResponse.isEmpty()) {
                    parseProducts(finalResponse);
                } else {
                    Toast.makeText(ProdutoActivity.this, "Sem dados retornados da API", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void addProduct() {
        String name = editTextProductName.getText().toString().trim();
        String description = editTextProductDescription.getText().toString().trim();
        String priceStr = editTextProductPrice.getText().toString().trim();
        String quantityStr = editTextProductQuantity.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        int quantity = Integer.parseInt(quantityStr);
        String id = String.valueOf(System.currentTimeMillis()); // Simple ID generation

        Produto newProduct = new Produto(id, name, description, price, quantity);
        productList.add(newProduct);
        productAdapter.notifyItemInserted(productList.size() - 1);

        Log.d(TAG, "Produto adicionado: " + newProduct.toString());

        // Clear input fields after adding
        editTextProductName.setText("");
        editTextProductDescription.setText("");
        editTextProductPrice.setText("");
        editTextProductQuantity.setText("");

        Toast.makeText(this, "Produto adicionado!", Toast.LENGTH_SHORT).show();

        // Send product to server
        sendProductToServer(newProduct);

        // Enviar notificação
        sendNotification("Produto Adicionado", "O produto '" + newProduct.getName() + "' foi adicionado com sucesso!");
    }

    private void sendProductToServer(Produto product) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String urlString = BuildConfig.API_URL + "/add_product";
            Log.d(TAG, "Sending product to server: " + product.toString());

            try {
                JSONObject jsonRequest = new JSONObject();
                JSONObject jsonInfo = new JSONObject();
                jsonInfo.put("name", product.getName());
                jsonInfo.put("description", product.getDescription());
                jsonInfo.put("quantity", product.getQuantity());
                jsonInfo.put("price", product.getPrice());

                jsonRequest.put("info", jsonInfo);
                jsonRequest.put("email", email); // Add the email

                Log.d(TAG, "JSON to send: " + jsonRequest.toString());

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("Content-Type", "application/json");

                // Send the JSON data
                conn.getOutputStream().write(jsonRequest.toString().getBytes("UTF-8"));
                conn.getOutputStream().flush();
                conn.getOutputStream().close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response Code from add_product: " + responseCode);

                // Read the server response
                StringBuilder response = new StringBuilder();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    Log.d(TAG, "Response from server: " + response.toString());
                } else {
                    Log.e(TAG, "Failed to add product. Response Code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending product to server", e);
            }
        });
    }

    private void parseProducts(String json) {
        try {
            JSONArray productsArray = new JSONArray(json);
            Log.d(TAG, "Parsing products... Total products: " + productsArray.length());
            productList.clear(); // Clear the list before adding new items
            for (int i = 0; i < productsArray.length(); i++) {
                JSONObject product = productsArray.getJSONObject(i);
                String id = product.getString("id");
                String name = product.getJSONObject("info").getString("name");
                String description = product.getJSONObject("info").getString("description");
                double price = product.getJSONObject("info").getDouble("price");
                int quantity = product.getJSONObject("info").getInt("quantity");

                productList.add(new Produto(id, name, description, price, quantity));
                Log.d(TAG, "Produto added: " + name);
            }
            productAdapter.notifyDataSetChanged(); // Notify the adapter about data change
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON", e);
        }
    }

    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Troque pelo seu ícone
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Intent to open the app when the notification is clicked
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_IMMUTABLE);

        builder.setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Product Notifications";
            String description = "Channel for product addition notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
