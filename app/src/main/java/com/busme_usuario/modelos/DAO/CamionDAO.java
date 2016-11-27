package com.busme_usuario.modelos.DAO;

import android.util.Log;

import com.busme_usuario.interfaces.ConsultasBD;
import com.busme_usuario.modelos.ConexionBD;
import com.busme_usuario.modelos.DTO.Camion;
import com.google.maps.android.geometry.Point;

import org.postgis.PGgeometry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CamionDAO implements ConsultasBD<Camion> {
    private static final String SQL_INSERT = "INSERT INTO camiones(id_unidad, id_ruta, capacidad_max, asientos_disponibles, geom) VALUES(?, ?, ?, ?, ?)";
    private static final String SQL_DELETE = "DELETE FROM camiones WHERE id_unidad = ?";
    private static final String SQL_UPDATE = "UPDATE camiones SET id_ruta = ?, capacidad_max = ?, asientos_disponibles = ?, geom = ? WHERE id_unidad = ?";
    private static final String SQL_READ = "SELECT * FROM camiones WHERE id_unidad = ?";
    private static final String SQL_READALL = "SELECT * FROM camiones";
    private static final String SQL_OBTENERCAMIONESDELARUTA = "SELECT * FROM camiones WHERE id_ruta = ?;";
    private static final String SQL_OBTENERPUNTO = "SELECT ST_ASTEXT(GEOM) FROM camiones WHERE id_unidad = ?";
    private static final ConexionBD conexion = ConexionBD.connect();

    @Override
    public boolean create(Camion t) {
        PreparedStatement ps;
        try {
            ps = conexion.getConexion().prepareStatement(SQL_INSERT);
            ps.setString(1, t.getIdRuta());
            ps.setString(2, t.getIdRuta());
            ps.setInt(3, t.getCapacidadMaxima());
            ps.setInt(4, t.getAsientosDisponibles());
            ps.setObject(5, t.getGeom());
            if(ps.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conexion.cerrarConexion();
        }
        return false;
    }

    @Override
    public boolean delete(Object key) {
        PreparedStatement ps;
        try {
            ps = conexion.getConexion().prepareStatement(SQL_DELETE);
            ps.setString(1, key.toString());
            if(ps.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conexion.cerrarConexion();
        }
        return false;
    }

    @Override
    public boolean update(Camion t) {
        PreparedStatement ps;
        try {
            ps = conexion.getConexion().prepareStatement(SQL_UPDATE);
            ps.setString(1, t.getIdRuta());
            ps.setInt(2, t.getCapacidadMaxima());
            ps.setInt(3, t.getAsientosDisponibles());
            ps.setObject(4, t.getGeom());
            ps.setString(5, t.getIdCamion());
            if(ps.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conexion.cerrarConexion();
        }
        return false;
    }

    @Override
    public Camion read(Object key) {
        PreparedStatement ps;
        ResultSet rs;
        Camion camion = null;
        try {
            ps = conexion.getConexion().prepareStatement(SQL_READ);
            ps.setString(1, key.toString());
            rs = ps.executeQuery();
            while(rs.next()) {
                camion = new Camion(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4), (PGgeometry)rs.getObject(5));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conexion.cerrarConexion();
        }
        return camion;
    }

    @Override
    public List<Camion> readAll() {
        PreparedStatement ps;
        ResultSet rs;
        ArrayList<Camion> camiones = new ArrayList<>();
        try {
            ps = conexion.getConexion().prepareStatement(SQL_READALL);
            rs = ps.executeQuery();
            while(rs.next()) {
                camiones.add(new Camion(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4), (PGgeometry)rs.getObject(5)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conexion.cerrarConexion();
        }
        return camiones;
    }

    public List<Camion> obtenerCamionesDeLaRuta(Object key) {
        PreparedStatement ps;
        ResultSet rs;
        ArrayList<Camion> camiones = new ArrayList<>();
        try {
            ps = conexion.getConexion().prepareStatement(SQL_OBTENERCAMIONESDELARUTA);
            ps.setString(1, key.toString());
            rs = ps.executeQuery();
            while(rs.next()) {
                camiones.add(new Camion(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4), (PGgeometry)rs.getObject(5)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conexion.cerrarConexion();
        }
        return camiones;
    }

    public Point obtenerPunto(Object key) {
        PreparedStatement ps;
        ResultSet rs;
        Point punto = null;
        try {
            ps = conexion.getConexion().prepareStatement(SQL_OBTENERPUNTO);
            ps.setString(1, key.toString());
            rs = ps.executeQuery();
            while(rs.next()) {
                Log.i("DEBUG", " ASDA " + rs.getObject(1));
                punto = (Point)rs.getObject(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conexion.cerrarConexion();
        }
        return punto;
    }
}
