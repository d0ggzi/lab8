import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DBConnect {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        String url = "jdbc:postgresql://localhost:63333/studs";
        String user = "s336701";
        String password = "spv931";


        try (Connection con = DriverManager.getConnection(url, user, password);
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("select id, name, x, y, creation_date, annual_turnover, employees_count,  org_type, zip_code, loc_x, loc_y, loc_z from \"Organizations\" as O inner join \"Address\" A on A.addr_id = O.id")) {

            if (rs.next()) {
                ResultSetMetaData metadata = rs.getMetaData();
                int columnCount = metadata.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    System.out.println(metadata.getColumnName(i) + ", ");
                }
                System.out.println();
                while (rs.next()) {
                    String row = "";
                    for (int i = 1; i <= columnCount; i++) {
                        row += rs.getString(i) + ", ";
                    }
                    System.out.println();
                    System.out.println(row);

                }
            }

        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }
}