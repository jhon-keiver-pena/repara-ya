package com.example.library;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;


public class MainActivity extends AppCompatActivity {

    //declaracion de variable
    private Button inicio;
    private Button registrar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //llamada por id
        inicio = findViewById(R.id.btn_ir_inicio);
        registrar = findViewById(R.id.btn_Home);

        //funcionalidad de los botones
        inicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redirige a un activity
                Intent intent = new Intent(getBaseContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redirige a un activity
                Intent intent = new Intent(getBaseContext(), Registro.class);
                startActivity(intent);
                finish();
            }
        });
        };
    }
