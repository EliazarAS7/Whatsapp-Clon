package org.example.whatsapp2_;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

public class ChatController {

    @FXML private TextArea chatArea;
    @FXML private TextField messageField;
    @FXML private Label chatHeader;

    private String usuarioActual;
    private String contacto;

    // Método para iniciar el chat
    public void iniciarChat(String usuarioActual, String contacto) {
        this.usuarioActual = usuarioActual;
        this.contacto = contacto;
        chatHeader.setText("Chat con " + contacto);
    }

    // Método para enviar un mensaje
    @FXML
    private void enviarMensaje() {
        String mensaje = messageField.getText();
        if (!mensaje.isEmpty()) {
            chatArea.appendText(usuarioActual + ": " + mensaje + "\n");
            messageField.clear();

            // Aquí podrías agregar lógica para enviar el mensaje a través de la red
        }
    }
}
