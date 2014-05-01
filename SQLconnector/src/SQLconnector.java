import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class SQLconnector{

	private static Connection getConnection() throws SQLException, IOException
	{
		Properties props = new Properties();
		FileInputStream in = new FileInputStream("C:/Users/chen1123/workspace/SQLconnector/src/database.properties");
		System.out.println("C:/Users/chen1123/workspace/SQLconnector/src");
		System.out.println(Paths.get("").toAbsolutePath().toString());
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

	public static void createNewPlayer (String name) {
		Connection conn = null;
		try {
			conn = getConnection();
			Statement stat = conn.createStatement();
			ResultSet result = stat.executeQuery("SELECT * FROM HighScore WHERE name=\"" + name +"\";");
			if (result.wasNull())
				stat.executeUpdate("INSERT INTO HighScore VALUES (\"" + name + "\", \"0\", \"0000-00-00\");");
		} catch (Exception e) {e.printStackTrace();}
	}

	public static void updatePlayer (String name, int totalScore, String date) {
		Connection conn = null;
		try {
			conn = getConnection();
			Statement stat = conn.createStatement();
			stat.executeUpdate("UPDATE HighScore SET highscores=\"" + totalScore + "\" WHERE name=\"" + name + "\";");
			stat.executeUpdate("UPDATE HighScore SET datecreated=\"" + date + "\" WHERE name=\"" + name + "\";");
		} catch (Exception e) {e.printStackTrace();}
	}


	public static void getTop10Players () {
		Connection conn = null;
		try {
			conn = getConnection ();
			Statement stat = conn.createStatement();
			ResultSet result = stat.executeQuery("SELECT * FROM HighScore ORDER BY HighScores DESC");

			int columns = result.getMetaData().getColumnCount();
			StringBuilder message = new StringBuilder();

			while (result.next()){
				for (int i = 1; i <= columns; i++) {
					message.append(result.getString(i) + " ");
				}
				message.append("\n");
			}
			System.out.println(message);
		} catch (Exception e) {}
	}

}