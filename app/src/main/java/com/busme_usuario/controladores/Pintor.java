package com.busme_usuario.controladores;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

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

import java.util.List;

public class Pintor extends AsyncTask<String, String, Void> {

    List<Camion> camiones;
    String encodedPolyline;
    private GoogleMap googleMap;
    private String id_ruta;
    static private Polyline line;
    private Location ubicacionUsuario;

    public Pintor(GoogleMap googleMap, String id_ruta, Marker marcadorUsuario, Polyline line, Location ubicacionUsuario) {
        this.googleMap = googleMap;
        this.id_ruta = id_ruta;
        //this.marcadorUsuario = marcadorUsuario;
        //Log.i("DBEUG", "usuario recibido: " + marcadorUsuario.toString());
        this.line = line;
        this.ubicacionUsuario = ubicacionUsuario;
    }

    @Override
    protected Void doInBackground(String... params) {
        RutaDAO rutaDAO = new RutaDAO();
        CamionDAO camionDAO = new CamionDAO();
        // Obtener la polilinea codificada de la bd
        encodedPolyline = rutaDAO.obtenerPolilinea(id_ruta);
        // Obtener camiones de la ruta seleccionada
        camiones = camionDAO.obtenerCamionesDeLaRuta(id_ruta);
        return null;
    }

    protected void onPostExecute(Void result) {
        googleMap.clear();
        pintarUbicacionUsuario();
        mostrarCamiones();
        dibujarRuta();
    }

    private void dibujarRuta() {
        // Crear el objeto para agregar la polilinea
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(20);
        polylineOptions.geodesic(true);
        // Agregar la polilinea decodificada con PolyUtil.decode()
        polylineOptions.addAll(PolyUtil.decode(encodedPolyline));
        if(line != null) {
            line.remove();
        }
        line = googleMap.addPolyline(polylineOptions);
        line.setVisible(true);
        line.setClickable(true);
        BusMeUsuario.setLine(line);
    }

    private void mostrarCamiones() {
        Marker m[] = new Marker[camiones.size()];
        LatLng coordenadas;
        Point punto;
        for (int i = 0; i < camiones.size(); i++) {
            punto = (Point) camiones.get(i).getGeom().getGeometry();
            coordenadas = new LatLng(punto.x, punto.y);
            googleMap.addMarker(new MarkerOptions()
                    .position(coordenadas)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marcador_camion)));
        }
    }

    private void pintarUbicacionUsuario() {
        // Se muestra la posicion
        if (ubicacionUsuario != null) {
            double latitud = ubicacionUsuario.getLatitude();
            double longitud = ubicacionUsuario.getLongitude();
            LatLng coordenadas = new LatLng(latitud, longitud);
            googleMap.addMarker(new MarkerOptions()
                    .position(coordenadas)
                    .title("Yo")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.iconito)));
        }
    }

}