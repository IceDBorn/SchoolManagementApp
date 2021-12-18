package views;

import controllers.databaseController;
import controllers.panelController;
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
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.IntStream;

public class lessonsPanel extends JFrame {
    private final ArrayList<String> professionList;
    private final ArrayList<String> yearList;
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

    private int selectedId;
    private String selectedName;
    private String selectedProfession;

    public lessonsPanel() {
        this.professionList = new ArrayList<>();
        this.yearList = new ArrayList<>();

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
        ((JLabel) professionComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        ((JLabel) schoolYearComboBox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // Update professionList with distinct professions
        panelController.updateList("SELECT DISTINCT(subject) FROM \"Teachers\"", professionList);

        // Update yearList with year names
        panelController.updateList("SELECT name FROM \"Years\"", yearList);

        updateDetails();

        backButton.addActionListener(action -> {
            panelController.createMainPanel();
            this.setVisible(false);
        });

        addButton.addActionListener(action -> {
            try {
                boolean isAddButton = addButton.getText().equals("Add");

                Connection connection = DriverManager.getConnection(Database.getURL(), Database.getUser(), Database.getPass());
                PreparedStatement preparedStatement = connection.prepareStatement(
                        isAddButton ? "INSERT INTO \"Lessons\"(name, subject, year) VALUES (?, ?, ?)" : "UPDATE \"Lessons\" SET name = ?, subject = ?, year = ? WHERE id = ?",
                        PreparedStatement.RETURN_GENERATED_KEYS);

                String lesson = lessonNameTextField.getText();
                String subject = Objects.requireNonNull(professionComboBox.getSelectedItem()).toString();
                int year = databaseController.findYearId(Objects.requireNonNull(schoolYearComboBox.getSelectedItem()).toString());

                preparedStatement.setString(1, lesson);
                preparedStatement.setString(2, subject);
                preparedStatement.setInt(3, year);

                if (!isAddButton)
                    preparedStatement.setInt(4, selectedId);

                preparedStatement.executeUpdate();

                // Get the id of the newly inserted lesson
                int id = databaseController.getInsertedRowId(preparedStatement.getGeneratedKeys());

                preparedStatement.close();
                connection.close();

                System.out.printf("userId %d created lesson: %d (name: %s, subject: %s, year: %d)%n", User.getId(), id, lesson, subject, year);
            } catch (SQLException err) {
                System.out.println("SQL Exception:");
                err.printStackTrace();
            } finally {
                updateDetails();
                updateLessons();
                revertUIComponents();
            }
        });

        cancelButton.addActionListener(action -> revertUIComponents());

        editButton.addActionListener(action -> {
            // Get the selected row index
            int selectedRow = lessonsTable.getSelectedRow();

            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            addButton.setText("Save");
            lessonsTable.setEnabled(false);
            lessonNameTextField.setText(lessonsTable.getValueAt(selectedRow, 0).toString());
            professionComboBox.setSelectedItem(lessonsTable.getValueAt(selectedRow, 1));
            schoolYearComboBox.setSelectedItem(lessonsTable.getValueAt(selectedRow, 2));
            lessonsTable.clearSelection();

            // Store the selected user id, email and type to a global variable
            try {
                CachedRowSet lessons = databaseController.selectQuery(String.format("SELECT id, name, subject FROM \"Lessons\" WHERE name = '%s'", lessonNameTextField.getText()));
                lessons.next();

                selectedId = lessons.getInt("id");
                selectedName = lessons.getString("name");
                selectedProfession = lessons.getString("subject");
            } catch (SQLException err) {
                System.out.println("SQL Exception:");
                err.printStackTrace();
            }
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

        selectedId = -1;
        selectedName = "";
        selectedProfession = "";
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

    /**
     * Update professionComboBox and schoolYearComboBox with new values (if any)
     */
    private void updateDetails() {
        // Update professions
        professionComboBox.removeAllItems();

        for (String subject : professionList)
            professionComboBox.addItem(subject);

        if (professionComboBox.getItemCount() > 1)
            professionComboBox.setSelectedIndex(1);

        // Update school years
        schoolYearComboBox.removeAllItems();

        for (String year : yearList)
            schoolYearComboBox.addItem(year);

        if (schoolYearComboBox.getItemCount() > 1)
            schoolYearComboBox.setSelectedIndex(1);
    }

    private void updateLessons() {
        // TODO: (Prionysis) Update table with lessons from the database

        IntStream.iterate(lessonsTableModel.getRowCount() - 1, i -> i > -1, i -> i - 1).forEach(i -> lessonsTableModel.removeRow(i));

        try {
            Object[] row = new Object[3];
            CachedRowSet lessons = databaseController.selectQuery("SELECT name, subject, year FROM \"Lessons\" ORDER BY name");

            // Add rows
            while (lessons.next()) {
                row[0] = lessons.getString("name");
                row[1] = lessons.getString("subject");
                row[2] = databaseController.findYearName(lessons.getInt("year"));

                lessonsTableModel.addRow(row);
            }
        } catch (SQLException err) {
            System.out.println("SQL Exception:");
            err.printStackTrace();
        } finally {
            panelController.fillEmptyRows(lessonsTableModel);
            lessonsTable.setModel(lessonsTableModel);
        }
    }
}
