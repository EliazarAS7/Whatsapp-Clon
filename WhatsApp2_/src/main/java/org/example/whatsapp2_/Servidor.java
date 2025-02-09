package org.example.whatsapp2_;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private static final int PUERTO = 12345;

    private static void inicializarBaseDatos() {
        String crearUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "contraseña VARCHAR(50) NOT NULL)";

        String crearMensajes = "CREATE TABLE IF NOT EXISTS mensajes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "emisor VARCHAR(50) NOT NULL, " +
                "receptor VARCHAR(50) NOT NULL, " +
                "mensaje TEXT NOT NULL, " +
                "fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement()) {
            stmt.execute(crearUsuarios);
            stmt.execute(crearMensajes);
            System.out.println("Base de datos inicializada.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        inicializarBaseDatos(); // Inicializa la BD antes de aceptar conexiones
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


    /* private static void manejarCliente(Socket clienteSocket) {
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
    } */

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

    private static void guardarMensaje(String emisor, String receptor, String mensaje) {
        String query = "INSERT INTO mensajes (emisor, receptor, mensaje) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, emisor);
            stmt.setString(2, receptor);
            stmt.setString(3, mensaje);
            stmt.executeUpdate();
            System.out.println("Mensaje guardado en la base de datos.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static List<String> obtenerMensajes(String usuario1, String usuario2) {
        List<String> mensajes = new ArrayList<>();
        String query = "SELECT emisor, mensaje, fecha FROM mensajes " +
                "WHERE (emisor = ? AND receptor = ?) OR (emisor = ? AND receptor = ?) " +
                "ORDER BY fecha";

        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, usuario1);
            stmt.setString(2, usuario2);
            stmt.setString(3, usuario2);
            stmt.setString(4, usuario1);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                mensajes.add(rs.getString("fecha") + " | " + rs.getString("emisor") + ": " + rs.getString("mensaje"));
            }

            System.out.println("Mensajes recuperados: " + mensajes.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mensajes;
    }


    private static void manejarCliente(Socket clienteSocket) {
        try (
                ObjectInputStream ois = new ObjectInputStream(clienteSocket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(clienteSocket.getOutputStream())
        ) {
            System.out.println("🎯 Cliente manejado en un nuevo hilo.");

            while (true) { // Mantener la conexión activa
                String requestType = (String) ois.readObject();
                System.out.println("📩 Comando recibido: " + requestType);

                switch (requestType) {
                    case "LOGIN":
                        String username = (String) ois.readObject();
                        String password = (String) ois.readObject();
                        System.out.println("🔑 Intentando login para usuario: " + username);
                        boolean loginExitoso = validarCredenciales(username, password);
                        oos.writeObject(loginExitoso ? "Inicio de sesión exitoso" : "Credenciales inválidas");
                        break;

                    case "GET_CONTACTS":
                        System.out.println("📋 Enviando lista de contactos...");
                        List<String> contactos = obtenerListaUsuarios();
                        oos.writeObject(contactos);
                        break;

                    case "SEND_MESSAGE":
                        String emisor = (String) ois.readObject();
                        String receptor = (String) ois.readObject();
                        String mensaje = (String) ois.readObject();
                        System.out.println("📨 Mensaje recibido de " + emisor + " para " + receptor + ": " + mensaje);
                        guardarMensaje(emisor, receptor, mensaje);
                        oos.writeObject("Mensaje enviado");
                        break;

                    case "GET_MESSAGES":
                        String user1 = (String) ois.readObject();
                        String user2 = (String) ois.readObject();
                        System.out.println("🔄 Recuperando mensajes entre " + user1 + " y " + user2);
                        List<String> mensajes = obtenerMensajes(user1, user2);
                        oos.writeObject(mensajes);
                        break;

                    default:
                        System.out.println("⚠️ Comando desconocido recibido: " + requestType);
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("❌ Error en la comunicación con el cliente.");
            e.printStackTrace();
        }
    }


}
