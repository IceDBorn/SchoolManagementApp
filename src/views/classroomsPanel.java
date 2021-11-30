package views;

import javax.swing.*;

public class classroomsPanel extends JFrame {
    private JPanel classroomsPanel;
    private JTextField classNameTextField;
    private JSpinner classCapacitySpinner;
    private JButton addButton;

    public classroomsPanel() {
        add(classroomsPanel);
        setSize(400, 200);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setValue(1);
        model.setMinimum(1);
        model.setMaximum(99);
        classCapacitySpinner.setModel(model);
    }
}
