package com.example.library.service;

import android.app.Application;

import com.example.library.model.UsuarioContexto;

public class UserService extends Application {

    private UsuarioContexto usuario;

    @Override
    public void onCreate(){
        super.onCreate();
        usuario = new UsuarioContexto();
    }

    public UsuarioContexto getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioContexto usuario) {
        this.usuario = usuario;
    }
}
