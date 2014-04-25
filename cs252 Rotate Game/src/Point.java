
public class Point {
	int row;
	int col;
	int mark=0;
	
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
}
