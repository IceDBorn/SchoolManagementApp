package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class StudentPanel extends JFrame {

  private JTable scheduleTable;
  private JPanel studentPanel;
  private JLabel usernameLabel;
  private JButton homeButton;
  private JScrollPane scrollPane;

  public StudentPanel() {
    add(studentPanel);
    setSize(1280, 720);
    setResizable(false);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    usernameLabel.setText("IceDBorn");
    scrollPane.setBorder(new EmptyBorder(0,0,0,0));
  }

  private void createUIComponents() {
    // Add columns
    String[] studentTableColumns = {"Lesson", "Class", "Date"};
    DefaultTableModel studentTableModel = new DefaultTableModel(studentTableColumns, 0);
    scheduleTable = new JTable(studentTableModel);
    // Stop users from interacting with the table
    scheduleTable.getTableHeader().setReorderingAllowed(false);
    scheduleTable.setEnabled(false);

    // Add rows
    Object[] row = new Object[3];
    row[0] = "Mathematics";
    row[1] = "G1";
    row[2] = "Monday - 13:00";

    studentTableModel.addRow(row);

    // Fill rows missing to fix white space
    int rowCount = studentTableModel.getRowCount();

    if (rowCount < 17) {
      for (int i = 0; i < 17 - rowCount; i++) {
        row[0] = "";
        row[1] = "";
        row[2] = "";

        studentTableModel.addRow(row);
      }
    }
  }
}
