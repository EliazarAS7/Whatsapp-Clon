package org.example.whatsapp2_;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClienteController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;

    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 12345;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Por favor, llena todos los campos.");
            return;
        }

        loginButton.setDisable(true);
        statusLabel.setText("Conectando...");

        new Thread(() -> {
            boolean loginSuccess = iniciarSesion(username, password);
            javafx.application.Platform.runLater(() -> {
                loginButton.setDisable(false);
                if (loginSuccess) {
                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("Inicio de sesión exitoso.");
                    cambiarAVistaContactos(username); // Pasa a la vista de contactos
                } else {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("Usuario o contraseña incorrectos.");
                }
            });
        }).start();
    }

    private boolean iniciarSesion(String username, String password) {
        try (Socket socket = new Socket(SERVIDOR, PUERTO);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            oos.writeObject("LOGIN");
            oos.writeObject(username);
            oos.writeObject(password);

            String respuesta = (String) ois.readObject();
            return "Inicio de sesión exitoso".equals(respuesta);
        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> statusLabel.setText("Error de conexión: " + e.getMessage()));
            return false;
        }
    }

    private void cambiarAVistaContactos(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Contacts.fxml"));
            Parent root = loader.load();

            // Pasar el usuario a la vista de contactos
            ContactsController contactsController = loader.getController();
            contactsController.setUsuarioActual(username);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Lista de Contactos");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
