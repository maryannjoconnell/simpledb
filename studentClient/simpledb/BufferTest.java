import java.sql.*;
import simpledb.remote.SimpleDriver;

public class BufferTest {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            // Step 1: connect to database server
            Driver d = new SimpleDriver();
            conn = d.connect("jdbc:simpledb://localhost", null);

            // Step 2: execute the query
            Statement stmt = conn.createStatement();


            String s = "create table TABLE1(AId int, AName varchar(32))";
            stmt.executeUpdate(s);
            System.out.println("Table TABLE1 created.");
            s = "create table TABLE2(AId int, AName varchar(32))";
            stmt.executeUpdate(s);
            System.out.println("Table TABLE2 created.");
            s = "create table TABLE3(AId int, AName varchar(32))";
            stmt.executeUpdate(s);
            System.out.println("Table TABLE3 created.");
            s = "create table TABLE4(AId int, AName varchar(32))";
            stmt.executeUpdate(s);
            System.out.println("Table TABLE4 created.");
            s = "create table TABLE5(AId int, AName varchar(32))";
            stmt.executeUpdate(s);
            System.out.println("Table TABLE5 created.");

            stmt.executeUpdate("insert into TABLE1(AId, AName) values (1, 'data')");
            stmt.executeUpdate("insert into TABLE2(AId, AName) values (1, 'data')");
            stmt.executeUpdate("insert into TABLE3(AId, AName) values (1, 'data')");
            stmt.executeUpdate("insert into TABLE4(AId, AName) values (1, 'data')");
            stmt.executeUpdate("insert into TABLE5(AId, AName) values (1, 'data')");
            ResultSet res = stmt.executeQuery("select AId from TABLE1 where AName = 'data'");
            while (res.next()) {
                System.out.println("Found index: " + res.getInt("AId"));
            }
            res = stmt.executeQuery("select AId from TABLE2 where AName = 'data'");
            while (res.next()) {
                System.out.println("Found index: " + res.getInt("AId"));
            }
            res = stmt.executeQuery("select AId from TABLE5 where AName = 'data'");
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
