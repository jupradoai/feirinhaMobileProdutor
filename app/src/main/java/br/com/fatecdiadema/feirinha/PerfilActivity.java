package br.com.fatecdiadema.feirinha;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PerfilActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    public static String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        mAuth = FirebaseAuth.getInstance();

        // Obter dados do usuário
        FirebaseUser user = mAuth.getCurrentUser();
        TextView emailTextView = findViewById(R.id.emailTextView);
        LinearLayout logoutButton = findViewById(R.id.logoutButton);
        Button editPasswordButton = findViewById(R.id.editPasswordButton);
        Button deleteButton = findViewById(R.id.deleteButton);

        if (user != null) {
            userEmail = user.getEmail();
            storeEmail(userEmail); // Armazenando o email
            Log.d("ProfileActivity", "User email: " + userEmail);
            emailTextView.setText(userEmail != null ? userEmail : "Email não disponível");
        } else {
            // Redirecionar para a LoginActivity se o usuário não estiver autenticado
            Log.e("ProfileActivity", "User not authenticated");
            Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // Lógica de Logout
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Lógica de Editar Senha
        editPasswordButton.setOnClickListener(v -> showEditPasswordDialog());

        // Lógica de Excluir Conta
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmar Exclusão")
                    .setMessage("Tem certeza que deseja excluir sua conta?")
                    .setPositiveButton("Sim", (dialog, which) -> deleteUser())
                    .setNegativeButton("Não", null)
                    .show();
        });

        // Configurar padding para sistema de barras
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void storeEmail(String userEmail) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userEmail", userEmail);
        editor.apply(); // Salva as alterações
    }

    private void showEditPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Senha");
        final EditText input = new EditText(this);
        input.setHint("Nova senha");
        builder.setView(input);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String newPassword = input.getText().toString();
            updatePassword(newPassword);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updatePassword(String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(PerfilActivity.this, "Senha atualizada com sucesso", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PerfilActivity.this, "Erro ao atualizar a senha", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void deleteUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(PerfilActivity.this, "Erro ao excluir a conta", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
