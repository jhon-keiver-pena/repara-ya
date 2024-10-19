package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;
import com.example.library.model.rest.Maestro;
import com.example.library.service.UserService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Reserva extends AppCompatActivity {

    private TextView info;
    private Button confirmar, volverHome, volverCotizar;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Validar si esta logeado
        UserService userService = (UserService) getApplicationContext();
        if (!userService.getUsuario().isLogin()){
            Toast.makeText(this, "Debes iniciar Sesion para acceder a esta pantalla",
                    Toast.LENGTH_SHORT).show();
            //redirige a un activity
            Intent intent = new Intent(getBaseContext(), Login.class);
            startActivity(intent);
            finish();
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reserva);

        info = findViewById(R.id.txt_Info);
        confirmar = findViewById(R.id.btn_confirmar);
        volverHome = findViewById(R.id.btnVHome);
        volverCotizar = findViewById(R.id.btn_VCotizar);

        // Obtener los datos pasados desde Cotizar
        Intent intent = getIntent();
        String comunaSeleccionada = intent.getStringExtra("comuna");
        String diaSeleccionado = intent.getStringExtra("dia");
        double valorCotizacion = intent.getDoubleExtra("valor_cotizacion", 0);

        // mostramos los datos en el txt
        info.setText("Comuna: " + comunaSeleccionada + "\nDía: " + diaSeleccionado + "\nValor: $" + valorCotizacion);

        executor = Executors.newSingleThreadExecutor();

        volverHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), Home.class);
                startActivity(intent);
                finish();
            }
        });

        volverCotizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), Cotizar.class);
                startActivity(intent);
                finish();
            }
        });

        confirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realizarReserva();
            }
        });
    }

    private void realizarReserva() {
        // Obtener el id del maestro y del usuario
        Maestro maestro = (Maestro) getIntent().getSerializableExtra("maestro");
        int idMaestro = maestro != null ? maestro.getId() : -1;

        UserService userService = (UserService) getApplicationContext();
        int idUsuario = userService.getUsuario().getIdUsuario();

        // Obtener los datos que fueron pasados desde Cotizar
        String comunaSeleccionada = getIntent().getStringExtra("comuna");
        String diaSeleccionado = getIntent().getStringExtra("dia");
        double valorCotizacion = getIntent().getDoubleExtra("valor_cotizacion", 0.0);

        //mostrar datos validacion en logcat
        Log.d("Reserva", "idMaestro: " + idMaestro);
        Log.d("Reserva", "comunaSeleccionada: " + comunaSeleccionada);
        Log.d("Reserva", "diaSeleccionado: " + diaSeleccionado);
        Log.d("Reserva", "idUsuario: " + idUsuario);
        Log.d("Reserva", "valorCotizacion: " + valorCotizacion);

        // Validar que los datos estén correctos
        if (idMaestro == -1 || comunaSeleccionada.isEmpty() || diaSeleccionado.isEmpty() || idUsuario == -1) {
            Toast.makeText(Reserva.this, "Datos inválidos para realizar la reserva", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener la fecha en formato "YYYY-MM-DD" a partir del nombre del día
        String fechaVisita = obtenerFechaPorNombreDia(diaSeleccionado);
        if (fechaVisita == null) {
            Toast.makeText(this, "Error al obtener la fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el JSON con los datos de la reserva
        JSONObject reservaJSON = new JSONObject();
        try {
            reservaJSON.put("id_maestro", idMaestro);
            reservaJSON.put("fecha_visita", fechaVisita);
            reservaJSON.put("coste", valorCotizacion);
            reservaJSON.put("ciudad", comunaSeleccionada);
            reservaJSON.put("id_usuario", idUsuario);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(Reserva.this, "Error al generar la reserva", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enviar la reserva al servidor (como te expliqué anteriormente)
        sendReservation(reservaJSON);
    }

    private String obtenerFechaPorNombreDia(String diaNombre) {
        // Mapa de nombres de días a números
        int diaDeseado = -1;

        switch (diaNombre.toLowerCase()) {
            case "domingo":
                diaDeseado = Calendar.SUNDAY;
                break;
            case "lunes":
                diaDeseado = Calendar.MONDAY;
                break;
            case "martes":
                diaDeseado = Calendar.TUESDAY;
                break;
            case "miercoles":
                diaDeseado = Calendar.WEDNESDAY;
                break;
            case "jueves":
                diaDeseado = Calendar.THURSDAY;
                break;
            case "viernes":
                diaDeseado = Calendar.FRIDAY;
                break;
            case "sabado":
                diaDeseado = Calendar.SATURDAY;
                break;
            default:
                return null; // Día inválido
        }

        // Obtener el día actual
        Calendar calendar = Calendar.getInstance();
        int diaActual = calendar.get(Calendar.DAY_OF_WEEK);
        int diasHastaProximo = (diaDeseado - diaActual + 7) % 7;

        Log.d("diaActual", ":" + diaActual);
        Log.d("diaDeseado", ": " + diaDeseado);
        Log.d("diaHastaProximo", ":" + diasHastaProximo);


        // Si hoy es el día deseado, obtenemos la fecha para la próxima semana
        if (diasHastaProximo == 0) {
            diasHastaProximo = 7; // Obtener el próximo día deseado
        }

        calendar.add(Calendar.DAY_OF_MONTH, diasHastaProximo);

        // Formatear la fecha
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private void sendReservation(JSONObject reservaJSON) {
        executor.execute(() -> {
            String response;
            try {
                URL url = new URL("http://10.0.2.2:80/app-mobile/reserva_api.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);

                // Enviar el JSON al servidor
                OutputStream os = httpURLConnection.getOutputStream();
                os.write(reservaJSON.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    response = "Reserva realizada con éxito";
                    String finalResponse = response;
                    runOnUiThread(() -> {
                        Toast.makeText(Reserva.this, finalResponse, Toast.LENGTH_LONG).show();
                        // Redirigir si es necesario
                        Intent intent = new Intent(getBaseContext(), Home.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    InputStream errorStream = httpURLConnection.getErrorStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    response = "Error en la reserva: " + errorResponse.toString();
                    String finalResponse1 = response;
                    runOnUiThread(() -> Toast.makeText(Reserva.this, finalResponse1, Toast.LENGTH_LONG).show());
                }

                httpURLConnection.disconnect();
            } catch (Exception e) {
                response = "Error: " + e.getMessage();
                String finalResponse2 = response;
                runOnUiThread(() -> Toast.makeText(Reserva.this, finalResponse2, Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar el ExecutorService
        executor.shutdown();
    }
}
