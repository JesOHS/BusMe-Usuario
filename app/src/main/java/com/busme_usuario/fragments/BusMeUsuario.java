package com.busme_usuario.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.busme_usuario.R;
import com.busme_usuario.interfaces.RetrofitMaps;
import com.busme_usuario.modelos.DAO.CamionDAO;
import com.busme_usuario.modelos.DAO.RutaDAO;
import com.busme_usuario.modelos.DTO.Camion;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.widget.Toast;

import com.busme_usuario.modelos.POJO.Example;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.postgis.Point;

import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class BusMeUsuario extends FragmentActivity implements OnMapReadyCallback, Spinner.OnItemSelectedListener, LocationListener {

    static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    GoogleMap mMap;
    Marker marcadorUsuario;
    LocationManager locationManager;
    Spinner spinner;
    LatLng puntoEnRutaSeleccionado;
    LatLng origin;
    LatLng dest;
    Polyline line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_me_usuario);
        //show error dialog if Google Play Services not available
        if (!isGooglePlayServicesAvailable()) {
            Log.d("onCreate", "Google Play Services not available. Ending Test case.");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services available. Continuing.");
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        configurarActualizacionesDePosicion();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Se obtiene el spinner
        spinner = (Spinner) findViewById(R.id.spinnerRutas);
        //spinner.setOnItemClickListener(this);
        cargarRutasEnSpinner();
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Se obtiene la ubicacion del usuario
        Location ubicacionUsuario = obtenerUbicacionUsuario();
        // Se muestra la ubicacion en el mapa
        mostrarUbicacionUsuario(ubicacionUsuario);
        double latitud = ubicacionUsuario.getLatitude();
        double longitud = ubicacionUsuario.getLongitude();
        /*
        Se obtienen las coordenadas para hacer zoom a la posicion del usuario
        en el mapa
        */
        LatLng coordenadas = new LatLng(latitud, longitud);
        // Define en donde va a hacer zoom y a que nivel
        CameraUpdate actualizacionDeCamara = CameraUpdateFactory.newLatLngZoom(coordenadas, 16);
        // Se hace el zoom en el mapa
        mMap.animateCamera(actualizacionDeCamara);
        mostrarCamiones();
        /*
        Evento que se llama cuando se le da click al mapa,
        para agregar un marcador
         */
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                // Se limpia el mapa;
                // mMap.clear();
                // Se agrega un marcador donde el usuario selecciono
                puntoEnRutaSeleccionado = point;
                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();
                // Setting the position of the marker
                options.position(puntoEnRutaSeleccionado);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                // Se agrega el marcador al mapa
                mMap.addMarker(options);
            }
        });

        Button btnDriving = (Button) findViewById(R.id.btnCalcularTiempo);
        btnDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                build_retrofit_and_get_response("driving");
            }
        });
    }

    private void build_retrofit_and_get_response(String type) {
        String url = "https://maps.googleapis.com/maps/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        Call<Example> call = service.getDistanceDuration("metric", origin.latitude + "," + origin.longitude, dest.latitude + "," + dest.longitude, type);
        call.enqueue(new Callback<Example>() {

            @Override
            public void onResponse(Response<Example> response, Retrofit retrofit) {
                try {
                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getRoutes().size(); i++) {
                        String distance = response.body().getRoutes().get(i).getLegs().get(i).getDistance().getText();
                        String time = response.body().getRoutes().get(i).getLegs().get(i).getDuration().getText();
                        //ShowDistanceDuration.setText("Distance:" + distance + ", Duration:" + time);
                        Toast.makeText(getApplicationContext(), "Tiempo: " + time, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });

    }

    private Location obtenerUbicacionUsuario() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        // Se obtiene la posicion del usuario
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private void mostrarUbicacionUsuario(Location ubicacion) {
        // Se muestra la posicion
        if (ubicacion != null) {
            double latitud = ubicacion.getLatitude();
            double longitud = ubicacion.getLongitude();
            LatLng coordenadas = new LatLng(latitud, longitud);
            if (marcadorUsuario != null) {
                marcadorUsuario.remove();
            }
            marcadorUsuario = mMap.addMarker(new MarkerOptions()
                    .position(coordenadas)
                    .title("Yo")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.iconito)));
        }
    }

    // Checking if Google Play Services Available or not
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public void cargarRutasEnSpinner() {
        RutaDAO rutaDAO = new RutaDAO();
        List<String> listaRutas = rutaDAO.obtenerTodasLasIDRutas();
        // Se necesita crear un adaptador
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listaRutas);
        // Se le da el estilo con un radio button
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Se vincula el adaptador con el spinner
        spinner.setAdapter(adaptador);
    }

    // Se ejecuta cuando se selecciona una ruta del spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        actualizarMapa();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void dibujarRuta() {
        RutaDAO rutaDAO = new RutaDAO();
        String id_ruta = spinner.getSelectedItem().toString();
        // Obtener la polilinea codificada
        String encodedPolyline = rutaDAO.obtenerPolilinea(id_ruta);
        // Crear el objeto para agregar la polilinea
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(5);
        polylineOptions.geodesic(true);
        // Agregar la polilinea decodificada con PolyUtil.decode()
        polylineOptions.addAll(PolyUtil.decode(encodedPolyline));
        if(line != null) {
            line.remove();
        }
        line = mMap.addPolyline(polylineOptions);
        line.setVisible(true);
    }

    public void mostrarCamiones() {
        CamionDAO camionDAO = new CamionDAO();
        String id_ruta = spinner.getSelectedItem().toString();
        List<Camion> camiones = camionDAO.obtenerCamionesDeLaRuta(id_ruta);
        Marker m[] = new Marker[camiones.size()];
        LatLng coordenadas;
        Point punto;
        for (int i = 0; i < camiones.size(); i++) {
            punto = (Point) camiones.get(i).getGeom().getGeometry();
            coordenadas = new LatLng(punto.x, punto.y);
            mMap.addMarker(new MarkerOptions()
                    .position(coordenadas)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marcador_camion)));
        }
    }

    public void actualizarMapa() {
        mMap.clear();
        // Obtener la ubicacion del usuario y de los camiones si hay cambios
        mostrarUbicacionUsuario(obtenerUbicacionUsuario());
        mostrarCamiones();
        dibujarRuta();
    }

    public void configurarActualizacionesDePosicion() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Se configuran las actualizaciones de posiciones a cada 5segundos o 1 metro
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, this);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        configurarActualizacionesDePosicion();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        actualizarMapa();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}