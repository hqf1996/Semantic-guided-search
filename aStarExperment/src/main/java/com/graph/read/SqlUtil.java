package com.graph.read;


import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Created by yhj on 2018/5/28.
 * 数据库的连接关闭
 */
public class SqlUtil {
    public static Connection getConn(){
        return getConn("/database.properties");
    }

    public static Connection getConn(String path){
        Properties prop = new Properties();
        InputStream in = SqlUtil.class.getResourceAsStream(path);
        Connection conn = null;
        try {
            prop.load(in);
            String dirverClass = prop.getProperty("driverClass");
            String url = prop.getProperty("url");
            String username = prop.getProperty("username");
            String password = prop.getProperty("password");
            Class.forName(dirverClass);
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            if(conn != null){
                close(conn);
            }
        }finally {
            return conn;
        }
    }

    public static Statement getStatement(Connection conn){
        Statement stat = null;
        try {
            stat = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            if(stat != null){
                close(stat);
            }
        }finally {
            return stat;
        }
    }

    public static void close(Connection conn){
        try {
            if(conn != null){
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(Statement stat){
        try {
            if(stat != null){
                stat.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(ResultSet result){
        try {
            if(result != null){
                result.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close(Statement stat, ResultSet result){
        close(result);
        close(stat);
    }

    public static void close(Connection conn, Statement stat, ResultSet result){
        close(stat, result);
        close(conn);
    }
}
