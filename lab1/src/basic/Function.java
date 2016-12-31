package basic;

import java.util.HashMap;
import java.util.Map;

public class Function {
	
	public int FunctionStartPoint;
	public Map<Integer, BasicBlock> FunctionBB;
	public int BlockNumInFunction;
	
	public Function(int line) {
		this.FunctionStartPoint = line;
		this.BlockNumInFunction = 0;
		FunctionBB = new HashMap<Integer, BasicBlock>();
	}
	
}