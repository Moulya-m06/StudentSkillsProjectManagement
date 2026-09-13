import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {
    public static void importDatabase(String jdbcUrl, String user, String password) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Path script = Paths.get("sql", "database.sql");
        if (!Files.exists(script)) {
            throw new IllegalStateException("Cannot find sql/database.sql. Open the app from the project folder.");
        }

        String sql = Files.readString(script);
        try (Connection con = DriverManager.getConnection(serverUrl(jdbcUrl), user, password)) {
            for (String statement : splitStatements(sql)) {
                if (!statement.isBlank()) {
                    try (Statement st = con.createStatement()) {
                        st.execute(statement);
                    }
                }
            }
        }
    }

    private static String serverUrl(String jdbcUrl) {
        int question = jdbcUrl.indexOf('?');
        String main = question >= 0 ? jdbcUrl.substring(0, question) : jdbcUrl;
        String params = question >= 0 ? jdbcUrl.substring(question) : "";
        int marker = main.indexOf("://");
        int slash = marker >= 0 ? main.indexOf('/', marker + 3) : -1;
        if (slash < 0) {
            return jdbcUrl;
        }
        return main.substring(0, slash + 1) + params;
    }

    private static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        String delimiter = ";";
        StringBuilder current = new StringBuilder();

        for (String line : sql.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            if (trimmed.toUpperCase().startsWith("DELIMITER ")) {
                delimiter = trimmed.substring("DELIMITER ".length()).trim();
                continue;
            }
            current.append(line).append(System.lineSeparator());
            if (trimmed.endsWith(delimiter)) {
                int end = current.lastIndexOf(delimiter);
                if (end >= 0) {
                    statements.add(current.substring(0, end).trim());
                    current.setLength(0);
                }
            }
        }

        if (!current.toString().trim().isEmpty()) {
            statements.add(current.toString().trim());
        }
        return statements;
    }
}
