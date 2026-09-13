import java.io.OutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/student_skills_pm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final Path CONFIG_PATH = Paths.get("db.properties");

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL JDBC driver not found. Add mysql-connector-j.jar to the lib folder.", ex);
        }

        Properties config = loadConfig();
        String url = envOrProperty("DB_URL", config, "db.url");
        String user = envOrProperty("DB_USER", config, "db.user");
        String password = envOrProperty("DB_PASSWORD", config, "db.password");

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException ex) {
            String message = "Database connection failed for user '" + user
                    + "'. Open db.properties and set db.user/db.password to your MySQL login, "
                    + "then import sql/database.sql. MySQL says: " + ex.getMessage();
            throw new SQLException(message, ex);
        }
    }

    public static boolean canConnect() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static Properties loadConfig() throws SQLException {
        Properties config = new Properties();
        config.setProperty("db.url", DEFAULT_URL);
        config.setProperty("db.user", DEFAULT_USER);
        config.setProperty("db.password", DEFAULT_PASSWORD);

        if (Files.exists(CONFIG_PATH)) {
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                config.load(in);
            } catch (Exception ex) {
                throw new SQLException("Unable to read db.properties.", ex);
            }
        }
        return config;
    }

    public static void saveConfig(String url, String user, String password) throws SQLException {
        Properties config = new Properties();
        config.setProperty("db.url", url);
        config.setProperty("db.user", user);
        config.setProperty("db.password", password);
        try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
            config.store(out, "Student Skills and Project Management System database settings");
        } catch (Exception ex) {
            throw new SQLException("Unable to save db.properties.", ex);
        }
    }

    public static void testConnection(String url, String user, String password) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL JDBC driver not found. Add mysql-connector-j.jar to the lib folder.", ex);
        }
        try (Connection ignored = DriverManager.getConnection(url, user, password)) {
            // Connection opened and closed successfully.
        }
    }

    private static String envOrProperty(String envName, Properties config, String propertyName) {
        String envValue = System.getenv(envName);
        if (envValue != null) {
            return envValue;
        }
        return config.getProperty(propertyName, "");
    }

    public static int update(String sql, Object... params) throws SQLException {
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    public static Object scalar(String sql, Object... params) throws SQLException {
        try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getObject(1) : null;
            }
        }
    }

    public static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    public static void ensureDefaultAdmin() {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            String hash = PasswordUtil.hashPassword("admin123");
            try (PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO LOGIN(user_id, username, password_hash, role, status) VALUES('ADM001','admin',?,'ADMIN','ACTIVE')")) {
                ps.setString(1, hash);
                ps.executeUpdate();
            }
            st.executeUpdate("INSERT IGNORE INTO ADMIN(admin_id, full_name, email, phone) VALUES('ADM001','System Admin','admin@college.edu','9999999999')");
        } catch (Exception ignored) {
        }
    }
}
