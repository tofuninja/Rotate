package com.example.rotate;

import java.util.*;

public class Board {
	
	Point [][]gameBoard;
	static Point end;
	static int maxRow;
	static int maxCol;
	int result1;
	
	static Stack<Point> pointStack = new Stack<Point>();
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
		//randomize array locations
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
	
	
	public boolean checkIfCorrect()//should implement a timer later so it's not infinite
	{
		
		if(1==South(gameBoard, gameBoard[0][0]))//replace with south function
			return true;
		
		if(1==East(gameBoard, gameBoard[0][0]))//replace with east function
			return true;
		
		return false;
	}
	
	public int South(Point [][]board, Point currentPoint)
	{
		//TODO check if exit (a-1,b-1)...so end pt row/col
		//do this for North West and East too
		if(currentPoint.row==maxRow && currentPoint.col==maxCol)
			return 1;		//check current position is (a-1, b-1) which means end point.
		
		if (currentPoint.value==1)
		{
			//TODO
			//check if calling point is valid  i.e) currentPoint.row+1 is out of bounds or not
			//if it is out of bounds then return 0
			//you have to do this here for every if else that calls another function i.e) South North West East
			//meaning you should do this for 3*4directions=12times
			//hint just write code for south call east call north call west call and copy and past for each of the different calls
			//this way you only have to write 4 different test cases
			
			if(currentPoint.row+1>maxRow)
				return 0;

			return South(board, board[currentPoint.row+1][currentPoint.col]);
		}
		
		else if(currentPoint.value==3)
		{
			if(currentPoint.col+1>maxCol)
				return 0;
			
			return East(board, board[currentPoint.row][currentPoint.col+1]);
			
		}
		
		else if(currentPoint.value==4)
		{
			if(currentPoint.col-1<0)
				return 0;
			
			return West(board, board[currentPoint.row][currentPoint.col-1]);
		}
		
		else return 0;
	}
	
	public int North(Point [][]board, Point currentPoint)
	{
		if(currentPoint.row==maxRow && currentPoint.col==maxCol)
			return 1;		//check current position is (a-1, b-1) which means end point.
		
		if(currentPoint.value==1)
		{
			//check if valid otherwise return 0;
			if(currentPoint.row-1<0)
				return 0;			

			return North(board, board[currentPoint.row-1][currentPoint.col]);
		}
		else if(currentPoint.value==5)
		{
			if(currentPoint.col-1<0)
				return 0;
			
			return West(board, board[currentPoint.row][currentPoint.col-1]);
		}
		else if(currentPoint.value==6)
		{
			if(currentPoint.col+1>maxCol)
				return 0;
			
			return East(board, board[currentPoint.row][currentPoint.col+1]);
		}
		else return 0;
	}
	
	public int West(Point [][]board, Point currentPoint)
	{
		if(currentPoint.row==maxRow && currentPoint.col==maxCol)
			return 1;		//check current position is (a-1, b-1) which means end point.
		
		if (currentPoint.value==2)
		{
			//check if valid otherwise return 0;
			if(currentPoint.col-1<0)
				return 0;			

			return North(board, board[currentPoint.row][currentPoint.col-1]);
		}
		else if(currentPoint.value==3)
		{
			if(currentPoint.row-1<0)
				return 0;
			
			return North(board, board[currentPoint.row-1][currentPoint.col]);
		}
		else if(currentPoint.value==6)
		{
			if(currentPoint.row+1>maxRow)
				return 0;
			
			return South(board, board[currentPoint.row+1][currentPoint.col]);
		}
		else return 0;
	}
	
	public int East(Point [][]board, Point currentPoint)
	{
		if(currentPoint.row==maxRow && currentPoint.col==maxCol)
			return 1;		//check current position is (a-1, b-1) which means end point.
		
		if (currentPoint.value==2)
		{
			//check if valid otherwise return 0;			
			if(currentPoint.col+1<maxCol)
				return 0;			

			return East(board, board[currentPoint.row][currentPoint.col+1]);
		}
		else if(currentPoint.value==4)
		{
			if(currentPoint.row-1<0)
				return 0;
			
			return North(board, board[currentPoint.row-1][currentPoint.col]);
		}
		else if(currentPoint.value==6)
		{
			if(currentPoint.row+1>maxRow)
				return 0;
			
			return South(board, board[currentPoint.row+1][currentPoint.col]);
		}
		else return 0;
	}
}

