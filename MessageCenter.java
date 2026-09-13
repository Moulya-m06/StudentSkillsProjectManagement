import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MessageCenter extends JPanel {
    private final UserSession session;
    private final JPanel tableHolder = new JPanel(new BorderLayout());

    public MessageCenter(UserSession session) {
        this.session = session;
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        JPanel actions = new JPanel(new GridLayout(1, 3, 10, 10));
        actions.setBackground(Theme.PANEL);
        JButton send = Theme.button("Send Message");
        JButton read = Theme.button("Mark Read");
        JButton refresh = Theme.button("Refresh");
        actions.add(send);
        actions.add(read);
        actions.add(refresh);
        add(Theme.title("Mentor/Admin Communication", 24), BorderLayout.NORTH);
        add(tableHolder, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        send.addActionListener(e -> send());
        read.addActionListener(e -> markRead());
        refresh.addActionListener(e -> refresh());
        refresh();
    }

    private void refresh() {
        try {
            tableHolder.removeAll();
            String sql = "SELECT message_id, sender_id, receiver_id, subject, message, sent_at, is_read FROM MESSAGES WHERE sender_id=? OR receiver_id=? ORDER BY sent_at DESC";
            JTable table = Theme.table(Theme.model(sql, session.userId, session.userId));
            tableHolder.add(Theme.scroll(table), BorderLayout.CENTER);
            tableHolder.revalidate();
            tableHolder.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void send() {
        JTextField receiver = Theme.input();
        JTextField subject = Theme.input();
        JTextArea message = Theme.textArea(4, 24);
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Receiver ID", receiver, "Subject", subject, "Message", message}, "Send Message", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("INSERT INTO MESSAGES(sender_id, receiver_id, subject, message) VALUES(?,?,?,?)", session.userId, receiver.getText(), subject.getText(), message.getText());
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void markRead() {
        JTextField id = Theme.input();
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Message ID", id}, "Mark Read", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                DBConnection.update("UPDATE MESSAGES SET is_read=1 WHERE message_id=? AND receiver_id=?", Integer.parseInt(id.getText()), session.userId);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }
}
