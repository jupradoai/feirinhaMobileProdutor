package com.aula.twittercourse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText editUsuario;
    private EditText editEmail;
    private EditText editSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        editUsuario = findViewById(R.id.cadUsuario);
        editEmail = findViewById(R.id.cadEmail);
        editSenha = findViewById(R.id.cadSenha);
    }

    public void salvar(View view) {
        String usuario = editUsuario.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (usuario.isEmpty()) {
            editUsuario.setError("Preencha este campo");
            editUsuario.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            editEmail.setError("Preencha este campo");
            editEmail.requestFocus();
            return;
        }
        if (senha.isEmpty()) {
            editSenha.setError("Preencha este campo");
            editSenha.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Cadastro bem-sucedido, redirecionar para a HomeActivity
                            Intent intent = new Intent(CadastroActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // Fechar a atividade de cadastro
                        } else {
                            // Exibir mensagens de erro apropriadas
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                editSenha.setError("Senha fraca");
                                editSenha.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                editEmail.setError("E-mail inválido");
                                editEmail.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                editEmail.setError("E-mail já existe");
                                editEmail.requestFocus();
                            } catch (Exception e) {
                                Log.e("Cadastro", e.getMessage());
                            }
                        }
                    }
                });
    }
}
