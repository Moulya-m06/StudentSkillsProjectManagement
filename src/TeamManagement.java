import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

public class TeamManagement extends JPanel {
    private final UserSession session;
    private final JPanel tableHolder = new JPanel(new BorderLayout());

    public TeamManagement(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        JPanel actions = new JPanel(new GridLayout(1, 3, 10, 10));
        actions.setBackground(Theme.PANEL);
        JButton create = Theme.button("Create Team");
        JButton member = Theme.button("Add Member");
        JButton role = Theme.button("Update Role");
        actions.add(create);
        actions.add(member);
        actions.add(role);
        add(Theme.title("Team Management", 24), BorderLayout.NORTH);
        add(tableHolder, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        create.addActionListener(e -> createTeam());
        member.addActionListener(e -> addMember());
        role.addActionListener(e -> updateRole());
        refresh();
    }

    private void refresh() {
        try {
            tableHolder.removeAll();
            JTable table = Theme.table(Theme.model("SELECT t.team_id, t.team_name, t.project_id, tm.student_id, tm.role, tm.contribution FROM TEAM t LEFT JOIN TEAM_MEMBERS tm ON t.team_id=tm.team_id WHERE t.created_by=? OR tm.student_id=?", session.userId, session.userId));
            tableHolder.add(Theme.scroll(table), BorderLayout.CENTER);
            tableHolder.revalidate();
            tableHolder.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void createTeam() {
        JTextField name = Theme.input();
        JTextField projectId = Theme.input();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Team Name", name, "Project ID", projectId}, "Create Team", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int teamId = 0;
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement("INSERT INTO TEAM(team_name, project_id, created_by) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name.getText());
                    ps.setInt(2, Integer.parseInt(projectId.getText()));
                    ps.setString(3, session.userId);
                    ps.executeUpdate();
                    try (java.sql.ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            teamId = keys.getInt(1);
                        }
                    }
                }
                DBConnection.update("INSERT INTO TEAM_MEMBERS(team_id, student_id, role, contribution) VALUES(?,?,?,?)", teamId, session.userId, "Leader", "Project coordination");
                DBConnection.update("UPDATE PROJECT SET team_id=? WHERE project_id=?", teamId, Integer.parseInt(projectId.getText()));
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void addMember() {
        JTextField teamId = Theme.input();
        JTextField studentId = Theme.input();
        JTextField role = Theme.input();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Team ID", teamId, "Student ID", studentId, "Role", role}, "Add Member", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("INSERT INTO TEAM_MEMBERS(team_id, student_id, role, contribution) VALUES(?,?,?,?)", Integer.parseInt(teamId.getText()), studentId.getText(), role.getText(), "Joined team");
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void updateRole() {
        JTextField teamId = Theme.input();
        JTextField studentId = Theme.input();
        JTextField role = Theme.input();
        JTextField contribution = Theme.input();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Team ID", teamId, "Student ID", studentId, "Role", role, "Contribution", contribution}, "Update Team Role", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("UPDATE TEAM_MEMBERS SET role=?, contribution=? WHERE team_id=? AND student_id=?", role.getText(), contribution.getText(), Integer.parseInt(teamId.getText()), studentId.getText());
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }
}
