package com.example.rotate;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class whiteBackBoarder extends Drawable
{
	private static Paint p1 = new Paint();
	private static Paint p2 = new Paint();
	
	static
	{
		p1.setColor(Color.WHITE);
		p2.setColor(Color.BLACK);
	}

	@Override
	public void draw(Canvas g) {
		int w = g.getWidth();
		int h = g.getHeight();
		g.drawRoundRect(new RectF(0, 0, w, h), 15, 15, p1);
		g.drawRoundRect(new RectF(1, 1, w-1, h-1), 15, 15, p2);
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
