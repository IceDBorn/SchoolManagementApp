import controllers.panelController;
import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialOceanicTheme;
import javax.swing.*;

public class Main {
    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        // Use material theme
        UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialOceanicTheme()));
        // Launch the login window
        SwingUtilities.invokeLater(() -> panelController.createLoginPanel(null));
    }
}
