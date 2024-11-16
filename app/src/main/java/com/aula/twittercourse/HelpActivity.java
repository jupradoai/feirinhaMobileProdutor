package com.aula.twittercourse;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends MenuActivity {
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<String> messages;
    private LinearLayout optionsLayout;
    private Button helpButton;
    private Button endSupportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        messages = new ArrayList<>();
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        chatAdapter = new ChatAdapter(messages);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        optionsLayout = findViewById(R.id.options_layout);
        helpButton = findViewById(R.id.support_button);
        endSupportButton = new Button(this);
        endSupportButton.setText("Encerrar Suporte");
        endSupportButton.setVisibility(View.GONE); // Inicialmente invisível

        helpButton.setOnClickListener(v -> {
            helpButton.setVisibility(View.GONE); // Esconde o botão de ajuda
            showHelpOptions(); // Exibe as opções de ajuda
        });
    }

    private void showHelpOptions() {
        optionsLayout.setVisibility(View.VISIBLE);
        optionsLayout.removeAllViews();

        String[] options = {"Como usar o app", "Problemas técnicos", "Fale com o suporte"};

        for (int i = 0; i < options.length; i++) {
            final int index = i;
            Button button = new Button(this);
            button.setText(options[i]);
            button.setOnClickListener(v -> handleMainOption(index + 1));
            optionsLayout.addView(button);
        }

        optionsLayout.addView(endSupportButton); // Adiciona o botão de encerrar suporte
        endSupportButton.setVisibility(View.VISIBLE); // Exibe o botão de encerrar suporte
        endSupportButton.setOnClickListener(v -> {
            optionsLayout.setVisibility(View.GONE); // Esconde as opções de suporte
            endSupportButton.setVisibility(View.GONE); // Esconde o botão de encerrar suporte
            helpButton.setVisibility(View.VISIBLE); // Exibe novamente o botão de ajuda
        });
    }

    private void handleMainOption(int option) {
        optionsLayout.setVisibility(View.GONE);

        switch (option) {
            case 1:
                showSubOptions("Para usar o app, você gostaria de saber:\n1. Como criar uma conta?\n2. Como enviar uma mensagem?");
                break;
            case 2:
                chatAdapter.addMessage("Se você está enfrentando problemas técnicos, tente reiniciar o app ou verificar sua conexão.");
                break;
            case 3:
                chatAdapter.addMessage("Você pode entrar em contato com o suporte através deste link...");
                break;
        }
    }

    private void showSubOptions(String message) {
        chatAdapter.addMessage(message);

        Button backButton = new Button(this);
        backButton.setText("Voltar");
        backButton.setOnClickListener(v -> {
            chatAdapter.addMessage("Você voltou para o menu anterior.");
            showHelpOptions();
        });

        optionsLayout.removeAllViews();
        optionsLayout.addView(backButton);

        for (int i = 1; i <= 2; i++) {
            final int subOptionIndex = i;
            Button button = new Button(this);
            button.setText("Subopção " + subOptionIndex);
            button.setOnClickListener(v -> {
                chatAdapter.addMessage("Você escolheu a Subopção " + subOptionIndex + ".");
            });
            optionsLayout.addView(button);
        }

        optionsLayout.addView(endSupportButton); // Adiciona o botão de encerrar suporte
        optionsLayout.setVisibility(View.VISIBLE);
    }
}
