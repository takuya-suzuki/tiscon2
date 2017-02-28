package db.migration;

import java.sql.Connection;
import java.sql.Statement;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

/**
 * @author takahashi
 */
public class V1__CreateUser implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE user (" +
                    "user_id INTEGER DEFAULT 0 AUTO_INCREMENT PRIMARY KEY," +
                    "first_name VARCHAR(20)," +
                    "last_name VARCHAR(20)," +
                    "email VARCHAR(50) NOT NULL," +
                    "pass VARCHAR(20) NOT NULL," +
                    "login_flag BOOLEAN NULL" +
                    ")");
        }
    }
}
