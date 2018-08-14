package com.atecher.devtools.code.generate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @description:
 * @author: hanhongwei
 * @date: 2018/6/25 下午1:43
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DBDriver{
    private String driverName = "com.mysql.jdbc.Driver";
    private String ip;
    private String port="3306";
    private String datebase;
    private String user;
    private String password;
    private Connection conn;


    public Connection getConncetion(){
        String url="jdbc:mysql://"+ip+":"+(port==null?"3306":port)+"/" + datebase + "?characterEncoding=utf8";

        try {
            Class.forName(driverName==null?"com.mysql.jdbc.Driver":driverName);
            conn = DriverManager.getConnection(url, user, password);
            return conn;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

}
