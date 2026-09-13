import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

public class Theme {
    public static final Color BG = new Color(9, 16, 28);
    public static final Color PANEL = new Color(17, 28, 46);
    public static final Color CARD = new Color(24, 39, 62);
    public static final Color FIELD = new Color(12, 22, 38);
    public static final Color BORDER = new Color(51, 65, 85);
    public static final Color ACCENT = new Color(0, 180, 216);
    public static final Color ACCENT2 = new Color(72, 202, 228);
    public static final Color TEXT = new Color(238, 246, 255);
    public static final Color MUTED = new Color(148, 163, 184);
    public static final Color DANGER = new Color(239, 68, 68);

    public static Font font(int size, int style) {
        return new Font("Segoe UI", style, size);
    }

    public static void frame(javax.swing.JFrame frame, String title) {
        frame.setTitle(title);
        frame.setSize(1180, 720);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(BG);
    }

    public static JLabel title(String text, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(font(size, Font.BOLD));
        return label;
    }

    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(font(13, Font.PLAIN));
        return label;
    }

    public static JTextField input() {
        JTextField field = new JTextField();
        styleTextField(field);
        return field;
    }

    public static JPasswordField passwordInput() {
        JPasswordField field = new JPasswordField();
        styleTextField(field);
        field.setEchoChar('*');
        field.setEditable(true);
        field.setEnabled(true);
        field.setFocusable(true);
        field.setToolTipText("Enter password");
        return field;
    }

    public static JTextArea textArea(int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        styleTextComponent(area);
        return area;
    }

    public static <T> JComboBox<T> combo(T[] values) {
        JComboBox<T> combo = new JComboBox<>(values);
        combo.setBackground(FIELD);
        combo.setForeground(TEXT);
        combo.setFont(font(14, Font.PLAIN));
        combo.setFocusable(true);
        combo.setBorder(fieldBorder(BORDER));
        combo.setPreferredSize(new Dimension(220, 40));
        return combo;
    }

    private static void styleTextField(JTextField field) {
        styleTextComponent(field);
        field.setPreferredSize(new Dimension(260, 40));
    }

    private static void styleTextComponent(JTextComponent field) {
        field.setOpaque(true);
        field.setBackground(FIELD);
        field.setForeground(TEXT);
        field.setCaretColor(ACCENT2);
        field.setSelectionColor(new Color(14, 116, 144));
        field.setSelectedTextColor(Color.WHITE);
        field.setFont(font(14, Font.PLAIN));
        field.setBorder(fieldBorder(BORDER));
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(fieldBorder(ACCENT));
            }

            public void focusLost(FocusEvent e) {
                field.setBorder(fieldBorder(BORDER));
            }
        });
    }

    private static Border fieldBorder(Color color) {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(color), new EmptyBorder(10, 12, 10, 12));
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFont(font(14, Font.BOLD));
        b.setPreferredSize(new Dimension(150, 38));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(ACCENT2);
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(ACCENT);
            }
        });
        return b;
    }

    public static JButton nav(String text) {
        JButton b = button(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBackground(PANEL);
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(CARD);
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(PANEL);
            }
        });
        return b;
    }

    public static JPanel panel() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(PANEL);
        p.setBorder(new EmptyBorder(18, 18, 18, 18));
        return p;
    }

    public static JPanel card(String title, String value) {
        JPanel p = new JPanel(new GridLayout(2, 1, 4, 4));
        p.setBackground(CARD);
        p.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel t = label(title);
        JLabel v = title(value, 26);
        p.add(t);
        p.add(v);
        return p;
    }

    public static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(font(13, Font.PLAIN));
        table.setForeground(TEXT);
        table.setBackground(new Color(13, 24, 40));
        table.setSelectionBackground(new Color(14, 116, 144));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(51, 65, 85));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(CARD);
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setFont(font(13, Font.BOLD));
        return table;
    }

    public static JScrollPane scroll(Component component) {
        JScrollPane pane = new JScrollPane(component);
        pane.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        pane.getViewport().setBackground(BG);
        return pane;
    }

    public static void padded(JComponent c, int top, int left, int bottom, int right) {
        c.setBorder(new EmptyBorder(top, left, bottom, right));
    }

    public static DefaultTableModel model(String sql, Object... params) throws SQLException {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            DBConnection.bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
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
    }
}
