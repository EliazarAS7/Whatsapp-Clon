package org.example.whatsapp2_;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/whatsapp2";
    private static final String USUARIO = "root"; // Cambia si es necesario
    private static final String CONTRASEÑA = "";  // Cambia si es necesario

    public static Connection obtenerConexion() {
        try {
            return DriverManager.getConnection(URL, USUARIO, CONTRASEÑA);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al conectar con la base de datos");
        }
    }
}

