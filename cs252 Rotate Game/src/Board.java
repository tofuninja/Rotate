import java.util.*;
public class Board {
	
	Point [][]gameBoard;
	static Point end;
	static int maxRow;
	static int maxCol;
	
	
	public Board(int a,int b)
	{
		maxRow=a;
		maxCol=b;
		gameBoard=new Point[a][b];
		end=new Point(a,b-1,0);
		Point start=gameBoard[0][0];
		int resul=DFS(gameBoard,start);
		
	}
	
	//returns 1 if found a board
	static int DFS(Point [][]board, Point myPoint)
	{
		myPoint.mark=1;
		if(end.mark==1)
		{
			return 1;//solution found			
		}
		//make an array of 4 points for all directions from current point
		Point north,south,west,east;
		if(myPoint.row-1<0)
			north=null;
		else
			north=board[myPoint.row-1][myPoint.col];
		
		if(myPoint.col+1>maxCol)
			south=null;
		else
			south=board[myPoint.row+1][myPoint.col]; 
		///////////////////////////
		//left off here  matane//
		//bangohan wo teberu//2014/4/24 /7:36
		///////////////////////
		west=board[myPoint.row][myPoint.col-1];
		east=board[myPoint.row][myPoint.col+1];
		Point []locations={north,south,west,east};
		
		
		
		
		
		return 0;
	}
	
}
