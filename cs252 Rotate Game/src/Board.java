import java.util.*;

public class Board {
	
	Point [][]gameBoard;
	static Point end;
	static int maxRow;
	static int maxCol;
	int result1;
	
	static Stack pointStack = new Stack();
	static int pointStackCount=0;
	 
	public void printBoard()
	{
		for(int i=0; i<maxRow; i++){
			for(int j=0; j<maxCol; j++)
			{
				gameBoard[i][j].printPoint();
			}
			System.out.println("");
		}
	}
	public Board(int a,int b)
	{
		
		maxRow=a;
		maxCol=b;
		gameBoard=new Point[a][b];
		
		//run a loop to initialize all the points
		for(int i=0; i<a; i++) {
				for (int j=0; j<b; j++) {
					gameBoard[i][j]=new Point(i,j,0);
					
				}
		}
		end=gameBoard[a-1][b-1];
		pointStack.push(end);
		//Point start=gameBoard[0][0];
		
		//System.out.println(re+" ok");
		
		int res=DFS(gameBoard,gameBoard[0][0]);
		//System.out.println("now here is the direction trace");
		setDirections(gameBoard);
		result1=res;
	}
	
	static void setDirections(Point [][]board)
	{
		Point first=(Point)pointStack.pop();
		pointStackCount--;
		while(0!=pointStackCount--)
		{
			Point middle=(Point)pointStack.pop();
			Point end=(Point)pointStack.peek();
			//middle.printPoint();
			
			if((first.row==middle.row && first.row==end.row)||(first.col==middle.col && first.col==end.col))
			{
				//strait
				middle.mark=1;
			}
			else
			{
				//curve
				middle.mark=2;
			}
			first=middle;
		}
		if(Math.random()>0.5)
			board[0][0].mark=2;
		if(Math.random()>0.5)
			end.mark=2;
	}
	
	//returns 1 if found a board
	static int DFS(Point [][]board, Point myPoint)
	{
		//myPoint.printPoint();
		//System.out.println("run on function");
		if(myPoint==null)
			System.out.println("error");
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
		
		if(myPoint.row+1>=maxRow)
			south=null;
		else
			south=board[myPoint.row+1][myPoint.col]; 
		if(myPoint.col-1<0)
			west=null;
		else
			west=board[myPoint.row][myPoint.col-1];
		if(myPoint.col+1>=maxCol)
			east=null;
		else
			east=board[myPoint.row][myPoint.col+1];
		Point []locations={north,south,west,east};
		//randomize array loactions
		if(Math.random()>0.5)
		{
			Point temp=locations[0];
			locations[0]=locations[1];
			locations[1]=temp;
			
		}
		if(Math.random()>0.5)
		{
			Point temp=locations[1];
			locations[1]=locations[2];
			locations[2]=temp;
			
		}
		if(Math.random()>0.5)
		{
			Point temp=locations[2];
			locations[2]=locations[3];
			locations[3]=temp;
			
		}
		if(Math.random()>0.5)
		{
			Point temp=locations[0];
			locations[0]=locations[2];
			locations[2]=temp;
			
		}
		if(Math.random()>0.5)
		{
			Point temp=locations[0];
			locations[0]=locations[3];
			locations[3]=temp;
			
		}
		//start with locations [0] then go through them all and call the function recursively
		
		if((locations[0]!=null)&&(locations[0].marked()==0))
		{
			int ret=DFS(board,locations[0]);
			if(ret==1)
			{
				pointStack.push(myPoint);
				pointStackCount++;
				return 1;//this means it's done
			}
			
		}
		if((locations[1]!=null)&&(locations[1].marked()==0))
		{
			int ret=DFS(board,locations[1]);
			if(ret==1)
			{
				pointStack.push(myPoint);
				pointStackCount++;
				return 1;//this means it's done
			}
		}
		if((locations[2]!=null)&&(locations[2].marked()==0))
		{
			int ret=DFS(board,locations[2]);
			if(ret==1)
			{
				pointStack.push(myPoint);
				pointStackCount++;
				return 1;//this means it's done
			}
		}
		if((locations[3]!=null)&&(locations[3].marked()==0))
		{
			int ret=DFS(board,locations[3]);
			if(ret==1)
			{
				pointStack.push(myPoint);
				pointStackCount++;
				return 1;//this means it's done
			}
		}
			
		
		
		
		myPoint.mark=0;
		return 0;
	}
	
}
