package com.busme_usuario.modelos.DTO;

import org.postgis.PGgeometry;

public class Camion {
    String idCamion;
    String idRuta;
    int capacidadMaxima;
    int asientosDisponibles;
    PGgeometry geom;

    public Camion(String idRuta, int capacidadMaxima, int asientosDisponibles, PGgeometry geom) {
        this.idRuta = idRuta;
        this.capacidadMaxima = capacidadMaxima;
        this.asientosDisponibles = asientosDisponibles;
        this.geom = geom;
    }

    public Camion(String idCamion, String idRuta, int capacidadMaxima, int asientosDisponibles, PGgeometry geom) {
        this.idCamion = idCamion;
        this.idRuta = idRuta;
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
