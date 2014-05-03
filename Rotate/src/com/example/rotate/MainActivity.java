package com.example.rotate;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Build;
import android.preference.EditTextPreference;

public class MainActivity extends Activity {

	int currentView = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
        	/*
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();*/
        }
        
        goToMainMenu();
    }
    
    /**
     * Goto the main menu
     */
    public void goToMainMenu()
    {
    	currentView = 0;
    	setContentView(R.layout.activity_main);
    	
    	View v = findViewById(R.id.main_layout);
    	v.setBackgroundDrawable(new coolView());
        
        // Set play button stuff
        Button button = (Button) findViewById(R.id.button_play);
        button.setOnClickListener(new OnClickListener()
        {
          public void onClick(View v)
          {
        	  goToGameScreen();
          }
        });
        
        // Set high score button stuff
        button = (Button) findViewById(R.id.button_score);
        button.setOnClickListener(new OnClickListener()
        {
          public void onClick(View v)
          {
        	  goToHighScoreScreen();
          }
        });
        
        // Set exit button stuff
        button = (Button) findViewById(R.id.button_exit);
        button.setOnClickListener(new OnClickListener()
        {
          public void onClick(View v)
          {
        	  finish();
          }
        });
    }
    
    /**
     * Go to the loss screen
     * @param Score The score from the previous game
     */
    public void goToLoss(int Score)
    {
    	currentView = 2;
    	setContentView(R.layout.game_loss_scree);
    	View v = findViewById(R.id.loss_layout);
    	v.setBackgroundDrawable(new coolView());
    	
    	TextView score_box = (TextView)findViewById(R.id.text_score);
    	score_box.setText("Score:" + Score );
    	
    	// Set submit button stuff
        Button button = (Button) findViewById(R.id.button_submit);
        final int f_score = Score;
        button.setOnClickListener(new OnClickListener()
        {
          public void onClick(View v)
          {
        	  EditText txt = (EditText)findViewById(R.id.text_name);
        	  String name = txt.getText().toString().trim();
        	  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        	  Date now = new Date();
        	  String date = sdf.format(now);
        	  if(!name.equals(""))
        	  {
        		  SQLconnector.createNewPlayer(name, f_score, date);
        		  goToHighScoreScreen();
        	  }
          }
        });
        
        // Set main menu button stuff
        button = (Button) findViewById(R.id.button_menu);
        button.setOnClickListener(new OnClickListener()
        {
          public void onClick(View v)
          {
        	  goToMainMenu();
          }
        });
    }
    
    /**
     * Go to the main game screen
     */
    public void goToGameScreen()
    {
    	currentView = 1;
		setContentView(R.layout.gamescreen);
		FrameLayout gameLayout = (FrameLayout) findViewById(R.id.game_layout);
		gameLayout.removeAllViews();
		gameLayout.addView(new gameView(getApplicationContext(), this));
    }

    /**
     * Go to the high score screen
     */
    public void goToHighScoreScreen()
    {
    	currentView = 3;
    	setContentView(R.layout.high_score_screen);
    	View v = findViewById(R.id.high_score_layout);
    	v.setBackgroundDrawable(new coolView());
    	String top10 = SQLconnector.getTop10Players();
    	TextView top_10_box = (TextView)findViewById(R.id.text_top_10);
    	top_10_box.setText(top10);
    	
    	// Set main menu button stuff
        Button button = (Button) findViewById(R.id.button_main_menu_from_score);
        button.setOnClickListener(new OnClickListener()
        {
          public void onClick(View v)
          {
        	  goToMainMenu();
          }
        });
    	
    }
    
    @Override
	public void onBackPressed() {
	    if(currentView != 0)
	    {
	    	goToMainMenu();
	    }
	    else if(currentView == 0)
	    {
	    	finish();
	    }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
