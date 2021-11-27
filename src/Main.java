import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialOceanicTheme;
import views.*;

public class Main {
    public static void main(String[] args)
            throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialOceanicTheme()));

        // Load the postgres driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }

        SwingUtilities.invokeLater(() -> {
            schedulePanel schedule = new schedulePanel();
            schedule.setVisible(true);
//            gradesPanel grades = new gradesPanel();
//            grades.setVisible(true);
        });
    }
}
