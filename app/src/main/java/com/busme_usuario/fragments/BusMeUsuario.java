package com.busme_usuario.fragments;

import android.Manifest;
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
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.busme_usuario.R;
import com.busme_usuario.controladores.Pintor;
import com.busme_usuario.modelos.DAO.RutaDAO;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class BusMeUsuario extends FragmentActivity implements OnMapReadyCallback, Spinner.OnItemSelectedListener, LocationListener {

    static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    GoogleMap mMap;
    LocationManager locationManager;
    Spinner spinner;
    private Location ubicacionCamion;
    private Location ubicacionEstacion;
    private static Marker marcadorEnRuta;
    private static Polyline line;
    private static Marker marcadorUsuario;
    private static List<Marker> marcadoresDeCamiones;
    Switch switchRuta;
    String recorriendo;
    TextView txtTiempoEstimado;
    private boolean estimandoLlegadaCamion;
    String TAG = "DEBUG";
    String color = "rojo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recorriendo = "polilinea1";
        ubicacionCamion = new Location("");
        ubicacionEstacion = new Location("");
        marcadoresDeCamiones = new ArrayList<>();
        estimandoLlegadaCamion = false;
        setContentView(R.layout.activity_bus_me_usuario);
        //show error dialog if Google Play Services not available
        if (!isGooglePlayServicesAvailable()) {
            Log.d("onCreate", "Google Play Services not available. Ending Test case.");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services available. Continuing.");
        }
        revisarPermisoDeUbicacion();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        configurarActualizacionesDePosicion();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Se obtiene el spinner
        spinner = (Spinner) findViewById(R.id.spinnerRutas);
        cargarRutasEnSpinner();
        spinner.setOnItemSelectedListener(this);
        txtTiempoEstimado = (TextView) findViewById(R.id.txtTiempoEstimado);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String id_ruta = spinner.getSelectedItem().toString();
        // Se obtiene la ubicacion del usuario
        Location ubicacionUsuario = obtenerUbicacionUsuario();
        switchRuta = (Switch) findViewById(R.id.switchRuta);
        switchRuta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    recorriendo = "polilinea2";
                    color = "azul";
                } else {
                    recorriendo = "polilinea1";
                    color = "rojo";
                }
                if (marcadorEnRuta != null) {
                    marcadorEnRuta.remove();
                    marcadorEnRuta = null;
                }
                // Se necesita quitar el texto si esta visible, y detener lo de estimando
                txtTiempoEstimado.setVisibility(View.INVISIBLE);
                estimandoLlegadaCamion = false;
            }
        });
        // Se muestra la ubicacion en el mapa
        new Pintor(mMap, id_ruta, ubicacionUsuario, recorriendo, color).execute();
        // Hacer zoom en la ubicacion
        enfocarEnUbicacion(ubicacionUsuario);

        /*
        Evento que se llama cuando se le da click al mapa,
        para agregar un marcador
         */

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String advertencia = "";
                if (marcadorEnRuta != null) {
                    if (marker.getTitle().equals("Camion")) {
                        estimandoLlegadaCamion = true;
                        // Obtener la ubicacion del camion
                        ubicacionCamion.setLatitude(marker.getPosition().latitude);
                        ubicacionCamion.setLongitude(marker.getPosition().longitude);
                        // Obtener la ubicacion del punto seleccionado en la ruta
                        ubicacionEstacion.setLatitude(marcadorEnRuta.getPosition().latitude);
                        ubicacionEstacion.setLongitude(marcadorEnRuta.getPosition().longitude);
                        estimarTiempoLlegadaCamion();
                        return false;
                    } else {
                        advertencia = "Selecciona un camion";
                    }
                } else {
                    advertencia = "Selecciona un punto en la ruta";
                }
                txtTiempoEstimado.setVisibility(View.INVISIBLE);
                estimandoLlegadaCamion = false;
                Toast.makeText(getApplicationContext(), advertencia, Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                float zoom = mMap.getCameraPosition().zoom;
                int tolerancia = 10;
                // Dependiendo del zoom se ajusta el nivel de tolerancia de eror
                if (zoom >= 10 && zoom <= 13) {
                    tolerancia = 70;
                } else if (zoom > 13 && zoom <= 15) {
                    tolerancia = 50;
                } else if (zoom > 15 && zoom <= 17) {
                    tolerancia = 30;
                } else if (zoom > 17) {
                    tolerancia = 15;
                }
                // Si el punto se encuentra cerca de la ruta
                if (PolyUtil.isLocationOnPath(point, line.getPoints(), true, tolerancia)) {
                    if (marcadorEnRuta != null) {
                        marcadorEnRuta.remove();
                    }
                    marcadorEnRuta = mMap.addMarker(new MarkerOptions()
                            .title("Parada")
                            .position(point));
                }
            }
        });

    }

    private void estimarTiempoLlegadaCamion() {
        /*
         Conseguir la distancia entre los dos puntos en metros
         y convertirla a kilometros
        */
        if (marcadorEnRuta != null && ubicacionCamion != null) {
            double distanciaEnKilometros = ubicacionEstacion.distanceTo(ubicacionCamion) / 1000;
            if (distanciaEnKilometros <= 0.3) {
                Toast.makeText(getApplicationContext(), "El camion está por llegar", Toast.LENGTH_SHORT).show();
                txtTiempoEstimado.setVisibility(View.INVISIBLE);
            } else {
                int tolerancia = 0;
                int LIMITE_DE_VELOCIDAD = 50; // 50 km/s por reglamento
                // Se estima un error o tolerancia en minutos dependiendo de la distancia
                if (distanciaEnKilometros <= 0.7) {
                    tolerancia = 3;
                } else if (distanciaEnKilometros > 0.7 && distanciaEnKilometros <= 1.5) {
                    tolerancia = 6;
                } else if (distanciaEnKilometros > 1.5 && distanciaEnKilometros <= 2.6) {
                    tolerancia = 10;
                } else if (distanciaEnKilometros > 2.6 && distanciaEnKilometros <= 5) {
                    tolerancia = 13;
                } else if (distanciaEnKilometros > 5 && distanciaEnKilometros <= 8) {
                    tolerancia = 20;
                } else if (distanciaEnKilometros > 8 && distanciaEnKilometros <= 12) {
                    tolerancia = 33;
                } else if (distanciaEnKilometros > 12 && distanciaEnKilometros <= 19) {
                    tolerancia = 36;
                } else {
                    tolerancia = 40;
                }
                // El tiempo se calcula en horas, por lo que se convierte a minutos y se aplica una tolerancia
                int tiempoDeLlegadaEstimado = (int) Math.ceil((distanciaEnKilometros / LIMITE_DE_VELOCIDAD) * 60) + tolerancia;
                /*Log.d("DEBUG", "Distancia " + ubicacionEstacion.distanceTo(ubicacionCamion));
                Log.d("DEBUG", "Tiempo: " + tiempoDeLlegadaEstimado);
                */
                txtTiempoEstimado.setVisibility(View.VISIBLE);
                txtTiempoEstimado.setText("Tiempo de llegada: " + String.valueOf(tiempoDeLlegadaEstimado) + " minutos");
                txtTiempoEstimado.setBackgroundColor(Color.WHITE);
            }

        }
    }

    private Location obtenerUbicacionUsuario() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        // Se obtiene la posicion del usuario
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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

    public boolean revisarPermisoDeUbicacion() {
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

    public void configurarActualizacionesDePosicion() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            revisarPermisoDeUbicacion();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // Se configuran las actualizaciones de posiciones a cada 5segundos o 1 metro
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
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

    private void actualizarMapa() {
        String id_ruta = spinner.getSelectedItem().toString();
        Location ubicacionUsuario = obtenerUbicacionUsuario();
        new Pintor(mMap, id_ruta, ubicacionUsuario, recorriendo, color).execute();
        if (estimandoLlegadaCamion) {
            estimarTiempoLlegadaCamion();
        }
    }

    private void enfocarEnUbicacion(Location ubicacion) {
        double latitud = ubicacion.getLatitude();
        double longitud = ubicacion.getLongitude();
        /*
        Se obtienen las coordenadas para hacer zoom a la posicion en el mapa
        */
        LatLng coordenadas = new LatLng(latitud, longitud);
        // Define en donde va a hacer zoom y a que nivel
        CameraUpdate actualizacionDeCamara = CameraUpdateFactory.newLatLngZoom(coordenadas, 16);
        // Se hace el zoom en el mapa
        mMap.animateCamera(actualizacionDeCamara);
    }

    // Se ejecuta cuando se selecciona una ruta del spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        actualizarMapa();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
            revisarPermisoDeUbicacion();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    public static Polyline getLine() {
        return line;
    }

    public static void setLine(Polyline line) {
        BusMeUsuario.line = line;
    }

    public static Marker getMarcadorEnRuta() {
        return marcadorEnRuta;
    }

    public static void setMarcadorEnRuta(Marker marcadorEnRuta) {
        BusMeUsuario.marcadorEnRuta = marcadorEnRuta;
    }

    public static Marker getMarcadorUsuario() {
        return marcadorUsuario;
    }

    public static void setMarcadorUsuario(Marker marcadorUsuario) {
        BusMeUsuario.marcadorUsuario = marcadorUsuario;
    }

    public static List<Marker> getMarcadoresDeCamiones() {
        return marcadoresDeCamiones;
    }

    public static void setMarcadoresDeCamiones(List<Marker> marcadoresDeCamiones) {
        BusMeUsuario.marcadoresDeCamiones = marcadoresDeCamiones;
    }
}