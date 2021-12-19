package views;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class addNewEntryPanel extends JFrame{
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

        if (isProfession) {
            messageLabel.setText("Add new profession");
        } else {
            messageLabel.setText("Add new school year");
        }

        cancelButton.addActionListener(action -> {
            this.dispose();
        });

        addButton.addActionListener(action -> {
            // TODO: (Prionysis) Add entry to database
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
