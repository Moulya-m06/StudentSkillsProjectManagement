import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            if (!DBConnection.canConnect()) {
                DatabaseSetupDialog setup = new DatabaseSetupDialog();
                setup.setVisible(true);
                if (!setup.isConfigured()) {
                    return;
                }
            }
            DBConnection.ensureDefaultAdmin();
            new LoginForm().setVisible(true);
        });
    }
}
