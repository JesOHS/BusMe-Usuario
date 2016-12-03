package com.busme_usuario.controladores;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;

import com.busme_usuario.R;
import com.busme_usuario.fragments.BusMeUsuario;
import com.busme_usuario.modelos.DAO.CamionDAO;
import com.busme_usuario.modelos.DAO.RutaDAO;
import com.busme_usuario.modelos.DTO.Camion;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.postgis.Point;

import java.util.ArrayList;
import java.util.List;

public class Pintor extends AsyncTask<String, String, Void> {

    List<Camion> camiones;
    String encodedPolyline;
    private GoogleMap googleMap;
    private String id_ruta;
    private Location ubicacionUsuario;
    private String recorriendo;
    String color;


    public Pintor(GoogleMap googleMap, String id_ruta, Location ubicacionUsuario, String recorriendo,String color) {
        this.id_ruta = id_ruta;
        this.googleMap = googleMap;
        this.ubicacionUsuario = ubicacionUsuario;
        this.recorriendo = recorriendo;
        this.color = color;
    }

    @Override
    protected Void doInBackground(String... params) {
        RutaDAO rutaDAO = new RutaDAO();
        CamionDAO camionDAO = new CamionDAO();
        // Obtener la polilinea codificada de la bd
        encodedPolyline = rutaDAO.obtenerPolilinea(id_ruta, recorriendo);
        // Obtener camiones de la ruta seleccionada
        camiones = camionDAO.obtenerCamionesDeLaRuta(id_ruta, recorriendo);
        return null;
    }

    protected void onPostExecute(Void result) {
        //googleMap.clear();
        limpiarElementosDelMapa();
        pintarUbicacionUsuario();
        mostrarCamiones();
        pintarRuta();
        if(BusMeUsuario.getMarcadorEnRuta() != null) {
            pintarMarcadorEnRuta();
        }
    }

    private void limpiarElementosDelMapa() {
        if(BusMeUsuario.getLine() != null) {
            BusMeUsuario.getLine().remove();
        }
        if(BusMeUsuario.getMarcadorUsuario() != null) {
            BusMeUsuario.getMarcadorUsuario().remove();
        }
        if(BusMeUsuario.getMarcadorEnRuta() != null) {
            BusMeUsuario.getMarcadorEnRuta().remove();
        }
        if(BusMeUsuario.getMarcadoresDeCamiones().size() > 0) {
            for (Marker marcadorCamion:BusMeUsuario.getMarcadoresDeCamiones()) {
                marcadorCamion.remove();
            }
            BusMeUsuario.setMarcadoresDeCamiones(new ArrayList<Marker>());
            //BusMeUsuario.setMarcadoresDeCamiones(new ArrayList<Marker>());
        }
    }

    private void pintarMarcadorEnRuta() {
        Marker marcadorRuta = googleMap.addMarker(new MarkerOptions()
                .position(BusMeUsuario.getMarcadorEnRuta().getPosition())
                .title("Parada"));
        BusMeUsuario.setMarcadorEnRuta(marcadorRuta);
    }

    private void pintarRuta() {
        // Crear el objeto para agregar la polilinea
        PolylineOptions polylineOptions = new PolylineOptions();
        if (color=="rojo"){
            polylineOptions.color(Color.RED);

        }else if(color=="azul"){
            polylineOptions.color(Color.BLUE);
        }
        polylineOptions.width(20);
        polylineOptions.geodesic(true);
        // Agregar la polilinea decodificada con PolyUtil.decode()
        polylineOptions.addAll(PolyUtil.decode(encodedPolyline));
        Polyline linea = googleMap.addPolyline(polylineOptions);
        linea.setVisible(true);
        linea.setClickable(false);
        BusMeUsuario.setLine(linea);
    }

    private void mostrarCamiones() {
        LatLng coordenadas;
        Point punto;
        for (int i = 0; i < camiones.size(); i++) {
            punto = (Point) camiones.get(i).getGeom().getGeometry();
            coordenadas = new LatLng(punto.x, punto.y);
            Marker marcadorCamion = googleMap.addMarker(new MarkerOptions()
                    .position(coordenadas)
                    .title("Camion")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marcador_camion)));
            BusMeUsuario.getMarcadoresDeCamiones().add(marcadorCamion);
        }
    }

    private void pintarUbicacionUsuario() {
        double latitud = ubicacionUsuario.getLatitude();
        double longitud = ubicacionUsuario.getLongitude();
        LatLng coordenadas = new LatLng(latitud, longitud);
        Marker marcadorUsuario = googleMap.addMarker(new MarkerOptions()
                .position(coordenadas)
                .title("Yo")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.iconito)));
        BusMeUsuario.setMarcadorUsuario(marcadorUsuario);
    }

}