import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class ProjectManagement extends JPanel {
    private final UserSession session;
    private final boolean admin;
    private final JPanel tableHolder = new JPanel(new BorderLayout());

    public ProjectManagement(UserSession session, boolean admin) {
        this.session = session;
        this.admin = admin;
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        JPanel actions = new JPanel(new GridLayout(1, 0, 10, 10));
        actions.setBackground(Theme.PANEL);
        JButton create = Theme.button("Create Project");
        JButton join = Theme.button("Apply/Join");
        JButton task = Theme.button("Add Task");
        JButton complete = Theme.button("Complete Task");
        JButton approve = Theme.button("Approve/Reject");
        JButton procedure = Theme.button("My Projects SP");
        actions.add(create);
        actions.add(join);
        actions.add(task);
        actions.add(complete);
        if (admin) {
            actions.add(approve);
        }
        if (!admin) {
            actions.add(procedure);
        }
        add(Theme.title(admin ? "Project Administration" : "Project Management", 24), BorderLayout.NORTH);
        add(tableHolder, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        create.addActionListener(e -> createProject());
        join.addActionListener(e -> joinProject());
        task.addActionListener(e -> addTask());
        complete.addActionListener(e -> completeTask());
        approve.addActionListener(e -> approveProject());
        procedure.addActionListener(e -> callStudentProjectsProcedure());
        refresh();
    }

    private void refresh() {
        try {
            tableHolder.removeAll();
            String sql = admin
                    ? "SELECT project_id, project_name, domain, technologies_used, team_size, deadline, status, completion_percentage, approval_status, created_by FROM PROJECT"
                    : "SELECT DISTINCT p.project_id, p.project_name, p.domain, p.technologies_used, p.deadline, p.status, p.completion_percentage, p.approval_status FROM PROJECT p LEFT JOIN TEAM t ON p.team_id=t.team_id LEFT JOIN TEAM_MEMBERS tm ON t.team_id=tm.team_id WHERE p.created_by=? OR tm.student_id=?";
            JTable table = Theme.table(admin ? Theme.model(sql) : Theme.model(sql, session.userId, session.userId));
            tableHolder.add(Theme.scroll(table), BorderLayout.CENTER);
            tableHolder.revalidate();
            tableHolder.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void createProject() {
        if (admin) {
            JOptionPane.showMessageDialog(this, "Use a student account to create student projects.");
            return;
        }
        JTextField name = Theme.input();
        JTextField domain = Theme.input();
        JTextField tech = Theme.input();
        JTextField teamSize = Theme.input();
        JTextField deadline = Theme.input();
        JTextArea desc = Theme.textArea(4, 24);
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Project Name", name, "Domain", domain, "Technologies Used", tech, "Team Size", teamSize, "Deadline (YYYY-MM-DD)", deadline, "Description", desc}, "Create Project", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("INSERT INTO PROJECT(project_name, domain, technologies_used, team_size, deadline, status, description, completion_percentage, approval_status, created_by) VALUES(?,?,?,?,?,'PLANNING',?,0,'PENDING',?)",
                        name.getText(), domain.getText(), tech.getText(), Integer.parseInt(teamSize.getText()), deadline.getText(), desc.getText(), session.userId);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void joinProject() {
        JTextField projectId = Theme.input();
        JTextField teamId = Theme.input();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Project ID", projectId, "Existing Team ID (optional)", teamId}, "Apply/Join Project", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String tid = teamId.getText().trim();
                if (tid.isEmpty()) {
                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement ps = con.prepareStatement("INSERT INTO TEAM(team_name, project_id, created_by) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, "Team-" + projectId.getText());
                        ps.setInt(2, Integer.parseInt(projectId.getText()));
                        ps.setString(3, session.userId);
                        ps.executeUpdate();
                        try (java.sql.ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                tid = String.valueOf(keys.getInt(1));
                            }
                        }
                    }
                    DBConnection.update("UPDATE PROJECT SET team_id=? WHERE project_id=?", Integer.parseInt(tid), Integer.parseInt(projectId.getText()));
                }
                DBConnection.update("INSERT IGNORE INTO TEAM_MEMBERS(team_id, student_id, role, contribution) VALUES(?,?,?,?)", Integer.parseInt(tid), session.userId, "Member", "Applied to collaborate");
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void addTask() {
        JTextField projectId = Theme.input();
        JTextField title = Theme.input();
        JTextField assigned = Theme.input();
        JTextField due = Theme.input();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Project ID", projectId, "Task Title", title, "Assigned Student ID", assigned, "Due Date (YYYY-MM-DD)", due}, "Add Task", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("INSERT INTO PROJECT_TASKS(project_id, task_title, assigned_to, due_date, status) VALUES(?,?,?,?, 'TODO')", Integer.parseInt(projectId.getText()), title.getText(), assigned.getText(), due.getText());
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void completeTask() {
        JTextField taskId = Theme.input();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Task ID", taskId}, "Mark Completed", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("UPDATE PROJECT_TASKS SET status='COMPLETED', completed_on=CURDATE() WHERE task_id=?", Integer.parseInt(taskId.getText()));
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void approveProject() {
        JTextField projectId = Theme.input();
        JComboBox<String> status = Theme.combo(new String[]{"APPROVED", "REJECTED", "PENDING"});
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Project ID", projectId, "Approval", status}, "Project Approval", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("UPDATE PROJECT SET approval_status=? WHERE project_id=?", status.getSelectedItem(), Integer.parseInt(projectId.getText()));
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void callStudentProjectsProcedure() {
        try (Connection con = DBConnection.getConnection(); CallableStatement cs = con.prepareCall("{CALL GetProjectsByStudent(?)}")) {
            cs.setString(1, session.userId);
            cs.execute();
            JTable table = Theme.table(modelFrom(cs.getResultSet()));
            JOptionPane.showMessageDialog(this, Theme.scroll(table), "Stored Procedure Result", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private DefaultTableModel modelFrom(ResultSet rs) throws Exception {
        ResultSetMetaData meta = rs.getMetaData();
        DefaultTableModel model = new DefaultTableModel();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            model.addColumn(meta.getColumnLabel(i));
        }
        while (rs.next()) {
            Object[] row = new Object[meta.getColumnCount()];
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row[i - 1] = rs.getObject(i);
            }
            model.addRow(row);
        }
        return model;
    }
}
