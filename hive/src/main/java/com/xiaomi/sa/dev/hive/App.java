package com.xiaomi.sa.dev.hive;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;

/**
 * Hello world!
 *
 */
public class App 
{
	private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    public static void main( String[] args ) throws SQLException
    {
    	System.out.println("hello world");
    	 try {
             Class.forName(driverName);
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
             System.exit(1);
         }

         Connection con = DriverManager.getConnection(
                            "jdbc:hive2://10.108.97.222:80/sadev", "hive", "123456");
         Statement stmt = con.createStatement();

   
         String sql = "select * from user" ;
         System.out.println("Running: " + sql);
         ResultSet res = stmt.executeQuery(sql);
         while (res.next()) {
        	 System.out.println(res.getInt(1) + "\t" + res.getString(2) + "\t" +res.getString(3) + "\t" +res.getString(4)  );
         }
    }
}
