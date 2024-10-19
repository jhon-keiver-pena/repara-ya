package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.gamelibery.R;
import com.example.library.service.UserService;
import com.example.library.model.rest.Maestro;

public class Cotizar extends AppCompatActivity {

    private TextView txtComuna, txtDias, infoMaestro, valorCotizacion;
    private Spinner comuna, dias;
    private Button btnCotizar, btnBiblioteca, btnReserva;
    private ImageView imageViewMaestro;

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
        setContentView(R.layout.activity_cotizacion);

        imageViewMaestro = findViewById(R.id.imageView4);
        txtComuna = findViewById(R.id.txtcomuna);
        txtDias = findViewById(R.id.txtDias);
        infoMaestro = findViewById(R.id.txtMaestro);
        comuna = findViewById(R.id.spinnerComuna);
        dias = findViewById(R.id.spinnerDias);
        btnCotizar = findViewById(R.id.btnCotiza);
        btnBiblioteca = findViewById(R.id.btnCotizarR);
        btnReserva = findViewById(R.id.btnConfirmar);
        imageViewMaestro = findViewById(R.id.imageView4);
        valorCotizacion= findViewById(R.id.txtValor);

        Maestro maestro = (Maestro) getIntent().getSerializableExtra("maestro");
        String nombreMaestro = getIntent().getStringExtra("nombre_maestro");

        // Mostrar el nombre del maestro si se ha recibido correctamente
        if (nombreMaestro != null) {
            infoMaestro.setText('\n' + maestro.getNombre() + '\n'+  maestro.getEspecialidad());
        }

        if (maestro != null) {
            // Cargar la imagen usando Glide
            Glide.with(this).load(maestro.getUrlImagen()).into(imageViewMaestro);
        }else {
            Log.d("Cotizar", "El maestro es null.");
        }

        //creamos un adaptador para implementar las opciones del spinner
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource
                (this,R.array.comunas, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> adapterDias=ArrayAdapter.createFromResource
                (this,R.array.dias, android.R.layout.simple_spinner_item);

        //se carga el adaptador al spinner
        comuna.setAdapter(adapter);

        dias.setAdapter(adapterDias);

        //creamos un evento para obtener la selecion del usuario
        comuna.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                Toast.makeText(parent.getContext(), parent.getItemAtPosition(position).toString()
                        ,Toast.LENGTH_SHORT).show();

                txtComuna.setText(parent.getItemAtPosition(position).toString());

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                Toast.makeText(parent.getContext(), parent.getItemAtPosition(position).toString()
                        ,Toast.LENGTH_SHORT).show();

                txtDias.setText(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnCotizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                cotizar();
            }
        });

        btnBiblioteca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), BibliotecaMaestro.class);
                startActivity(intent);
                finish();
            }
        });

        btnReserva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //datos que necesito pasar para Reservar
                String comunaSeleccionada = comuna.getSelectedItem() != null ? comuna.getSelectedItem().toString() : "";
                String diaSeleccionado = dias.getSelectedItem() != null ? dias.getSelectedItem().toString() : "";
                double valorCotizado = generarPrecioPorComuna(comunaSeleccionada);

                Intent intent = new Intent(getBaseContext(), Reserva.class);
                intent.putExtra("comuna", comunaSeleccionada);
                intent.putExtra("dia", diaSeleccionado);
                intent.putExtra("valor_cotizacion", valorCotizado);
                intent.putExtra("maestro", maestro);
                startActivity(intent);
                finish();
            }
        });
    }

    private void cotizar() {
        String comunaSeleccionada = comuna.getSelectedItem() != null ? comuna.getSelectedItem().toString() : "";
        String diaSeleccionado = dias.getSelectedItem() != null ? dias.getSelectedItem().toString() : "";

        // Validar que no se haya seleccionado "Seleccione Comuna" y "Seleccione Día"
        if (comunaSeleccionada.isEmpty()) {
            Toast.makeText(Cotizar.this, "Debe seleccionar una comuna válida", Toast.LENGTH_SHORT).show();
            return;
        }
        if (diaSeleccionado.isEmpty()) {
            Toast.makeText(Cotizar.this, "Debe seleccionar un día válido", Toast.LENGTH_SHORT).show();
            return;
        }else {

            // Generar un valor base de la comuna entre $20,000 y $50,000
            double precioBase = generarPrecioPorComuna(comunaSeleccionada);

            // Aumentar el precio si es fin de semana (sábado o domingo)
            if (diaSeleccionado.equalsIgnoreCase("Sábado")
                    || diaSeleccionado.equalsIgnoreCase("Domingo")) {
                precioBase += 15000; // Incremento adicional por ser fin de semana
            }

            // Limitar el precio final al rango entre $20,000 y $65,000
            if (precioBase > 65000) {
                precioBase = 65000;
            } else if (precioBase < 20000) {
                precioBase = 20000;
            }

            // Mostrar el precio final en txtV
            valorCotizacion.setText("El precio final es: $" + precioBase);
        }
    }

    private double generarPrecioPorComuna(String comuna) {
        // Definir un precio base fijo para algunas comunas
        double precioBase;
        switch (comuna) {
            case "Cerrillos":
            case "Cerro Navia":
            case "Conchalí":
            case "El Bosque":
            case "Lo Espejo":
                precioBase = 20000 + (int)(Math.random() * 5000); // Comunas más económicas
                break;
            case "Las Condes":
            case "Vitacura":
            case "Lo Barnechea":
                precioBase = 45000 + (int)(Math.random() * 5000); // Comunas más caras
                break;
            default:
                precioBase = 30000 + (int)(Math.random() * 10000); // Comunas promedio
                break;
        }

        return precioBase;
    }


}