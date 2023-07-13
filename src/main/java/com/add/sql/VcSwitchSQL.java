package com.add.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum VcSwitchSQL implements SQLTable {
    EMOJI("emoji", String.class),
    CHANNEL_ID("channelId", Long.class);

    private static Logger logger = LoggerFactory.getLogger(VcSwitchSQL.class);
    private static final String TABLE = "VcSwitchEmojis";
    private Class<?> type;
    private String name;

    private VcSwitchSQL(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String getTableName() {
        return TABLE;
    }

    public static void createTable(long guildId) {
        logger.info("Creating table {}...", TABLE);
        try {
            SQLHelper.executeSQL(guildId, "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                    + "emoji VARCHAR(255) NOT NULL,"
                    + "channelId BIGINT NOT NULL,"
                    + "PRIMARY KEY (emoji),"
                    + "INDEX (channelId)"
                    + ")");
        } catch (Exception e) {
            logger.error("Error creating table", e);
        }
    }

    public static Map<String, Long> getEmojis(long guildId) {
        Map<String, Long> emojis = new HashMap<>();
        try (Connection conn = SQLHelper.getConnection(guildId)) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + TABLE);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                emojis.put(rs.getString("emoji"), rs.getLong("channelId"));
            }
        } catch (SQLException e) {
            logger.error("Error getting emojis", e);
        }
        return emojis;
    }

    public static boolean hasEmoji(long guildId, String emoij) {
        try (Connection conn = SQLHelper.getConnection(guildId)) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + TABLE + " WHERE emoji = ?");
            stmt.setString(1, emoij);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            return rs.next();
        } catch (SQLException e) {
            logger.error("Error getting emojis", e);
        }
        return false;
    }

    public static void delete(long guildId, String emoji) {
        try (Connection conn = SQLHelper.getConnection(guildId)) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + TABLE + " WHERE emoji = ?");
            stmt.setString(1, emoji);
            stmt.execute();
        } catch (SQLException e) {
            logger.error("Error deleting emoji", e);
        }
    }

    public static void insert(long guildId, String emoji, long channelId) {
        try (Connection conn = SQLHelper.getConnection(guildId)) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + TABLE + " (emoji, channelId) VALUES (?, ?)");
            stmt.setString(1, emoji);
            stmt.setLong(2, channelId);
            stmt.execute();
        } catch (SQLException e) {
            logger.error("Error inserting emoji", e);
        }
    }

}