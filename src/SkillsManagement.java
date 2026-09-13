import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

public class SkillsManagement extends JPanel {
    private final UserSession session;
    private final JPanel tableHolder = new JPanel(new BorderLayout());

    public SkillsManagement(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        JPanel top = new JPanel(new GridLayout(1, 4, 10, 10));
        top.setBackground(Theme.PANEL);
        JButton addSkill = Theme.button("Add Skill");
        JButton addToProfile = Theme.button("Add To Profile");
        JButton updateLevel = Theme.button("Update Level");
        JButton delete = Theme.button("Remove");
        top.add(addSkill);
        top.add(addToProfile);
        top.add(updateLevel);
        top.add(delete);
        add(Theme.title("Skills Management", 24), BorderLayout.NORTH);
        add(top, BorderLayout.SOUTH);
        add(tableHolder, BorderLayout.CENTER);
        addSkill.addActionListener(e -> addSkill());
        addToProfile.addActionListener(e -> addToProfile());
        updateLevel.addActionListener(e -> updateLevel());
        delete.addActionListener(e -> remove());
        refresh();
    }

    private void refresh() {
        try {
            tableHolder.removeAll();
            String sql = "ADMIN".equals(session.role)
                    ? "SELECT skill_id, skill_name, category FROM SKILLS ORDER BY skill_name"
                    : "SELECT ss.student_skill_id, sk.skill_name, sk.category, ss.skill_level, ss.years_experience FROM STUDENT_SKILLS ss JOIN SKILLS sk ON ss.skill_id=sk.skill_id WHERE ss.student_id=?";
            JTable table = Theme.table("ADMIN".equals(session.role) ? Theme.model(sql) : Theme.model(sql, session.userId));
            tableHolder.add(Theme.scroll(table), BorderLayout.CENTER);
            tableHolder.revalidate();
            tableHolder.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void addSkill() {
        JTextField name = Theme.input();
        JComboBox<String> cat = Theme.combo(new String[]{"Technical", "Soft Skill", "Domain"});
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Skill Name", name, "Category", cat}, "Add Skill Master", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("INSERT INTO SKILLS(skill_name, category) VALUES(?,?)", name.getText(), cat.getSelectedItem());
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void addToProfile() {
        if ("ADMIN".equals(session.role)) {
            JOptionPane.showMessageDialog(this, "Admins maintain the skill database. Student skill mapping is available in student login.");
            return;
        }
        JTextField skillId = Theme.input();
        JComboBox<String> level = Theme.combo(new String[]{"Beginner", "Intermediate", "Advanced", "Expert"});
        JTextField exp = Theme.input();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Skill ID", skillId, "Level", level, "Years Experience", exp}, "Add Student Skill", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("INSERT INTO STUDENT_SKILLS(student_id, skill_id, skill_level, years_experience) VALUES(?,?,?,?)", session.userId, Integer.parseInt(skillId.getText()), level.getSelectedItem(), Double.parseDouble(exp.getText()));
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void updateLevel() {
        JTextField id = Theme.input();
        JComboBox<String> level = Theme.combo(new String[]{"Beginner", "Intermediate", "Advanced", "Expert"});
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Student Skill ID", id, "New Level", level}, "Update Level", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("UPDATE STUDENT_SKILLS SET skill_level=? WHERE student_skill_id=? AND student_id=?", level.getSelectedItem(), Integer.parseInt(id.getText()), session.userId);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void remove() {
        JTextField id = Theme.input();
        String label = "ADMIN".equals(session.role) ? "Skill ID" : "Student Skill ID";
        if (JOptionPane.showConfirmDialog(this, new Object[]{label, id}, "Remove Skill", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                if ("ADMIN".equals(session.role)) {
                    DBConnection.update("DELETE FROM SKILLS WHERE skill_id=?", Integer.parseInt(id.getText()));
                } else {
                    DBConnection.update("DELETE FROM STUDENT_SKILLS WHERE student_skill_id=? AND student_id=?", Integer.parseInt(id.getText()), session.userId);
                }
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }
}
