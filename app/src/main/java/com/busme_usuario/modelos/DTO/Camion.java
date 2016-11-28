package com.busme_usuario.modelos.DTO;

import org.postgis.PGgeometry;

public class Camion {
    String idCamion;
    String idRuta;
    String recorriendo;
    int capacidadMaxima;
    int asientosDisponibles;
    PGgeometry geom;

    public Camion(String idRuta, String recorriendo, int capacidadMaxima, int asientosDisponibles, PGgeometry geom) {
        this.idRuta = idRuta;
        this.recorriendo = recorriendo;
        this.capacidadMaxima = capacidadMaxima;
        this.asientosDisponibles = asientosDisponibles;
        this.geom = geom;
    }

    public Camion(String idCamion, String idRuta, String recorriendo, int capacidadMaxima, int asientosDisponibles, PGgeometry geom) {
        this.idCamion = idCamion;
        this.idRuta = idRuta;
        this.recorriendo = recorriendo;
        this.capacidadMaxima = capacidadMaxima;
        this.asientosDisponibles = asientosDisponibles;
        this.geom = geom;
    }

    public String getIdCamion() {
        return idCamion;
    }

    public void setIdCamion(String idCamion) {
        this.idCamion = idCamion;
    }

    public String getIdRuta() {
        return idRuta;
    }

    public void setIdRuta(String idRuta) {
        this.idRuta = idRuta;
    }

    public String getRecorriendo() {
        return recorriendo;
    }

    public void setRecorriendo(String recorriendo) {
        this.recorriendo = recorriendo;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public int getAsientosDisponibles() {
        return asientosDisponibles;
    }

    public void setAsientosDisponibles(int asientosDisponibles) {
        this.asientosDisponibles = asientosDisponibles;
    }

    public PGgeometry getGeom() {
        return geom;
    }

    public void setGeom(PGgeometry geom) {
        this.geom = geom;
    }
}
