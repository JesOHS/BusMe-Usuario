package com.busme_usuario.modelos.DTO;

import org.postgis.PGgeometry;

public class Ruta {
    String idRuta;
    PGgeometry geom;

    public Ruta(String idRuta, PGgeometry geom) {
        this.idRuta = idRuta;
        this.geom = geom;
    }

    public String getIdRuta() {
        return idRuta;
    }

    public void setIdRuta(String idRuta) {
        this.idRuta = idRuta;
    }

    public PGgeometry getGeom() {
        return geom;
    }

    public void setGeom(PGgeometry geom) {
        this.geom = geom;
    }

}