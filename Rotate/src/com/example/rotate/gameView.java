package com.example.rotate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

/**
 * Game board view
 */
public class gameView extends View 
{
	private static Paint p = new Paint();
	private static Paint p2 = new Paint();
	private static Paint p3 = new Paint();
	
	private Bitmap pipe;
	private Bitmap pipe_b;
	
	private gameBoard board;
	
	private Matrix m = new Matrix();
	private float x_spacing;
	private float y_spacing;
	private int time = 0;
	private int score = 0;
	private float timeLeft = 150;
	private float timeMax = 150;
	private MainActivity main;
	
	static
	{
		
		p2.setColor(Color.DKGRAY);
		p.setColor(Color.WHITE);
		p.setTextSize(100);
		p3.setColor(Color.rgb(100, 100, 255));
		p3.setStyle(Paint.Style.FILL);
	}
	
	
	public gameView(Context context, MainActivity main) 
	{
		super(context);
		this.main = main;
		
		pipe = BitmapFactory.decodeResource(getResources(), R.drawable.pipe);
		pipe_b = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_b);
		pipe.setDensity(Bitmap.DENSITY_NONE);
		pipe_b.setDensity(Bitmap.DENSITY_NONE);
		
		genNewBoard();
		
	}
	
	/**
	 * Generates a new board
	 */
	private void genNewBoard()
	{
		int w = 5; int h = 9;
		board = new gameBoard(w, h);
	}
	
	/**
	 * Gets called when board is solved
	 */
	private void winRound()
	{
		score += timeLeft;
		timeLeft = 150;
		genNewBoard();
	}
	
	/**
	 * Gets called when time runs out 
	 */
	private void loseRound()
	{
		main.setContentView(R.layout.game_loss_scree);
	}
	
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() != MotionEvent.ACTION_DOWN) return false;
		
		int ix = (int)(event.getX()/x_spacing);
		int iy = (int)(event.getY()/y_spacing);
		
		if(ix < 0 || ix >= board.w || iy < 0 || iy >= board.h) return false;
		
		gamePiece p = board.getPiece(ix, iy);
		p.rotation = (p.rotation+1)%4; 
		this.postInvalidate();
		
		if(board.verifyBoard())
			winRound();
		
		return true;
	}
	
	@Override
	protected void onDraw(Canvas g) 
	{
		// TODO Auto-generated method stub
		super.onDraw(g);
		int w = g.getWidth();
		int h = g.getHeight()-80;
		g.setDensity(Bitmap.DENSITY_NONE);
		g.drawRGB(0,0,0);
		
		
		if(timeLeft > timeMax - 0.5f)
			p2.setColor(Color.RED);
		else
			p2.setColor(Color.DKGRAY);
		
		float lx = (float)(Math.cos(0)*400 + 300*Math.cos(0)) + w/2;
		float ly = (float)(Math.sin(0)*400 + 300*Math.sin(0)) + h/2;
		for(int i = 0; i < 1000; i++)
		{
			float px = lx;
			float py = ly;
			lx = (float)(Math.cos(i/100.0)*600 + 600*Math.cos(i*Math.sin(time/200.0)*5)) + w/2;
			ly = (float)(Math.sin(i/100.0)*600 + 600*Math.sin(i*Math.sin(time/200.0)*5)) + h/2;
			g.drawLine(px, py, lx, ly, p2);
			
		}
		
		
		
		x_spacing = (float)w/(float)board.w;
		y_spacing = (float)h/(float)board.h;
		
		for(int i = 0; i < board.w; i++)
		{
			for(int j = 0; j < board.h; j++)
			{
				Bitmap b = pipe;
				if(board.array[i][j].type == 1) b = pipe_b;
				
				
				m.reset();
				m.postRotate(90*board.array[i][j].rotation,b.getWidth()/2,b.getHeight()/2);
				m.postScale(x_spacing/b.getWidth(), y_spacing/b.getHeight());
				m.postTranslate(i*x_spacing, j*y_spacing);
				
				g.drawBitmap(b, m, null);
				
				
				
			}
		}
		
		
		g.drawRect(0,h,w*(timeLeft/timeMax),h+80, p3);
		g.drawText("Score:" + score, 0, h+80, p);
		
		postInvalidate();
		time ++;
		timeLeft-= 0.1f;
		if(timeLeft <= 0) loseRound();
		
	}
}
