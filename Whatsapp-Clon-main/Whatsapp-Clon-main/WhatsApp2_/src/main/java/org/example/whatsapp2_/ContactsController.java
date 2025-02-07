package org.example.whatsapp2_;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ContactsController {

    @FXML private ListView<String> contactsListView;
    @FXML private Label userLabel;

    private String usuarioActual;

    public void setUsuarioActual(String username) {
        this.usuarioActual = username;
        userLabel.setText("Bienvenido, " + username);

        // Obtener contactos de la base de datos
        List<String> contactos = obtenerContactos(usuarioActual);

        // Mostrar los contactos en la ListView
        contactsListView.getItems().clear();
        contactsListView.getItems().addAll(contactos);

        // Agregar un evento de clic al ListView
        contactsListView.setOnMouseClicked(this::handleContactClick);
    }

    // Método que obtiene los contactos de la base de datos
    private List<String> obtenerContactos(String usuarioActual) {
        List<String> contactos = new ArrayList<>();
        String url = "jdbc:mysql://localhost:3306/whatsapp2"; // Cambia la URL si es necesario
        String user = "root"; // Tu usuario de la base de datos
        String password = ""; // Tu contraseña de la base de datos

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            // SQL para obtener los contactos (usuarios diferentes al actual)
            String query = "SELECT username FROM usuarios WHERE username != '" + usuarioActual + "'";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String contacto = rs.getString("username");
                contactos.add(contacto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return contactos;
    }

    // Método que se llama al hacer clic en un contacto
    private void handleContactClick(MouseEvent event) {
        if (event.getClickCount() == 2) { // Doble clic
            String selectedContact = contactsListView.getSelectionModel().getSelectedItem();
            if (selectedContact != null) {
                abrirVentanaChat(selectedContact);
            }
        }
    }

    // Método para abrir la ventana de chat
    private void abrirVentanaChat(String contacto) {
        try {
            // Cargar el FXML para la ventana de chat
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Chat.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del chat y pasar el usuario y contacto
            ChatController chatController = loader.getController();
            chatController.iniciarChat(usuarioActual, contacto);

            // Mostrar la ventana de chat
            Stage chatStage = new Stage();
            chatStage.setScene(new Scene(root));
            chatStage.setTitle("Chat con " + contacto);
            chatStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
