package views;

import controllers.databaseController;
import models.Database;
import models.User;

import javax.sql.rowset.CachedRowSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public class lessonsPanel extends JFrame {
    DefaultTableModel lessonsTableModel;
    private JPanel lessonsPanel;
    private JTextField lessonNameTextField;
    private JButton addButton;
    private JComboBox<String> professionComboBox;
    private JComboBox<String> schoolYearComboBox;
    private JButton backButton;
    private JTable lessonsTable;
    private JScrollPane scrollPane;
    private JButton editButton;
    private JButton removeButton;
    private JButton cancelButton;

    public lessonsPanel() {
        add(lessonsPanel);
        setSize(1280, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // Customize table to have no border, disable cell editing and switch single row selection
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        lessonsTable.setDefaultEditor(Object.class, null);
        lessonsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Center combobox text
        ((JLabel)professionComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        ((JLabel)schoolYearComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // Get all distinct subjects from teachers
        try {
            CachedRowSet subjects = databaseController.selectQuery("SELECT DISTINCT(subject) FROM \"Teachers\"");
            while (subjects.next())
                professionComboBox.addItem(subjects.getString("subject"));
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        }

        for (String schoolYear : Arrays.asList("1η Γυμνασίου", "2α Γυμνασίου", "3η Γυμνασίου", "1η Λυκείου", "2α Λυκείου", "3η Λυκείου")) {
            schoolYearComboBox.addItem(schoolYear);
        }

        backButton.addActionListener(action -> {
            // TODO: (IceDBorn) Close this panel and open the main panel
        });

        addButton.addActionListener(action -> {
            // TODO: (Prionysis) Update this listener to edit existing entries too
            String lesson = lessonNameTextField.getText();

            // Check if the text field is blank to avoid unnecessary sql errors
            if (lesson.equals("")) {
                System.out.println("You can not insert a blank name");
            } else {
                String subject = professionComboBox.getItemAt(professionComboBox.getSelectedIndex());
                int year = schoolYearComboBox.getSelectedIndex() + 1;

                try {
                    Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO \"Lessons\"(name, subject, year) VALUES (?, ?, ?)");

                    preparedStatement.setString(1, lesson);
                    preparedStatement.setString(2, subject);
                    preparedStatement.setInt(3, year);
                    preparedStatement.executeUpdate();

                    // Get the lessonId of the newly inserted classroom
                    int lessonId = databaseController.getInsertedRowId(preparedStatement.getGeneratedKeys());

                    preparedStatement.close();
                    connection.close();

                    System.out.printf("userId %d created lesson: %d (name: %s, subject: %s, year: %d)%n", User.getId(), lessonId, lesson, subject, year);
                } catch (SQLException err) {
                    System.out.println("SQL Exception:");
                    err.printStackTrace();
                }
            }
        });

        cancelButton.addActionListener(action -> {
            revertUIComponents();
        });

        editButton.addActionListener(action -> {
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            addButton.setText("Save");
            lessonsTable.setEnabled(false);
            lessonNameTextField.setText(lessonsTable.getValueAt(lessonsTable.getSelectedRow(), 0).toString());
            professionComboBox.setSelectedItem(lessonsTable.getValueAt(lessonsTable.getSelectedRow(), 1));
            schoolYearComboBox.setSelectedItem(lessonsTable.getValueAt(lessonsTable.getSelectedRow(), 2));
            lessonsTable.clearSelection();
        });

        removeButton.addActionListener(action -> {
            // TODO: (Prionysis) Remove entry from database
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            lessonsTable.getSelectionModel().clearSelection();
        });

        // Listen for changes in the lesson name text
        lessonNameTextField.getDocument().addDocumentListener(new DocumentListener() {
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

        lessonsTable.getSelectionModel().addListSelectionListener(selection -> {
            if (lessonsTable.getSelectedRow() != -1
                    && !lessonsTable.getValueAt(lessonsTable.getSelectedRow(), 0).toString().equals("")
                    && !lessonsTable.getValueAt(lessonsTable.getSelectedRow(), 1).toString().equals("")) {
                editButton.setEnabled(true);
                removeButton.setEnabled(true);
            } else {
                editButton.setEnabled(false);
                removeButton.setEnabled(false);
            }
        });
    }

    private void createUIComponents() {
        // Add columns
        String[] lessonsTableColumns = {"Name", "Profession", "School Year"};
        lessonsTableModel = new DefaultTableModel(lessonsTableColumns, 0);
        lessonsTable = new JTable(lessonsTableModel);
        // Stop users from interacting with the table
        lessonsTable.getTableHeader().setReorderingAllowed(false);
        updateLessons();
    }

    private void revertUIComponents() {
        addButton.setEnabled(false);
        cancelButton.setEnabled(false);
        lessonNameTextField.setText("");
        lessonsTable.setEnabled(true);
        addButton.setText("Add");

        if (professionComboBox.getItemCount() > 1) {
            professionComboBox.setSelectedIndex(1);
        } else {
            professionComboBox.setSelectedIndex(0);
        }

        if (schoolYearComboBox.getItemCount() > 1) {
            schoolYearComboBox.setSelectedIndex(1);
        } else {
            schoolYearComboBox.setSelectedIndex(0);
        }
    }

    private void enableButtons() {
        // Enable or disable buttons based on class name text
        if (!lessonNameTextField.getText().equals("")) {
            addButton.setEnabled(true);
            cancelButton.setEnabled(true);
        } else {
            addButton.setEnabled(false);
            cancelButton.setEnabled(false);
        }
    }

    private void updateLessons() {
        // TODO: (Prionysis) Update table with lessons from the database
        Object[] row = new Object[3];
        row[0] = "Mathematics";
        row[1] = "ne";
        row[2] = "2α Γυμνασίου";
        lessonsTableModel.addRow(row);
        row[0] = "Physics";
        row[1] = "ne";
        row[2] = "3η Γυμνασίου";
        lessonsTableModel.addRow(row);
        lessonsTable.setModel(lessonsTableModel);
    }
}
