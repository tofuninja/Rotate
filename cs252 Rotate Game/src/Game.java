
public class Game {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Board myGameBoard=new Board(5,5);
		//int val=myGameBoard.DFS();
		System.out.println("the DFS returned "+myGameBoard.result1);
		myGameBoard.printBoard();
	}

}
