package com.etho.discordlink.utils.sql;

import com.etho.discordlink.Discordlink;
import com.etho.discordlink.utils.Config;

import java.sql.*;
import java.util.*;

public class SqlConnection {
    static SqlConnection instance;

    private Connection connection;
    private Statement statement;

    public static void init() {
        if (instance == null) {
            instance = new SqlConnection();
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            instance.connection = null;
            try {
                if (Config.DEFAULT.getString("sql-connection.address") != null) {
                    String server = Config.DEFAULT.getString("sql-connection.address");
                    String database = Config.DEFAULT.getString("sql-connection.database");
                    String username = Config.DEFAULT.getString("sql-connection.username");
                    String password = Config.DEFAULT.getString("sql-connection.password");
                    instance.connection = DriverManager.getConnection("jdbc:sqlite://" + server + "/" + database + "?autoReconnect=true", username, password);
                } else {
                    instance.connection = DriverManager.getConnection("jdbc:sqlite:" + Discordlink.getDataFolder() +"/verifications.db");
                }
                if (instance != null) {
                    instance.statement = instance.connection.createStatement();
                    instance.statement.setQueryTimeout(30);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                instance = null;
            }
            if (instance != null) {
                try {
                    instance.statement.executeUpdate("create table if not exists verified (uuid string, username string, tag string)");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void addVerified(UUID id, String username, String tag) {
        try {
            instance.statement.executeUpdate("insert into verified values('" + id.toString() + "', '" + username + "', '" + tag + "')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeVerified(String username) {
        try {
            instance.statement.executeUpdate("delete from verified where username='" + username +"'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getVerifiedUsernames(String tag) {
        try {
            ResultSet rs = instance.statement.executeQuery("select * from verified where tag = '" + tag + "'");
            List<String> names = new ArrayList<>();
            while (rs.next()) {
                names.add(rs.getString("username"));
            }
            return names;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getVerifiedUsername(UUID id) {
        try {
            ResultSet rs = instance.statement.executeQuery("select * from verified where uuid = '" + id.toString() + "'");
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isVerified(String username) {
        try {
            ResultSet rs = instance.statement.executeQuery("select * from verified where username = '" + username + "'");
            if (rs.next()) {
                return true;
            };
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static HashMap<String, String> getAllVerifiedIds() {
        HashMap<String, String> verified = new HashMap<>();
        try {
            ResultSet rs = instance.statement.executeQuery("select * from verified");
            while (rs.next()) {
                verified.put(rs.getString("uuid"), rs.getString("tag"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return verified;
    }

    public static void updateUsername(String old, String newU) {
        try {
            ResultSet rs = instance.statement.executeQuery("update verified set username = '" + newU + "' where username = '" + old + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getVerifiedDiscord(String username) {
        try {
            ResultSet rs = instance.statement.executeQuery("select * from verified where username = '" + username + "'");
            if (rs.next()) {
                return rs.getString("tag");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getVerifiedDiscord(UUID id) {
        try {
            ResultSet rs = instance.statement.executeQuery("select * from verified where uuid = '" + id.toString() + "'");
            if (rs.next()) {
                return rs.getString("tag");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SqlConnection get() {
        return instance;
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        instance = null;
    }
}
