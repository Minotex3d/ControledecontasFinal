package com.example.controledecontas;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class Cadastrar extends AppCompatActivity {

    private ListView listViewEnderecos;
    private FirebaseFirestore db;
    private ArrayAdapter<String> adapter;
    private List<String> enderecoList;
    private List<String> documentIds;
    private ListenerRegistration registration;
    private String selectedDocumentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar);

        Toolbar toolbar = findViewById(R.id.toolbar6);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        listViewEnderecos = findViewById(R.id.listView6);
        enderecoList = new ArrayList<>();
        documentIds = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, enderecoList);
        listViewEnderecos.setAdapter(adapter);

        registerForContextMenu(listViewEnderecos);


        ///floating buton
        FloatingActionButton fabAdicionar = findViewById(R.id.fab5);
        fabAdicionar.setOnClickListener(v -> {
            Intent intent = new Intent(Cadastrar.this, AdicionarEndereco.class);
            startActivityForResult(intent, 1);
        });

        carregarEnderecos();
    }

    private void carregarEnderecos() {
        registration = db.collection("contas_db")
                .addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                        Toast.makeText(Cadastrar.this, "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    enderecoList.clear();
                    documentIds.clear();
               for (DocumentChange documentChange : snapshot.getDocumentChanges()) {
                        String documentId = documentChange.getDocument().getId();
                        String endereco = documentChange.getDocument().getString("rua") + ", " +
                        documentChange.getDocument().getString("bairro") + ", " +
                         documentChange.getDocument().getLong("numero") + ", " +
                       documentChange.getDocument().getString("cidade");

           if (documentChange.getType() == DocumentChange.Type.ADDED || documentChange.getType() == DocumentChange.Type.MODIFIED) {
                            enderecoList.add(endereco);
                            documentIds.add(documentId);
                        }
                    }
                    adapter.notifyDataSetChanged();

                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            carregarEnderecos();
        }
    }
    //menus de contexto
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contexto5, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        selectedDocumentId = documentIds.get(info.position);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_edit) {
            editarEndereco(selectedDocumentId);
            return true;
        } else if (itemId == R.id.action_delete) {
            excluirEndereco(selectedDocumentId);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    //acionar edicao
    private void editarEndereco(String documentId) {
        Intent intent = new Intent(Cadastrar.this, AdicionarEndereco.class);
        intent.putExtra("documentId", documentId);
        startActivityForResult(intent, 2);
    }

    private void excluirEndereco(String documentId) {
        db.collection("contas_db")
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Cadastrar.this, "Endereço excluído com sucesso", Toast.LENGTH_SHORT).show();
                    carregarEnderecos();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Cadastrar.this, "Não foi possivel excluir endereco " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
