package com.busme_usuario.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
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
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.busme_usuario.R;
import com.busme_usuario.controladores.Pintor;
import com.busme_usuario.interfaces.RetrofitMaps;
import com.busme_usuario.modelos.DAO.RutaDAO;
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

import android.widget.Switch;
import android.widget.Toast;

import com.busme_usuario.modelos.POJO.Example;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.PolyUtil;

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
    Marker marcadorEnRuta;
    LocationManager locationManager;
    Spinner spinner;
    LatLng origin;
    LatLng dest;

    Switch switchRuta;
    int actualizacion = 0;
    boolean polilinea1 = true;


    private static Polyline line;


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
        cargarRutasEnSpinner();
        spinner.setOnItemSelectedListener(this);
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
                if(isChecked){
                    polilinea1=true;

                }else{
                    polilinea1=false;
                }
            }
        });
        // Se muestra la ubicacion en el mapa

        new Pintor(mMap, id_ruta, marcadorUsuario,line,ubicacionUsuario, polilinea1).execute();
        //mostrarUbicacionUsuario(ubicacionUsuario);
        //mostrarCamiones();

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

        /*
        Evento que se llama cuando se le da click al mapa,
        para agregar un marcador
         */

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                float zoom = mMap.getCameraPosition().zoom;
                int tolerancia = 10;
                // Dependiendo del zoom se ajusta el nivel de tolerancia de eror
                if(zoom >= 10 && zoom <= 15) {
                    tolerancia = 50;
                } else if(zoom > 15 && zoom <= 17){
                    tolerancia = 30;
                } else  if (zoom > 17){
                    tolerancia = 15;
                }
                if(PolyUtil.isLocationOnPath(point, line.getPoints(), true, tolerancia)) {
                    if(marcadorEnRuta != null) {
                        marcadorEnRuta.remove();
                    }
                    marcadorEnRuta = mMap.addMarker(new MarkerOptions()
                            .position(point));
                            //.title("Yo")
                            //.icon(BitmapDescriptorFactory.fromResource(R.mipmap.iconito)));
                }
            }
        });

        /*Button btnDriving = (Button) findViewById(R.id.btnCalcularTiempo);
        btnDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                build_retrofit_and_get_response("driving");
            }
        });*/
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

    public void configurarActualizacionesDePosicion() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
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
        new Pintor(mMap, id_ruta, marcadorUsuario,line,ubicacionUsuario, polilinea1).execute();
        actualizacion++;
        Log.i("DEBUG", "Actualizacion #" + actualizacion);
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

    public static void setLine(Polyline line) {
        BusMeUsuario.line = line;
    }
}