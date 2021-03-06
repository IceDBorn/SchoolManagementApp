package views;

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
import controllers.databaseController;
import controllers.fileController;
import controllers.panelController;
import models.Database;
import models.User;

public class addNewEntryPanel extends JDialog {
    private JPanel addNewEntryPanel;
    private JLabel messageLabel;
    private JTextField entryTextField;
    private JButton addButton;
    private JButton cancelButton;

    public addNewEntryPanel(boolean isProfession) {
        add(addNewEntryPanel);
        setSize(400, 200);
        // Disable resizing of window
        setResizable(false);
        // Set location based on parent window
        setLocationRelativeTo(getParent());
        // Do not exit the application when closing this window
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // Set the application's image
        setIconImage(new ImageIcon("res/school.png").getImage());
        // Set modality type to block interaction with the main window, until this one is closed
        setModalityType(ModalityType.APPLICATION_MODAL);

        if (isProfession)
            messageLabel.setText("Add new profession");
        else
            messageLabel.setText("Add new school year");

        // Close this window by pressing cancel
        cancelButton.addActionListener(action -> this.dispose());

        // Add new profession to the database
        addButton.addActionListener(action -> {
            try {
                Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());

                String name = entryTextField.getText();
                boolean entryExists = databaseController.selectQuery(String.format(isProfession ?
                        "SELECT id FROM \"Professions\" WHERE name = '%s'" : "SELECT id FROM \"Years\" WHERE name = '%s'", name)).isBeforeFirst();

                if (entryExists)
                    panelController.createErrorPanel("A %s with that name already exists.".formatted(isProfession ? "profession" : "year"), this, isProfession ? 330 : 300);
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

                panelController.createErrorPanel("Something went wrong.", this, 220);
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
