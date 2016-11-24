package com.busme_usuario.modelos.DAO;

import android.util.Log;

import com.busme_usuario.interfaces.ConsultasBD;
import com.busme_usuario.modelos.ConexionBD;
import com.busme_usuario.modelos.DTO.Ruta;

import org.postgis.PGgeometry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RutaDAO implements ConsultasBD<Ruta> {
    private static final String SQL_INSERT = "INSERT INTO rutas(id_ruta, geom) VALUES (?, ?)";
    private static final String SQL_DELETE = "DELETE FROM rutas WHERE id_ruta = ?";
    private static final String SQL_UPDATE = "UPDATE rutas SET geom = ? WHERE id_ruta = ?";
    private static final String SQL_READ = "SELECT * FROM rutas WHERE id_ruta = ?";
    private static final String SQL_READALL = "SELECT * FROM rutas";
    private static final String SQL_READALLID = "SELECT id_ruta FROM rutas";
    private static final String SQL_OBTENERPOLILINEA = "SELECT ST_AsEncodedPolyline(rutas.geom) FROM rutas WHERE (rutas.id_ruta = ?);";
    private static final ConexionBD conexion = ConexionBD.connect();

    @Override
    public boolean create(Ruta t) {
        PreparedStatement ps;
        try {
            ps = conexion.getConexion().prepareStatement(SQL_INSERT);
            ps.setString(1, t.getIdRuta());
            ps.setObject(2, t.getGeom());
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
    public boolean update(Ruta t) {
        PreparedStatement ps;
        try {
            ps = conexion.getConexion().prepareStatement(SQL_UPDATE);
            ps.setObject(1, t.getGeom());
            ps.setString(2, t.getIdRuta());
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
    public Ruta read(Object key) {
        PreparedStatement ps;
        ResultSet rs;
        Ruta ruta = null;
        try {
            ps = conexion.getConexion().prepareStatement(SQL_READ);
            ps.setString(1, key.toString());
            rs = ps.executeQuery();
            while(rs.next()) {
                ruta = new Ruta(rs.getString(1), (PGgeometry) rs.getObject(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conexion.cerrarConexion();
        }
        return ruta;
    }

    @Override
    public List<Ruta> readAll() {
        PreparedStatement ps;
        ResultSet rs;
        ArrayList<Ruta> rutas = new ArrayList<>();
        try {
            ps = conexion.getConexion().prepareStatement(SQL_READALL);
            rs = ps.executeQuery();
            while(rs.next()) {
                rutas.add(new Ruta(rs.getString(1), (PGgeometry) rs.getObject(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conexion.cerrarConexion();
        }
        return rutas;
    }

    public List<String> obtenerTodasLasIDRutas() {
        PreparedStatement ps;
        ResultSet rs;
        ArrayList<String> rutas = new ArrayList<>();
        try {
            ps = conexion.getConexion().prepareStatement(SQL_READALLID);
            rs = ps.executeQuery();
            while(rs.next()) {
                rutas.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conexion.cerrarConexion();
        }
        return rutas;
    }

    public String obtenerPolilinea(Object key) {
        PreparedStatement ps;
        ResultSet rs;
        String polilinea = "";
        try {
            ps = conexion.getConexion().prepareStatement(SQL_OBTENERPOLILINEA);
            ps.setString(1, key.toString());
            rs = ps.executeQuery();
            while(rs.next()) {
                polilinea = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            conexion.cerrarConexion();
        }
        return polilinea;
    }

}