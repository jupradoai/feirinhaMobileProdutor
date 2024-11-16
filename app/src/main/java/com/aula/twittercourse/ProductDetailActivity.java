package com.aula.twittercourse;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView textViewName;
    private TextView textViewDescription;
    private TextView textViewPrice;
    private TextView textViewQuantity;
    private TextView textViewId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        textViewId = findViewById(R.id.textViewId);
        textViewName = findViewById(R.id.textViewName);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewQuantity = findViewById(R.id.textViewQuantity);

        // Recebe os dados passados pela intent
        String id = getIntent().getStringExtra("product_id");
        String name = getIntent().getStringExtra("product_name");
        String description = getIntent().getStringExtra("product_description");
        double price = getIntent().getDoubleExtra("product_price", 0);
        int quantity = getIntent().getIntExtra("product_quantity", 0);

        // Exibe os dados nos TextViews

        textViewName.setText(name);
        textViewId.setText("Códido do Produto: " + id);
        textViewDescription.setText(description);
        textViewPrice.setText("R$ " + price);
        textViewQuantity.setText("Quantidade: " + quantity);
    }
}