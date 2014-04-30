import java.io.*;
import java.sql.*;
import java.util.*;

public class SQLconnector{

	public static Connection getConnection() throws SQLException, IOException
	{
		Properties props = new Properties();
		FileInputStream in = new FileInputStream("database.properties");
		props.load(in);
		in.close();
		String drivers = props.getProperty("jdbc.drivers");
		if (drivers != null)
			System.setProperty("jdbc.drivers", drivers);
		String url = props.getProperty("jdbc.url");
		String username = props.getProperty("jdbc.username");
		String password = props.getProperty("jdbc.password");

		System.out.println("url="+url+" user="+username+" password="+password);

		return DriverManager.getConnection( url, username, password);
	}

	void createNewPlayer (String name) {
		Connection conn = null;
		try {
			conn = getConnection();
			Statement stat = conn.createStatement();
			stat.executeUpdate("INSERT INTO HighScore VALUES" + "('" + name + 
					"'0','0000-00-00'");
		} catch (Exception e) {}
	}

	void updateNewPlayer (String name, int totalScore, String date) {
		Connection conn = null;
		try {
			conn = getConnection();
			Statement stat = conn.createStatement();
			stat.executeUpdate("UPDATE HighScore set HighScores=" + totalScore + " where Players=" + name);
			stat.executeUpdate("UPDATE HighScore set DateCreated=" + date + " where Players=" + name);
		} catch (Exception e) {}
	}
	
	
	void getTop10Players () {
		Connection conn = null;
		try {
			conn = getConnection ();
			Statement stat = conn.createStatement();
			ResultSet result = stat.executeQuery("SELECT * FROM HighScore ORDER BY HighScores DESC");
			
			while (result.next()){
				System.out.print(result.getString(1)+"|");
			}
		} catch (Exception e) {}
	}

}