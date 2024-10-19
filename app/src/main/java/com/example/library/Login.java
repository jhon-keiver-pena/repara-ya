package com.example.library;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gamelibery.R;
import com.example.library.service.UserService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Login extends AppCompatActivity {

    private EditText usuario, contraseña;
    private Button buttonini, buttonregis;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Inicializar el ExecutorService
        executor = Executors.newSingleThreadExecutor();

        usuario = findViewById(R.id.input_mail);
        contraseña = findViewById(R.id.input_password);
        buttonini = findViewById(R.id.btn_inicio);
        buttonregis = findViewById(R.id.btn_crear);

        buttonini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String correo=usuario.getText().toString();
                String clave=contraseña.getText().toString();

                if(correo.equals("") || clave.equals("")){
                    Toast.makeText(Login.this,"Los datos no estan completos...",Toast.LENGTH_SHORT).show();
                    usuario.requestFocus();     //para que el cursor aparezca en usuario
                }
                else
                {
                    validateData(correo,clave);
                }

            }
        });

        buttonregis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), Registro.class);
                startActivity(intent);
                finish();
            }
        });

    }
    private void validateData(String correo, String clave) {
        executor.execute(() -> {
            String response = "";
            try {
                // URL de la API con los parámetros de consulta
                URL url = new URL("http://10.0.2.2:80/app-mobile/usuarios_api.php?correo=" + correo + "&clave=" + clave);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                // Configurar la conexión
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoInput(true); // Para leer la respuesta

                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Leer la respuesta
                    StringBuilder responseBuilder = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            responseBuilder.append(inputLine);
                        }
                    }

                    // Convertir la respuesta a un objeto JSON
                    JSONObject jsonResponse = new JSONObject(responseBuilder.toString());

                    // Verificar que el objeto no sea null y tenga datos
                    if (jsonResponse != null && jsonResponse.length() > 0) {
                        UserService userService = (UserService) getApplicationContext();
                        userService.getUsuario().setIdUsuario(jsonResponse.getInt("id_usuario"));
                        userService.getUsuario().setNombre(jsonResponse.getString("nombre"));
                        userService.getUsuario().setApellido(jsonResponse.getString("apellido"));
                        userService.getUsuario().setTelefono(jsonResponse.getString("telefono"));
                        userService.getUsuario().setCorreo(jsonResponse.getString("correo"));
                        userService.getUsuario().setClave(jsonResponse.getString("clave"));
                        userService.getUsuario().setLogin(true);

                        // Redirige a un activity si el usuario es válido
                        Intent intent = new Intent(getBaseContext(), Home.class);
                        startActivity(intent);
                        finish();
                    } else {
                        response = "Credenciales inválidas";
                    }
                } else {
                    response = "Credenciales inválidas";
                }

                httpURLConnection.disconnect();
            } catch (Exception e) {
                response = "Error: " + e.getMessage();
            }

            // Mostrar el resultado en un Toast (debe hacerse en el hilo principal)
            String finalResponse = response;
            runOnUiThread(() -> Toast.makeText(Login.this, finalResponse, Toast.LENGTH_LONG).show());
        });
    }


    private void validateData_OLD(String correo, String clave){
        executor.execute(() -> {
            String response;
            try {
                // URL de la API
                URL url = new URL("http://10.0.2.2:80/app-mobile/usuarios_api.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                // Configurar la conexión
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);

                // Crear el JSON con los datos
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("correo", correo);
                jsonParam.put("clave", clave);

                OutputStream os = httpURLConnection.getOutputStream();
                os.write(jsonParam.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    response = "Inicio de sesion";
                    //redirige a un activity
                    Intent intent = new Intent(getBaseContext(), Home.class);
                    startActivity(intent);
                    finish();
                } else {
                    response = "Credenciales incorrectas " + responseCode;
                }

                httpURLConnection.disconnect();
            } catch (Exception e) {
                response = "Error: " + e.getMessage();
            }

            // Mostrar el resultado en un Toast (debe hacerse en el hilo principal)
            String finalResponse = response;
            runOnUiThread(() -> Toast.makeText(Login.this, finalResponse, Toast.LENGTH_LONG).show());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar el ExecutorService
        executor.shutdown();
    }
}