package com.busme_usuario.modelos.DTO;

public class Ruta {
    String idRuta;
    String polilinea;

    public Ruta(String polilinea) {
        this.polilinea = polilinea;
    }

    public Ruta(String idRuta, String polilinea) {
        this.idRuta = idRuta;
        this.polilinea = polilinea;
    }

    public String getIdRuta() {
        return idRuta;
    }

    public void setIdRuta(String idRuta) {
        this.idRuta = idRuta;
    }

    public String getPolilinea() {
        return polilinea;
    }

    public void setPolilinea(String polilinea) {
        this.polilinea = polilinea;
    }
}