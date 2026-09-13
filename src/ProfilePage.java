import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ProfilePage extends JPanel {
    private final UserSession session;
    private final JTextField name = Theme.input();
    private final JTextField phone = Theme.input();
    private final JTextField email = Theme.input();
    private final JTextField department = Theme.input();
    private final JComboBox<String> year = Theme.combo(new String[]{"1", "2", "3", "4"});
    private final JTextArea address = Theme.textArea(3, 20);
    private final JTextArea summary = Theme.textArea(3, 20);
    private final JTextField cgpa = Theme.input();
    private final JTextField goal = Theme.input();

    public ProfilePage(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setBackground(Theme.PANEL);
        Theme.padded(form, 18, 18, 18, 18);
        addRow(form, "Full Name", name);
        addRow(form, "Phone", phone);
        addRow(form, "Email", email);
        addRow(form, "Department", department);
        addRow(form, "Year", year);
        addRow(form, "Address", Theme.scroll(address));
        addRow(form, "Profile Summary", Theme.scroll(summary));
        addRow(form, "CGPA", cgpa);
        addRow(form, "Career Goal", goal);
        JPanel actions = new JPanel();
        actions.setBackground(Theme.PANEL);
        JButton save = Theme.button("Save Profile");
        JButton cert = Theme.button("Add Certification");
        JButton achieve = Theme.button("Add Achievement");
        JButton pass = Theme.button("Change Password");
        actions.add(save);
        actions.add(cert);
        actions.add(achieve);
        actions.add(pass);
        add(Theme.title("Profile Management", 24), BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        load();
        save.addActionListener(e -> save());
        cert.addActionListener(e -> addCertification());
        achieve.addActionListener(e -> addAchievement());
        pass.addActionListener(e -> changePassword());
    }

    private void addRow(JPanel p, String label, java.awt.Component c) {
        p.add(Theme.label(label));
        p.add(c);
    }

    private void load() {
        try (java.sql.Connection con = DBConnection.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement("SELECT s.*, d.summary, d.cgpa, d.career_goal FROM STUDENT s LEFT JOIN STUDENT_DETAILS d ON s.student_id=d.student_id WHERE s.student_id=?")) {
            ps.setString(1, session.userId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    name.setText(rs.getString("full_name"));
                    phone.setText(rs.getString("phone"));
                    email.setText(rs.getString("email"));
                    department.setText(rs.getString("department"));
                    year.setSelectedItem(String.valueOf(rs.getInt("year")));
                    address.setText(rs.getString("address"));
                    summary.setText(rs.getString("summary"));
                    cgpa.setText(rs.getString("cgpa"));
                    goal.setText(rs.getString("career_goal"));
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void save() {
        try {
            DBConnection.update("UPDATE STUDENT SET full_name=?, phone=?, email=?, department=?, year=?, address=? WHERE student_id=?",
                    name.getText(), phone.getText(), email.getText(), department.getText(), Integer.parseInt(year.getSelectedItem().toString()), address.getText(), session.userId);
            DBConnection.update("UPDATE STUDENT_DETAILS SET summary=?, cgpa=?, career_goal=? WHERE student_id=?",
                    summary.getText(), Double.parseDouble(cgpa.getText().isBlank() ? "0" : cgpa.getText()), goal.getText(), session.userId);
            JOptionPane.showMessageDialog(this, "Profile updated.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void addCertification() {
        JTextField title = Theme.input();
        JTextField issuer = Theme.input();
        JTextField date = Theme.input();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Title", title, "Issuer", issuer, "Issue Date (YYYY-MM-DD)", date}, "Add Certification", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("INSERT INTO CERTIFICATIONS(student_id, title, issuer, issue_date) VALUES(?,?,?,?)", session.userId, title.getText(), issuer.getText(), date.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void addAchievement() {
        JTextField title = Theme.input();
        JTextArea desc = Theme.textArea(3, 20);
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Title", title, "Description", desc}, "Add Achievement", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("INSERT INTO ACHIEVEMENTS(student_id, title, description, achievement_date) VALUES(?,?,?,CURDATE())", session.userId, title.getText(), desc.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void changePassword() {
        JPasswordField p = Theme.passwordInput();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"New Password", p}, "Change Password", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("UPDATE LOGIN SET password_hash=? WHERE user_id=?", PasswordUtil.hashPassword(new String(p.getPassword())), session.userId);
                JOptionPane.showMessageDialog(this, "Password changed.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }
}
