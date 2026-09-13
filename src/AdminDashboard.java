import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;

public class AdminDashboard extends JFrame {
    private final UserSession session;
    private final JPanel content = new JPanel(new BorderLayout(12, 12));

    public AdminDashboard(UserSession session) {
        this.session = session;
        Theme.frame(this, "Admin Dashboard");
        content.setBackground(Theme.BG);
        add(sidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        showHome();
    }

    private JPanel sidebar() {
        JPanel side = new JPanel(new GridLayout(0, 1, 0, 8));
        side.setBackground(Theme.PANEL);
        Theme.padded(side, 18, 12, 18, 12);
        JButton home = Theme.nav("Dashboard");
        JButton students = Theme.nav("Students");
        JButton projects = Theme.nav("Projects");
        JButton skills = Theme.nav("Skills DB");
        JButton reports = Theme.nav("Reports");
        JButton messages = Theme.nav("Messages");
        JButton logout = Theme.nav("Logout");
        side.add(Theme.title("Admin Console", 20));
        side.add(home);
        side.add(students);
        side.add(projects);
        side.add(skills);
        side.add(reports);
        side.add(messages);
        side.add(logout);
        home.addActionListener(e -> showHome());
        students.addActionListener(e -> tablePanel("Students", "SELECT student_id, usn, full_name, department, year, email, phone FROM STUDENT"));
        projects.addActionListener(e -> set(new ProjectManagement(session, true)));
        skills.addActionListener(e -> set(new SkillsManagement(session)));
        reports.addActionListener(e -> tablePanel("Performance Report", "SELECT s.student_id, s.full_name, COUNT(DISTINCT ss.skill_id) skills, COUNT(DISTINCT p.project_id) projects, COUNT(DISTINCT a.achievement_id) achievements FROM STUDENT s LEFT JOIN STUDENT_SKILLS ss ON s.student_id=ss.student_id LEFT JOIN PROJECT p ON s.student_id=p.created_by LEFT JOIN ACHIEVEMENTS a ON s.student_id=a.student_id GROUP BY s.student_id, s.full_name"));
        messages.addActionListener(e -> set(new MessageCenter(session)));
        logout.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });
        return side;
    }

    private void showHome() {
        JPanel p = Theme.panel();
        p.add(Theme.title("Admin Analytics Dashboard", 24), BorderLayout.NORTH);
        JPanel cards = new JPanel(new GridLayout(2, 3, 14, 14));
        cards.setBackground(Theme.PANEL);
        try {
            cards.add(Theme.card("Total Students", String.valueOf(DBConnection.scalar("SELECT COUNT(*) FROM STUDENT"))));
            cards.add(Theme.card("Total Projects", String.valueOf(DBConnection.scalar("SELECT COUNT(*) FROM PROJECT"))));
            cards.add(Theme.card("Approved Projects", String.valueOf(DBConnection.scalar("SELECT COUNT(*) FROM PROJECT WHERE approval_status='APPROVED'"))));
            cards.add(Theme.card("Completed Projects", String.valueOf(DBConnection.scalar("SELECT COUNT(*) FROM PROJECT WHERE status='COMPLETED'"))));
            cards.add(Theme.card("Skill Records", String.valueOf(DBConnection.scalar("SELECT COUNT(*) FROM STUDENT_SKILLS"))));
            cards.add(Theme.card("Open Tasks", String.valueOf(DBConnection.scalar("SELECT COUNT(*) FROM PROJECT_TASKS WHERE status<>'COMPLETED'"))));
        } catch (Exception ex) {
            cards.add(Theme.card("Database", "Connect MySQL"));
        }
        p.add(cards, BorderLayout.CENTER);
        tablePanelInto(p, "Popular Skills", "SELECT sk.skill_name, COUNT(*) student_count FROM STUDENT_SKILLS ss JOIN SKILLS sk ON ss.skill_id=sk.skill_id GROUP BY sk.skill_name ORDER BY student_count DESC");
        set(p);
    }

    private void tablePanel(String title, String sql) {
        JPanel p = Theme.panel();
        p.add(Theme.title(title, 22), BorderLayout.NORTH);
        try {
            JTable table = Theme.table(Theme.model(sql));
            p.add(Theme.scroll(table), BorderLayout.CENTER);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        set(p);
    }

    private void tablePanelInto(JPanel p, String title, String sql) {
        try {
            JPanel bottom = Theme.panel();
            bottom.add(Theme.title(title, 18), BorderLayout.NORTH);
            bottom.add(Theme.scroll(Theme.table(Theme.model(sql))), BorderLayout.CENTER);
            p.add(bottom, BorderLayout.SOUTH);
        } catch (Exception ignored) {
        }
    }

    private void set(JPanel panel) {
        content.removeAll();
        content.add(panel, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }
}
