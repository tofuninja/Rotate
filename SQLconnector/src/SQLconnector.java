package com.example.rotate;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;


public class SQLconnector{
	
	private static String url = "http://sslab02.cs.purdue.edu:5544/cgi-bin/SQLscript";
	//private static String url = "http://www.google.com";
	
	
	public static void createNewPlayer (String name, int totalScore, String date) {
		try 
		{
			int l = name.length();
			for(int i = 0; i < (10-l); i++)
			{
				name += "_";
			}
			
			String totScoreString = ""+totalScore;
			String pad = "";
			
			
			for(int i = 0; i < (5-totScoreString.length()); i++)
			{
				pad += "_";
			}
			
			
			
			String s = "?_" + name + "__." + totalScore + "._" + pad + date;
			s = s.replace(' ' , '+');
			s = s.replace('\n' , '+');
			s = s.replace('\t' , '+');
			
			(new urlGetter()).execute(url + s);
			
		}
		catch (Exception e) 
		{
			Log.e("ROTATE", e.toString());
		}
	}

	public static String getTop10Players () {
		try 
		{
			return (new urlGetter()).execute(url).get().replace('_', ' ');

		}
		catch (Exception e) 
		{
			Log.e("ROTATE", e.toString());
			return "Connection error";
		}
	}


}


class urlGetter extends AsyncTask<String, Void, String>
	{

		@Override
		protected String doInBackground(String... params) 
		{
			try 
			{
				URL web = new URL(params[0]);
				
				BufferedReader in = new BufferedReader(new InputStreamReader(web.openStream()));
	
				String page = "";
				String inputLine;
				while ((inputLine = in.readLine()) != null)
					page += inputLine + "\n";
				
				in.close();
				
				return page;
			} 
			catch (Exception e) 
			{
				return "Connection Error";
			}
		}
		
	}