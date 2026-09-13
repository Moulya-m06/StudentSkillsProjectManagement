import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class RegisterForm extends JFrame {
    private final JTextField studentId = Theme.input();
    private final JTextField usn = Theme.input();
    private final JTextField fullName = Theme.input();
    private final JTextField phone = Theme.input();
    private final JTextField email = Theme.input();
    private final JTextField department = Theme.input();
    private final JComboBox<String> year = Theme.combo(new String[]{"1", "2", "3", "4"});
    private final JTextField skills = Theme.input();
    private final JPasswordField password = Theme.passwordInput();
    private final JTextArea address = Theme.textArea(3, 20);
    private final JTextField photo = Theme.input();

    public RegisterForm() {
        Theme.frame(this, "Student Registration");
        JPanel root = Theme.panel();
        root.add(Theme.title("Create Student Profile", 26), BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(0, 2, 14, 10));
        form.setBackground(Theme.PANEL);
        Theme.padded(form, 8, 8, 8, 8);

        addRow(form, "Student ID", studentId);
        addRow(form, "USN", usn);
        addRow(form, "Full Name", fullName);
        addRow(form, "Phone Number", phone);
        addRow(form, "Email", email);
        addRow(form, "Department", department);
        addRow(form, "Year", year);
        addRow(form, "Skills (comma separated)", skills);
        addRow(form, "Password", passwordPanel());
        addRow(form, "Address", Theme.scroll(address));
        addRow(form, "Profile Photo Path", photo);

        JPanel actions = new JPanel();
        actions.setBackground(Theme.PANEL);
        JButton save = Theme.button("Register");
        JButton back = Theme.button("Back");
        actions.add(save);
        actions.add(back);
        save.addActionListener(e -> register());
        back.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });
        root.add(Theme.scroll(form), BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        add(root);
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

    private void addRow(JPanel form, String text, java.awt.Component field) {
        form.add(Theme.label(text));
        form.add(field);
    }

    private void register() {
        if (studentId.getText().trim().isEmpty() || usn.getText().trim().isEmpty() || fullName.getText().trim().isEmpty()
                || email.getText().trim().isEmpty() || password.getPassword().length < 6) {
            JOptionPane.showMessageDialog(this, "Student ID, USN, name, email and a 6+ character password are required.");
            return;
        }
        if (!email.getText().contains("@")) {
            JOptionPane.showMessageDialog(this, "Enter a valid email address.");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement s = con.prepareStatement("INSERT INTO STUDENT(student_id, usn, full_name, phone, email, department, year, address, profile_photo) VALUES(?,?,?,?,?,?,?,?,?)");
                 PreparedStatement d = con.prepareStatement("INSERT INTO STUDENT_DETAILS(student_id, summary, cgpa, career_goal) VALUES(?,?,?,?)");
                 PreparedStatement l = con.prepareStatement("INSERT INTO LOGIN(user_id, username, password_hash, role, status) VALUES(?,?,?,?,?)")) {
                s.setString(1, studentId.getText().trim());
                s.setString(2, usn.getText().trim());
                s.setString(3, fullName.getText().trim());
                s.setString(4, phone.getText().trim());
                s.setString(5, email.getText().trim());
                s.setString(6, department.getText().trim());
                s.setInt(7, Integer.parseInt(year.getSelectedItem().toString()));
                s.setString(8, address.getText().trim());
                s.setString(9, photo.getText().trim());
                s.executeUpdate();
                d.setString(1, studentId.getText().trim());
                d.setString(2, "New student profile");
                d.setDouble(3, 0);
                d.setString(4, "Build strong technical portfolio");
                d.executeUpdate();
                l.setString(1, studentId.getText().trim());
                l.setString(2, usn.getText().trim());
                l.setString(3, PasswordUtil.hashPassword(new String(password.getPassword())));
                l.setString(4, "STUDENT");
                l.setString(5, "ACTIVE");
                l.executeUpdate();
                saveSkills(con, studentId.getText().trim(), skills.getText());
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
            JOptionPane.showMessageDialog(this, "Registration successful. Login with USN and password.");
            dispose();
            new LoginForm().setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void saveSkills(Connection con, String id, String skillText) throws Exception {
        if (skillText == null || skillText.trim().isEmpty()) {
            return;
        }
        try (PreparedStatement insertSkill = con.prepareStatement("INSERT INTO SKILLS(skill_name, category) VALUES(?,'Technical') ON DUPLICATE KEY UPDATE skill_name=VALUES(skill_name)");
             PreparedStatement findSkill = con.prepareStatement("SELECT skill_id FROM SKILLS WHERE skill_name=?");
             PreparedStatement insertStudentSkill = con.prepareStatement("INSERT IGNORE INTO STUDENT_SKILLS(student_id, skill_id, skill_level, years_experience) VALUES(?,?, 'Beginner', 0)")) {
            for (String raw : skillText.split(",")) {
                String skillName = raw.trim();
                if (skillName.isEmpty()) {
                    continue;
                }
                insertSkill.setString(1, skillName);
                insertSkill.executeUpdate();
                findSkill.setString(1, skillName);
                try (java.sql.ResultSet rs = findSkill.executeQuery()) {
                    if (rs.next()) {
                        insertStudentSkill.setString(1, id);
                        insertStudentSkill.setInt(2, rs.getInt("skill_id"));
                        insertStudentSkill.executeUpdate();
                    }
                }
            }
        }
    }
}
