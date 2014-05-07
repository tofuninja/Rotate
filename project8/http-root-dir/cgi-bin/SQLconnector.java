import java.io.*;
import java.sql.*;
import java.util.*;


public class SQLconnector{

	private static Connection getConnection() throws SQLException, IOException
	{
		//try {Class.forName("com.mysql.jdbc.Driver");} 
		//catch (ClassNotFoundException e) {e.printStackTrace();}
						
		String url = "jdbc:mysql://mydb.itap.purdue.edu:3306/chen1123";
		String username = "chen1123";
		String password = "131131";
		return DriverManager.getConnection( url, username, password);
	}

	public static void createNewPlayer (String name, int totalScore, String date) {
		Connection conn = null;
		try {
			conn = getConnection();
			Statement stat = conn.createStatement();
			stat.executeUpdate("INSERT INTO HighScore VALUES (\"" + name + "\", \"" + totalScore + "\", \"" + date + "\");");

		} catch (Exception e) {e.printStackTrace();}
		try { conn.close();} catch (Exception e) {e.printStackTrace();}

	}


	public static String getTop10Players () {
		Connection conn = null;
		StringBuilder message = null;
		try {
			conn = getConnection ();
			Statement stat = conn.createStatement();
			ResultSet result = stat.executeQuery("SELECT * FROM HighScore ORDER BY HighScores DESC LIMIT 10;");

			int columns = result.getMetaData().getColumnCount();
			message = new StringBuilder();

			while (result.next()){
				for (int i = 1; i <= columns; i++) {
					message.append(result.getString(i) + " ");
				}
				message.append("\n");
			}
			result.close();
			//System.out.println(message);
		} catch (Exception e) {return "Could Not Get Top 10";}
		try { conn.close();} catch (Exception e) {return "Could Not Get Top 10";}
		return message.toString();
	}

	public static void main (String args[]) {
		if(args.length == 0)	System.out.println(getTop10Players());
		else {
			//System.out.println(args[0]);
			String fields[] = args[0].split("\\.");
			fields[0] = fields[0].replace('+', ' ');
			fields[2] = fields[0].replace('+', ' ');
			System.out.println(fields[0]);
			System.out.println(fields[1]);
			System.out.println(fields[2]);
			createNewPlayer(fields[0], Integer.parseInt(fields[1]), fields[2]);
		}
	}

}
