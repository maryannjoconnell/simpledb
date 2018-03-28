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

            s = "insert into ACTIVITIES(AId, AName) values ";
            String[] activityvals = {"(1, 'volleyball')",
                    "(2, 'soccer')",
                    "(3, 'ski team')",
                    "(4, 'ACM')"};
            for (int i=0; i<activityvals.length; i++)
                stmt.executeUpdate(s + activityvals[i]);
            System.out.println("ACTIVITY records inserted.");
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
