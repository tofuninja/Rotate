package com.example.rotate;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class coolView extends Drawable
{
	private int time = 0;
	private static Paint p2 = new Paint();
	
	static
	{
		p2.setColor(Color.DKGRAY);
	}
	
	public coolView() {
	}

	@Override
	public void draw(Canvas g) {
		int w = g.getWidth();
		int h = g.getHeight()-80;
		g.drawRGB(0,0,0);
		Log.d("ROTATE","w:" + w + "h:" + h);
		
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
		
		this.invalidateSelf();
		time ++;
	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 255;
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub
		
	}
}
