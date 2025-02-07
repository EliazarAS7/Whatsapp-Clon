package org.example.whatsapp2_;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ChatController {
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;

    private String usuarioActual;
    private String contacto;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    // Método para inicializar el chat con usuario y contacto
    public void iniciarChat(String usuarioActual, String contacto) {
        this.usuarioActual = usuarioActual;
        this.contacto = contacto;
        conectarServidor();
        cargarMensajes();
        escucharMensajes();
    }

    private void conectarServidor() {
        try {
            if (socket == null || socket.isClosed()) {
                System.out.println("🔄 Intentando conectar al servidor...");
                socket = new Socket("127.0.0.1", 12345);
                System.out.println("✅ Conexión establecida.");

                oos = new ObjectOutputStream(socket.getOutputStream());
                System.out.println("✅ ObjectOutputStream creado correctamente.");

                ois = new ObjectInputStream(socket.getInputStream());
                System.out.println("✅ ObjectInputStream creado correctamente.");
            }
        } catch (IOException e) {
            chatArea.appendText("❌ Error: No se pudo conectar con el servidor.\n");
            System.out.println("❌ No se pudo conectar con el servidor.");
            e.printStackTrace();
        }
    }





    @FXML
    private void enviarMensaje() {
        String mensaje = messageField.getText().trim();
        if (!mensaje.isEmpty()) {
            try {
                if (socket == null || socket.isClosed()) {
                    chatArea.appendText("❌ Error: No hay conexión con el servidor.\n");
                    System.out.println("Socket está cerrado o nulo.");
                    return;
                }
                if (oos == null) {
                    chatArea.appendText("❌ Error: No se pudo enviar el mensaje.\n");
                    System.out.println("ObjectOutputStream es nulo.");
                    return;
                }

                System.out.println("✅ Enviando mensaje: " + mensaje);
                System.out.println("Usuario actual: " + usuarioActual);
                System.out.println("Contacto: " + contacto);

                oos.writeObject("SEND_MESSAGE");
                System.out.println("📨 Enviando comando SEND_MESSAGE al servidor.");

                oos.writeObject(usuarioActual);
                System.out.println("📨 Enviando usuario: " + usuarioActual);

                oos.writeObject(contacto);
                System.out.println("📨 Enviando contacto: " + contacto);

                oos.writeObject(mensaje);
                System.out.println("📨 Enviando mensaje: " + mensaje);

                oos.flush();

                System.out.println("✅ Mensaje enviado correctamente.");

                chatArea.appendText("Yo: " + mensaje + "\n");
                messageField.clear();
            } catch (IOException e) {
                chatArea.appendText("❌ Error al enviar el mensaje. Verifica la conexión.\n");
                System.out.println("❌ Error al enviar el mensaje.");
                e.printStackTrace();
            }
        }
    }






    private void cargarMensajes() {
        try {
            oos.writeObject("GET_MESSAGES");
            oos.writeObject(usuarioActual);
            oos.writeObject(contacto);
            oos.flush();

            List<String> mensajes = (List<String>) ois.readObject();
            chatArea.clear();
            for (String msg : mensajes) {
                chatArea.appendText(msg + "\n");
            }
        } catch (IOException | ClassNotFoundException e) {
            chatArea.appendText("Error al cargar mensajes.\n");
            e.printStackTrace();
        }
    }

    private void escucharMensajes() {
        new Thread(() -> {
            try {
                while (true) {
                    String mensaje = (String) ois.readObject();
                    Platform.runLater(() -> chatArea.appendText(mensaje + "\n"));
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Desconectado del servidor.");
            }
        }).start();
    }

    public void cerrarConexion() {
        try {
            if (oos != null) oos.close();
            if (ois != null) ois.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
