package com.example.library;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Registro extends AppCompatActivity {

    private EditText editNombre, editApellido, editTelefono, editCorreo, editClave;
    private Button btnCrear, btnInicio;
    private ExecutorService executor;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Inicialización de los campos
        editNombre = findViewById(R.id.editNombre);
        editApellido = findViewById(R.id.editApellido);
        editTelefono = findViewById(R.id.editTelefono);
        editCorreo = findViewById(R.id.editCorreo);
        editClave = findViewById(R.id.editClave);
        btnCrear = findViewById(R.id.btn_crear);
        btnInicio = findViewById(R.id.btn_ir_inicio);

        // Inicializar el ExecutorService
        executor = Executors.newSingleThreadExecutor();

        // Acción del botón
        btnCrear.setOnClickListener(view -> {
            String nombre = editNombre.getText().toString();
            String apellido = editApellido.getText().toString();
            String telefono = editTelefono.getText().toString();
            String correo = editCorreo.getText().toString();
            String clave = editClave.getText().toString();

            // Validar datos
            if (!nombre.isEmpty() && !apellido.isEmpty() && !telefono.isEmpty() && !correo.isEmpty() && !clave.isEmpty()) {
                // Llamar al método para enviar los datos
                sendData(nombre, apellido, telefono, correo, clave);
            } else {
                Toast.makeText(Registro.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        btnInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redirige a un activity
                Intent intent = new Intent(getBaseContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void sendData(String nombre, String apellido, String telefono, String correo, String clave) {
        executor.execute(() -> {
            String response;
            try {
                // URL de la API
                URL url = new URL("http://10.0.2.2:80/app-mobile/usuarios_api.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                // Configurar la conexión
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);

                // Crear el JSON con los datos
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("nombre", nombre);
                jsonParam.put("apellido", apellido);
                jsonParam.put("telefono", telefono);
                jsonParam.put("correo", correo);
                jsonParam.put("clave", clave);

                // Enviar datos
                OutputStream os = httpURLConnection.getOutputStream();
                os.write(jsonParam.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                // Leer respuesta
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    response = "Registro exitoso";
                    //redirige a un activity
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    response = "Error en el registro: " + responseCode;
                }

                httpURLConnection.disconnect();
            } catch (Exception e) {
                response = "Error: " + e.getMessage();
            }

            // Mostrar el resultado en un Toast (debe hacerse en el hilo principal)
            String finalResponse = response;
            runOnUiThread(() -> Toast.makeText(Registro.this, finalResponse, Toast.LENGTH_LONG).show());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar el ExecutorService
        executor.shutdown();
    }
}
