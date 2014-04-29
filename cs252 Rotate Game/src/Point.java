
public class Point {
	int row;
	int col;
	int mark=0;
	int value = 0;	//pipe type  ranges from 1-6
					//1: straight vertical, 2: straight horizontal, 3: bend up and right, 4: bend up and left, 
					//5: bend down and left, 6: bend down right
	
	public Point (int a, int b, int c) {
		row=a;
		col=b;
		mark=c;
		
	}
	public void printPoint()
	{
		System.out.print("("+row+","+col+") : "+mark+"\t");
	}
	public int marked()
	{
		return mark;
	}
	public void setPointValue(int v)//sets the value (which pipe) the given point is
	{
		value=v;
	}
}
