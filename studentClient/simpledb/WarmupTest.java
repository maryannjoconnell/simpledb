import java.sql.*;
import simpledb.remote.SimpleDriver;

public class WarmupTest {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            // Step 1: connect to database server
            Driver d = new SimpleDriver();
            conn = d.connect("jdbc:simpledb://localhost", null);

            // Step 2: execute the query
            Statement stmt = conn.createStatement();

            String s = "create table ACTIVITIES(AId int, AName varchar(32))";
            stmt.executeUpdate(s);
            System.out.println("Table ACTIVITIES created.");
            s = "create table SPORTS(AId int, AName varchar(32))";
            stmt.executeUpdate(s);
            System.out.println("Table SPORTS created.");

            s = "insert into ACTIVITIES(AId, AName) values ";
            String[] activityvals = {
                    "(1, 'ACM')",
                    "(2, 'GDC')",
                    "(3, 'CSC')",
                    "(4, 'comedy club')",
                    "(5, 'art')",
                    "(6, 'WiCS')"
            };
            System.out.println("ACTIVITY records inserted.");
            s = "insert into SPORTS(AId, AName) values ";
            for (int i=0; i<activityvals.length; i++)
                stmt.executeUpdate(s + activityvals[i]);

            String[] sportvals = {
                    "(1, 'volleyball')",
                    "(2, 'soccer')",
                    "(3, 'ski team')",
                    "(4, 'swimming')",
                    "(5, 'basketball')",
                    "{6, 'badminton')"
            };
            System.out.println("SPORT records inserted.");
            for (int i=0; i<activityvals.length; i++)
                stmt.executeUpdate(s + activityvals[i]);
            ResultSet res = stmt.executeQuery("select AId from SPORTS where AName = 'soccer'");
            while (res.next()) {
                System.out.println("Found index: " + res.getInt("AId"));
            }
            res = stmt.executeQuery("select AId from ACTIVITIES where AName = 'GDC'");
            while (res.next()) {
                System.out.println("Found index: " + res.getInt("AId"));
            }
            res = stmt.executeQuery("select AId from ACTIVITIES where AName = 'CSC'");
            while (res.next()) {
                System.out.println("Found index: " + res.getInt("AId"));
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        finally {
            // Step 4: close the connection
            try {
                if (conn != null)
                    conn.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
