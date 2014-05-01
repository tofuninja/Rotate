package com.example.rotate;

public class Game {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Board myGameBoard=new Board(6,6);		//we need to indicate the direction when we start
												//for case (9,1), we don't need to bend at the end point, but it bends
												//for case (1,9), same as above
												//program randomly bend at the end of the point!!! even under same test case!!!
												//
		//int val=myGameBoard.DFS();
		System.out.println("the DFS returned "+myGameBoard.result1);
		myGameBoard.printBoard();
	}

}
