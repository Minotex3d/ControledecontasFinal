package com.example.controledecontas;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdicionarEndereco extends AppCompatActivity {

    private EditText editTextRua, editTextBairro, editTextNumero, editTextCidade;
    private Button buttonSalvar;
    private FirebaseFirestore db;
    private String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_endereco);

        db = FirebaseFirestore.getInstance();

        editTextRua = findViewById(R.id.editTextRua);
        editTextBairro = findViewById(R.id.editTextBairro);
        editTextNumero = findViewById(R.id.editTextNumero);
        editTextCidade = findViewById(R.id.editTextCidade);
        buttonSalvar = findViewById(R.id.buttonCadastrar2);

        documentId = getIntent().getStringExtra("documentId");

        if (documentId != null) {
            carregarEnderecoEdicao();
        }

        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarEndereco();
            }
        });
    }

    private void carregarEnderecoEdicao() {
        db.collection("contas_db")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editTextRua.setText(documentSnapshot.getString("rua"));
                        editTextBairro.setText(documentSnapshot.getString("bairro"));
                        editTextNumero.setText(String.valueOf(documentSnapshot.getLong("numero")));
                        editTextCidade.setText(documentSnapshot.getString("cidade"));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdicionarEndereco.this, "Erro ao carregar" + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void salvarEndereco() {
        Map<String, Object> endereco = criarMapEndereco();

        if (documentId != null) {
            atualizarEndereco(endereco);
        } else {
            adicionarNovoEndereco(endereco);
        }
    }

    private Map<String, Object> criarMapEndereco() {
        Map<String, Object> endereco = new HashMap<>();
        endereco.put("rua", editTextRua.getText().toString());
        endereco.put("bairro", editTextBairro.getText().toString());
        endereco.put("numero", Integer.parseInt(editTextNumero.getText().toString()));
        endereco.put("cidade", editTextCidade.getText().toString());
        return endereco;
    }

    private void adicionarNovoEndereco(Map<String, Object> endereco) {
        db.collection("contas_db")
                .add(endereco)
                .addOnSuccessListener(documentReference -> {
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdicionarEndereco.this, "Erro ao adicionar" + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void atualizarEndereco(Map<String, Object> endereco) {
        db.collection("contas_db")
                .document(documentId)
                .update(endereco)
                .addOnSuccessListener(aVoid -> {
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdicionarEndereco.this, "Erro ao atualizar" + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
