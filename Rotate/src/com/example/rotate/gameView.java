package com.example.rotate;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.renderscript.Matrix2f;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class gameView extends View 
{
	private Bitmap pipe;
	private Bitmap pipe_b;
	
	private int gameWidth;
	private int gameHeight;
	
	private int[][] rotations; 
	private int[][] types; 
	public gameView(Context context) 
	{
		super(context);
		pipe = BitmapFactory.decodeResource(getResources(), R.drawable.pipe);
		pipe_b = BitmapFactory.decodeResource(getResources(), R.drawable.pipe_b);
		pipe.setDensity(Bitmap.DENSITY_NONE);
		pipe_b.setDensity(Bitmap.DENSITY_NONE);
		setGameSize(5,10);
		p.setColor(Color.BLACK);
	}
	
	public void setGameSize(int w, int h)
	{
		gameWidth = w;
		gameHeight = h;
		rotations = new int[w][h];
		types = new int[w][h];
		
		Random r = new Random();
		
		for(int i = 0; i < gameWidth; i++)
		{
			for(int j = 0; j < gameHeight; j++)
			{
				rotations[i][j] = r.nextInt(4);
				types[i][j] = r.nextInt(2);
			}
		}
	}

	
	private Matrix m = new Matrix();
	private Paint p = new Paint();
	float x_spacing;
	float y_spacing;
	
	@Override
	protected void onDraw(Canvas g) 
	{
		// TODO Auto-generated method stub
		super.onDraw(g);
		int w = g.getWidth();
		int h = g.getHeight()-10;
		g.setDensity(Bitmap.DENSITY_NONE);
		
		x_spacing = (float)w/(float)gameWidth;
		y_spacing = (float)h/(float)gameHeight;
		
		for(int i = 0; i < gameWidth; i++)
		{
			for(int j = 0; j < gameHeight; j++)
			{
				int r = rotations[i][j];
				int t = types[i][j];
				Bitmap b = pipe;
				if(t == 1) b = pipe_b;
				m.reset();
				
				m.postRotate(90*r,b.getWidth()/2,b.getHeight()/2);
				m.postScale(x_spacing/b.getWidth(), y_spacing/b.getHeight());
				m.postTranslate(i*x_spacing, j*y_spacing);
				
				g.drawBitmap(b, m, null);
				
				
			}
		}
		
		g.drawText("w" + w + "h" + h + "xs"+ x_spacing + "ys" + y_spacing+ "    pw" + pipe.getWidth() + "     ph" + pipe.getHeight(),20,20,p);
		
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() != MotionEvent.ACTION_DOWN) return false;
		
		int ix = (int)(event.getX()/x_spacing);
		int iy = (int)(event.getY()/y_spacing);
		
		if(ix < 0 || ix >= gameWidth || iy < 0 || iy >= gameHeight) return false;
		rotations[ix][iy] = (rotations[ix][iy]+1)%4; 

		this.postInvalidate();
		Log.d("ROTATE","ix:" + ix + "iy:" + iy + "value:" + rotations[ix][iy]);
		
		return true;
	}
}
