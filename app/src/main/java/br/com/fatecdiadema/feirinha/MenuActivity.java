package br.com.fatecdiadema.feirinha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        // Não é mais necessário chamar setupNavigation aqui, pois usamos onClick no XML
    }

    public void nav_home(View view) {
        startActivity(new Intent(this, HomeActivity.class));
    }

    public void nav_profile(View view) {
        startActivity(new Intent(this, PerfilActivity.class));
    }

    public void nav_service(View view) {
        startActivity(new Intent(this, ServiceActivity.class));
    }
}
