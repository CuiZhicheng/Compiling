package basic;

public class BasicBlock{
	public int BlockStartPoint;
	public int SeccessorNum;
	public int[] Successor;
	
	public BasicBlock(){
		Successor = new int[2];
		BlockStartPoint = SeccessorNum = 0;
	}
}