package com.busme_usuario.modelos;

import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {
    /*
    * Pregunta si la instancia ya esta creada
    * para usar la misma y si no se crea
    * una nueva
    */
    private static ConexionBD instancia; // Singleton
    private Connection conexion;

    /*
     * Constructor privado
     * para no crear instancias desde afuera
     */
    private ConexionBD() {
        String url = "jdbc:postgresql://ec2-23-23-226-24.compute-1.amazonaws.com/d7naf0g01olcpi";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Properties props = new Properties();
        props.setProperty("user", "lhmukxzksrxdac");
        props.setProperty("password", "LD1-vOYp3VJ07QKfZ69UB0eXMm");
        props.setProperty("ssl", "true");
        props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
        try {
            Class.forName("org.postgresql.Driver");
            conexion = DriverManager.getConnection(url, props);
            //((org.postgresql.PGConnection)conexion).addDataType("geometry",Class.forName("org.postgis.PGgeometry"));
        } catch (SQLException e) {
            Log.i("DEBUG", e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.i("DEBUG", e.getMessage());
        } finally {
            //cerrarConexion();
        }
    }

    /*
     * Unica forma de crear una conexion y que aplica el Singleton.
     * La palabra synchronized hace una lista de espera para que
     * si hay muchos usuarios, estos esperen su turno hasta que termine
     * el usuario que lo está usando
     */
    public synchronized static ConexionBD connect() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    public Connection getConexion() {
        return conexion;
    }

    public void cerrarConexion() {
        instancia = null;
    }

}
