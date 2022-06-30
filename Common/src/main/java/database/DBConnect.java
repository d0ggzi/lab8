package database;


import java.io.FileReader;
import java.sql.*;
import java.util.Properties;

public class DBConnect {
    private String url;
    private String user;
    private String password;

    public Connection connect() {
        try {

            Properties properties = new Properties();
            FileReader fileReader = new FileReader("C:\\Users\\maks-\\GoogleDrive\\programming\\java\\lab8_all\\lab8\\Common\\src\\main\\resources\\db.properties");
//            FileReader fileReader = new FileReader("db.properties");
            properties.load(fileReader);
            url = properties.getProperty("db.localurl");
            user = properties.getProperty("db.username");
            password = properties.getProperty("db.password");

            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(url, user, password);
            return con;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
