package com.example.library;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamelibery.R;
import com.example.library.service.UserService;

public class Home extends AppCompatActivity {

    private Button btnCrear, btnReserva, btnCerrar;

    private ViewFlipper vf;
    private int[] image = {R.drawable.img_herramientas, R.drawable.img_obra, R.drawable.img_casco};
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
        setContentView(R.layout.activity_home);

        btnCrear = findViewById(R.id.ir_biblioteca);
        btnReserva = findViewById(R.id.btn_reservas);
        btnCerrar = findViewById(R.id.btn_Home);

        vf = (ViewFlipper)findViewById(R.id.slider);

        for (int i=0; i<image.length; i++){
            flipImg(image[i]);
        }
        // Acción del botón
        btnCrear.setOnClickListener(view -> {
            //redirige a un activity
            Intent intent = new Intent(getBaseContext(), BibliotecaMaestro.class);
            startActivity(intent);
            finish();
        });

        // Acción del botón
        btnReserva.setOnClickListener(view -> {
            //redirige a un activity
            Intent intent = new Intent(getBaseContext(), ListaReservaActivity.class);
            startActivity(intent);
            finish();
        });

        // Acción del botón
        btnCerrar.setOnClickListener(view -> {
            //Cambiar valor a false
            userService.getUsuario().setLogin(false);
            //redirige a un activity
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

    }

    public void flipImg(int i){
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundResource(i);
        vf.addView(imageView);
        vf.setFlipInterval(2800);
        vf.setAutoStart(true);

        vf.setInAnimation(this, android.R.anim.slide_in_left);
        vf.setOutAnimation(this, android.R.anim.slide_out_right);

    }
}