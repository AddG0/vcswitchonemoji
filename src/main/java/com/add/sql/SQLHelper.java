package com.add.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.cdimascio.dotenv.Dotenv;

public class SQLHelper {
    private static Logger logger = LoggerFactory.getLogger(SQLHelper.class.getName());
    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private static Dotenv env = Dotenv.load();

    private static HikariDataSource ds;

    private SQLHelper() {
    }

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env.get("SQLLINK"));
        config.setUsername(env.get("SQLUSER"));
        config.setPassword(env.get("SQLPASS"));
        try {
            int maxConnections = getMaxConnections();
            config.setMaximumPoolSize(maxConnections);
            logger.info("Max connections: {}", maxConnections);
        } catch (SQLException e) {
            logger.error("Error getting max connections", e);
        }
        ds = new HikariDataSource(config);
    }

    public static Connection getConnection(long guildId) throws SQLException {
        Connection conn = ds.getConnection();
        conn.setCatalog("" + guildId);
        Exception exception = new Exception();
        String callerClassName = exception.getStackTrace()[1].getClassName();
        executorService.schedule(() -> {
            try {
                if (!conn.isClosed()) {
                    logger.warn("SQL connection open in class " + callerClassName + " for more than 10 seconds",
                            exception);
                }
            } catch (SQLException e) {
                logger.error("Error checking if SQL connection is closed", e);
            }
        }, 10, TimeUnit.SECONDS);
        return conn;
    }

    private static int getMaxConnections() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Error loading MySQL driver", e);
        }
        try (Connection connection = DriverManager.getConnection(env.get("SQLLINK"), env.get("SQLUSER"),
                env.get("SQLPASS"))) {
            PreparedStatement statement = connection.prepareStatement("SHOW VARIABLES LIKE 'max_connections'");
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("Value");
                }
            }
        }
        throw new SQLException("Failed to retrieve max_connections");
    }

    private static int getCurrentConnections() throws SQLException {
        try (Connection connection = DriverManager.getConnection(env.get("SQLLINK"), env.get("SQLUSER"),
                env.get("SQLPASS"))) {
            try (PreparedStatement statement = connection.prepareStatement("SHOW PROCESSLIST")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    int count = 0;
                    while (resultSet.next()) {
                        count++;
                    }
                    return count;
                }
            }
        }
    }

    public static void connectToGuild(long guildId) {
        try {
            executeSQL(guildId, "CREATE DATABASE IF NOT EXISTS `" + guildId + "`");
        } catch (SQLException e) {
            logger.error("Error connecting to guild {}", guildId, e);
        }
    }

    public static void executeSQL(long guildId, String sql) throws SQLException {
        try (Connection conn = getConnection(guildId)) {
            conn.createStatement().execute(sql);
        }
    }

    public static <T> T get(long guildId, SQLTable enumGet, SQLTable enumFrom, Object where) {
        String tableName = enumGet.getTableName();
        String columnName = enumGet.getName();
        Class<T> classType = (Class<T>) enumGet.getType();
        String whereColumnName = enumFrom.getName();
        try (Connection conn = SQLHelper.getConnection(guildId)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
            ResultSet resultSet = create.select(field(
                    columnName)).from(table(tableName))
                    .where(field(whereColumnName).eq(where)).fetchResultSet();
            if (resultSet.next()) {
                return resultSet.getObject(columnName, classType);
            }
        } catch (SQLException e) {
            logger.error("Error getting from SQL", e);
        }
        throw new IllegalArgumentException("Invalid enum");
    }

    public static void update(long guildId, SQLTable enumSet, SQLTable enumWhere,
            Object where, Object newValue) {
        String tableName = enumSet.getTableName();
        String columnName = enumSet.getName();
        String whereColumnName = enumWhere.getName();
        try (Connection conn = SQLHelper.getConnection(guildId)) {
            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
            create.update(table(tableName))
                    .set(field(columnName), newValue)
                    .where(field(whereColumnName).eq(where))
                    .execute();
        } catch (SQLException e) {
            logger.error("Error updating SQL", e);
        }
        throw new IllegalArgumentException("Invalid enum");
    }
}

class SQLConnectionFailed extends RuntimeException {
    public SQLConnectionFailed(String message) {
        super(message);
    }
}