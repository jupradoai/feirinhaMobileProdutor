package com.aula.twittercourse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ServiceActivity extends MenuActivity {

    private Button myProducts;
    private Button supplyChain;
    private Button findProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_service);

        myProducts = findViewById(R.id.myProducts);
        supplyChain = findViewById(R.id.supplyChain);
        findProducts = findViewById(R.id.findProducts);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Método para o botão "Gerenciamento de Produtos"
    public void produtos(View view) {
        Intent intent = new Intent(ServiceActivity.this, ProdutoActivity.class);
        startActivity(intent);
    }

    // Método para o botão "Gerenciar Supply Chain"
    public void chain(View view) {
        Intent intent = new Intent(ServiceActivity.this, SupplyChainActivity.class);
        startActivity(intent);
    }

    // Método para o botão "Rastreamento de Produto"
    public void rastrear(View view) {
        Intent intent = new Intent(ServiceActivity.this, RastrearActivity.class);
        startActivity(intent);
    }
}
