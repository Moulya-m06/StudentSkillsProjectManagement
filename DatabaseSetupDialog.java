import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class DatabaseSetupDialog extends JDialog {
    private final JTextField url = Theme.input();
    private final JTextField user = Theme.input();
    private final JPasswordField password = Theme.passwordInput();
    private boolean configured;

    public DatabaseSetupDialog() {
        setTitle("Database Setup");
        setModal(true);
        setSize(760, 430);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Theme.BG);

        JPanel root = Theme.panel();
        root.add(header(), BorderLayout.NORTH);
        root.add(form(), BorderLayout.CENTER);
        root.add(actions(), BorderLayout.SOUTH);
        add(root);
        loadValues();
    }

    public boolean isConfigured() {
        return configured;
    }

    private JPanel header() {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(Theme.PANEL);
        p.add(Theme.title("Connect MySQL Database", 24), BorderLayout.NORTH);
        p.add(Theme.label("Enter the same MySQL username and password you use in MySQL Workbench."), BorderLayout.SOUTH);
        return p;
    }

    private JPanel form() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.PANEL);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(9, 8, 9, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        p.add(Theme.label("JDBC URL"), c);
        c.gridx = 1;
        c.weightx = 1;
        p.add(url, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        p.add(Theme.label("MySQL User"), c);
        c.gridx = 1;
        c.weightx = 1;
        p.add(user, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        p.add(Theme.label("MySQL Password"), c);
        c.gridx = 1;
        c.weightx = 1;
        p.add(passwordPanel(), c);

        c.gridx = 1;
        c.gridy++;
        JLabel help = Theme.label("If you see 'Access denied', the password is wrong or the MySQL user is different.");
        p.add(help, c);
        return p;
    }

    private JPanel passwordPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(Theme.PANEL);
        JCheckBox show = new JCheckBox("Show");
        show.setBackground(Theme.PANEL);
        show.setForeground(Theme.MUTED);
        show.setFocusPainted(false);
        show.addActionListener(e -> password.setEchoChar(show.isSelected() ? (char) 0 : '*'));
        p.add(password, BorderLayout.CENTER);
        p.add(show, BorderLayout.EAST);
        return p;
    }

    private JPanel actions() {
        JPanel p = new JPanel();
        p.setBackground(Theme.PANEL);
        JButton test = Theme.button("Test Connection");
        JButton importDb = Theme.button("Create Database");
        JButton save = Theme.button("Save & Continue");
        JButton exit = Theme.button("Exit");
        p.add(test);
        p.add(importDb);
        p.add(save);
        p.add(exit);
        test.addActionListener(e -> testConnection(false));
        importDb.addActionListener(e -> importDatabase());
        save.addActionListener(e -> {
            if (testConnection(true)) {
                try {
                    DBConnection.saveConfig(url.getText().trim(), user.getText().trim(), new String(password.getPassword()));
                    configured = true;
                    dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        });
        exit.addActionListener(e -> dispose());
        return p;
    }

    private void importDatabase() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "This will run sql/database.sql and recreate the student_skills_pm database. Continue?",
                "Create Database",
                JOptionPane.YES_NO_OPTION
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            DatabaseInitializer.importDatabase(url.getText().trim(), user.getText().trim(), new String(password.getPassword()));
            DBConnection.saveConfig(url.getText().trim(), user.getText().trim(), new String(password.getPassword()));
            JOptionPane.showMessageDialog(this, "Database imported successfully. Click Save & Continue.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, readableError(ex.getMessage()));
        }
    }

    private void loadValues() {
        try {
            Properties p = DBConnection.loadConfig();
            url.setText(p.getProperty("db.url"));
            user.setText(p.getProperty("db.user"));
            password.setText(p.getProperty("db.password"));
        } catch (SQLException ex) {
            url.setText("jdbc:mysql://localhost:3306/student_skills_pm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
            user.setText("root");
        }
    }

    private boolean testConnection(boolean quietOnSuccess) {
        if (url.getText().trim().isEmpty() || user.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter JDBC URL and MySQL username.");
            return false;
        }
        try {
            DBConnection.testConnection(url.getText().trim(), user.getText().trim(), new String(password.getPassword()));
            if (!quietOnSuccess) {
                JOptionPane.showMessageDialog(this, "Connection successful.");
            }
            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, readableError(ex.getMessage()));
            return false;
        }
    }

    private String readableError(String message) {
        if (message != null && message.contains("Access denied")) {
            return "Access denied. Enter your actual MySQL password for user '" + user.getText().trim() + "'.";
        }
        if (message != null && message.contains("Unknown database")) {
            return "Database not found. Click Create Database to import sql/database.sql automatically.";
        }
        return message;
    }
}
