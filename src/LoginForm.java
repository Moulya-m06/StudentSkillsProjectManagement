import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginForm extends JFrame {
    private final JTextField username = Theme.input();
    private final JPasswordField password = Theme.passwordInput();

    public LoginForm() {
        Theme.frame(this, "Student Skills and Project Management System - Login");

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Theme.BG);
        JPanel card = Theme.panel();
        card.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JLabel title = Theme.title("Student Skills and Project Management", 25);
        card.add(title, c);
        c.gridy++;
        card.add(Theme.label("Secure Java Swing + MySQL platform"), c);
        c.gridwidth = 1;
        c.gridy++;
        card.add(Theme.label("Username"), c);
        c.gridx = 1;
        username.setPreferredSize(new java.awt.Dimension(280, 38));
        card.add(username, c);
        c.gridx = 0;
        c.gridy++;
        card.add(Theme.label("Password"), c);
        c.gridx = 1;
        card.add(passwordPanel(), c);
        c.gridx = 0;
        c.gridy++;
        JButton login = Theme.button("Login");
        JButton register = Theme.button("Register Student");
        card.add(login, c);
        c.gridx = 1;
        card.add(register, c);

        login.addActionListener(e -> login());
        register.addActionListener(e -> {
            dispose();
            new RegisterForm().setVisible(true);
        });
        password.addActionListener(e -> login());
        getRootPane().setDefaultButton(login);

        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);
    }

    private JPanel passwordPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(Theme.PANEL);
        JCheckBox show = new JCheckBox("Show");
        show.setBackground(Theme.PANEL);
        show.setForeground(Theme.MUTED);
        show.setFocusPainted(false);
        show.addActionListener(e -> password.setEchoChar(show.isSelected() ? (char) 0 : '*'));
        panel.add(password, BorderLayout.CENTER);
        panel.add(show, BorderLayout.EAST);
        return panel;
    }

    private void login() {
        if (username.getText().trim().isEmpty() || password.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Enter username and password.");
            return;
        }
        String sql = "SELECT user_id, username, password_hash, role FROM LOGIN WHERE username=? AND status='ACTIVE'";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username.getText().trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verify(new String(password.getPassword()), rs.getString("password_hash"))) {
                    UserSession session = new UserSession(rs.getString("user_id"), rs.getString("username"), rs.getString("role"));
                    dispose();
                    if ("ADMIN".equals(session.role)) {
                        new AdminDashboard(session).setVisible(true);
                    } else {
                        new StudentDashboard(session).setVisible(true);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid login credentials.");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
