package org.example.whatsapp2_;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private static final int PUERTO = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado, esperando conexiones...");

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado...");

                new Thread(() -> manejarCliente(clienteSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void manejarCliente(Socket clienteSocket) {
        try (
                ObjectInputStream ois = new ObjectInputStream(clienteSocket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(clienteSocket.getOutputStream())
        ) {
            String requestType = (String) ois.readObject();

            if ("LOGIN".equals(requestType)) {
                String username = (String) ois.readObject();
                String password = (String) ois.readObject();
                boolean loginExitoso = validarCredenciales(username, password);
                oos.writeObject(loginExitoso ? "Inicio de sesión exitoso" : "Credenciales inválidas");
            } else if ("GET_CONTACTS".equals(requestType)) {
                List<String> contactos = obtenerListaUsuarios();
                oos.writeObject(contactos);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static boolean validarCredenciales(String username, String password) {
        String query = "SELECT * FROM usuarios WHERE username = ? AND contraseña = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static List<String> obtenerListaUsuarios() {
        List<String> usuarios = new ArrayList<>();
        String query = "SELECT username FROM usuarios";

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                usuarios.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarios;
    }
}
