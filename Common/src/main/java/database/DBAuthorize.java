package database;

import commands.AuthData;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBAuthorize {
    private static final Logger logger = LogManager.getLogger(DBAuthorize.class);

    public String dbAuthorize(AuthData authData, Connection con) {
        PreparedStatement ps;
        ResultSet resultSet;
        if (authData.getMethod().equals("register")){
            try {
                int count;
                String sql = "select count(*) from \"User\" where login = ?";
                ps = con.prepareStatement(sql);
                ps.setString(1, authData.getLogin());
                ps.execute();
                resultSet = ps.getResultSet();
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                    if (count == 0) {
                        String add = "insert into \"User\" (login, password) VALUES (?, ?)";
                        ps = con.prepareStatement(add);
                        ps.setString(1, authData.getLogin());
                        ps.setString(2, hashPassword(authData.getPassword()));
                        ps.execute();
                        logger.info("Пользователь " + authData.getLogin() + " успешно зарегистрировался в системе");
                        return "true";
                    } else {
                        return "false";
                    }

                }
            } catch (SQLException e) {
                logger.error("Проблемы с базой данных");
            }
        }
        else if (authData.getMethod().equals("login")){
            try {
                String sql = "select * from \"User\" where login = ? and password = ?";
                ps = con.prepareStatement(sql);
                ps.setString(1, authData.getLogin());
                ps.setString(2, hashPassword(authData.getPassword()));
                ps.executeQuery();
                resultSet = ps.getResultSet();
                if (resultSet.next()){
                    String log = resultSet.getString(1);
                    String pass = resultSet.getString(2);
                    logger.info("Пользователь " + authData.getLogin() + " успешно вошел в систему");
                    return "true";
                }
                else{
                    return "false";
                }
            } catch (SQLException e){
                logger.error("Проблемы с базой данных");
            }
        }
        return "false";
    }

    public String hashPassword(String password){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] message = md.digest(password.getBytes());
            BigInteger no = new BigInteger(1, message);
            String hashText = no.toString(16);
            while (hashText.length() < 32){
                hashText = "0" + hashText;
            }
            return hashText;
        }catch (NoSuchAlgorithmException e){
            System.out.print("");
        }
        return password;
    }
}
