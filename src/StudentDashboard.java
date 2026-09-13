import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class StudentDashboard extends JFrame {
    private final UserSession session;
    private final JPanel content = new JPanel(new BorderLayout(12, 12));

    public StudentDashboard(UserSession session) {
        this.session = session;
        Theme.frame(this, "Student Dashboard");
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
        JButton profile = Theme.nav("Profile");
        JButton skills = Theme.nav("Skills");
        JButton projects = Theme.nav("Projects");
        JButton teams = Theme.nav("Teams");
        JButton messages = Theme.nav("Mentor Messages");
        JButton logout = Theme.nav("Logout");
        side.add(Theme.title("Student Portal", 20));
        side.add(home);
        side.add(profile);
        side.add(skills);
        side.add(projects);
        side.add(teams);
        side.add(messages);
        side.add(logout);
        home.addActionListener(e -> showHome());
        profile.addActionListener(e -> set(new ProfilePage(session)));
        skills.addActionListener(e -> set(new SkillsManagement(session)));
        projects.addActionListener(e -> set(new ProjectManagement(session, false)));
        teams.addActionListener(e -> set(new TeamManagement(session)));
        messages.addActionListener(e -> set(new MessageCenter(session)));
        logout.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });
        return side;
    }

    private void showHome() {
        JPanel p = Theme.panel();
        p.add(Theme.title("Student Dashboard", 24), BorderLayout.NORTH);
        JPanel cards = new JPanel(new GridLayout(2, 3, 14, 14));
        cards.setBackground(Theme.PANEL);
        try {
            cards.add(Theme.card("Skills Added", String.valueOf(DBConnection.scalar("SELECT COUNT(*) FROM STUDENT_SKILLS WHERE student_id=?", session.userId))));
            cards.add(Theme.card("Active Projects", String.valueOf(DBConnection.scalar("SELECT COUNT(DISTINCT p.project_id) FROM PROJECT p LEFT JOIN TEAM t ON p.team_id=t.team_id LEFT JOIN TEAM_MEMBERS tm ON t.team_id=tm.team_id WHERE (p.created_by=? OR tm.student_id=?) AND p.status<>'COMPLETED'", session.userId, session.userId))));
            cards.add(Theme.card("Achievements", String.valueOf(DBConnection.scalar("SELECT COUNT(*) FROM ACHIEVEMENTS WHERE student_id=?", session.userId))));
            cards.add(Theme.card("Certifications", String.valueOf(DBConnection.scalar("SELECT COUNT(*) FROM CERTIFICATIONS WHERE student_id=?", session.userId))));
            cards.add(Theme.card("Upcoming Tasks", String.valueOf(DBConnection.scalar("SELECT COUNT(*) FROM PROJECT_TASKS pt JOIN PROJECT p ON pt.project_id=p.project_id LEFT JOIN TEAM t ON p.team_id=t.team_id LEFT JOIN TEAM_MEMBERS tm ON t.team_id=tm.team_id WHERE (p.created_by=? OR tm.student_id=?) AND pt.status<>'COMPLETED' AND pt.due_date>=CURDATE()", session.userId, session.userId))));
            cards.add(Theme.card("Notifications", String.valueOf(DBConnection.scalar("SELECT COUNT(*) FROM MESSAGES WHERE receiver_id=? AND is_read=0", session.userId))));
        } catch (Exception ex) {
            cards.add(Theme.card("Database", "Connect MySQL"));
        }
        p.add(cards, BorderLayout.CENTER);
        set(p);
    }

    private void set(JPanel panel) {
        content.removeAll();
        content.add(panel, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }
}
