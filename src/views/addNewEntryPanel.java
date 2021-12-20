package views;

import controllers.databaseController;
import controllers.fileController;
import controllers.panelController;
import models.Database;
import models.User;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class addNewEntryPanel extends JDialog {
    private JPanel addNewEntryPanel;
    private JLabel messageLabel;
    private JTextField entryTextField;
    private JButton addButton;
    private JButton cancelButton;

    public addNewEntryPanel(boolean isProfession) {
        add(addNewEntryPanel);
        setSize(400, 200);
        setResizable(false);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // Set modality type to block interaction with the main window, until this one is closed
        setModalityType(ModalityType.APPLICATION_MODAL);

        if (isProfession)
            messageLabel.setText("Add new profession");
        else
            messageLabel.setText("Add new school year");

        cancelButton.addActionListener(action -> this.dispose());

        addButton.addActionListener(action -> {
            try {
                Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());

                String name = entryTextField.getText();
                boolean entryExists = databaseController.selectQuery(String.format(isProfession ?
                        "SELECT id FROM \"Professions\" WHERE name = '%s'" : "SELECT id FROM \"Years\" WHERE name = '%s'", name)).isBeforeFirst();

                if (entryExists)
                    panelController.createErrorPanel("A %s already exists with that name.".formatted(isProfession ? "profession" : "year"), this);
                else {
                    PreparedStatement preparedStatement = connection.prepareStatement(isProfession ?
                            "INSERT INTO \"Professions\"(name) VALUES (?)" : "INSERT INTO \"Years\"(name) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
                    preparedStatement.setString(1, name);
                    preparedStatement.executeUpdate();

                    // Get the id of the inserted entry
                    int id = databaseController.getInsertedRowId(preparedStatement.getGeneratedKeys());

                    preparedStatement.close();
                    connection.close();

                    fileController.saveFile("User (%d) %s created %s (%d) %s.".formatted(
                            User.getId(), User.getName(), isProfession ? "profession " : "school year ", id, name));
                }
            } catch (SQLException err) {
                StringWriter errors = new StringWriter();
                err.printStackTrace(new PrintWriter(errors));
                String message = errors.toString();
                try {
                    fileController.saveFile("SQL Exception: " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                panelController.createErrorPanel("Something went wrong.", this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.dispose();
        });

        // Listen for changes in the lesson name text
        entryTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                enableButtons();
            }

            public void removeUpdate(DocumentEvent e) {
                enableButtons();
            }

            public void insertUpdate(DocumentEvent e) {
                enableButtons();
            }
        });
    }

    private void enableButtons() {
        addButton.setEnabled(!entryTextField.getText().equals(""));
    }
}
