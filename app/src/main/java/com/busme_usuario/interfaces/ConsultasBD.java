package com.busme_usuario.interfaces;

import java.util.List;

public interface ConsultasBD<TypeOfObject> {
    boolean create(TypeOfObject t);
    boolean delete(Object key);
    boolean update(TypeOfObject t);
    TypeOfObject read(Object key);
    List<TypeOfObject> readAll();
}
