package basic;

import java.util.*;

public class Function {
	public int FunctionStartPoint; //是行号!
	public Map<Integer, BasicBlock> FunctionBB;
	public int BlockNumInFunction;
	public Instruction[] Ins;
	public VariableTable vt;
	public Vector<PropagationList> prolist;
	//构造函数
	public Function(int line) {
		this.FunctionStartPoint = line;
		this.BlockNumInFunction = 0;
		FunctionBB = new HashMap<Integer, BasicBlock>();
	}
	//把符号表和指令数组传进来使用,因为
	public void setInstr(Instruction[] In,VariableTable v){
		Ins = In;
		vt = v;
	}

	//在函数中求Reaching Definition
	public void ReachDef(LabFile lf) {
		Vector<BasicBlock> Changed = new Vector<BasicBlock>();
		for (Map.Entry<Integer, BasicBlock> entry : FunctionBB.entrySet()) {
			for(int i= 0 ;i<entry.getValue().maxInstru;i++) {
				entry.getValue().RD_OUT[i] = entry.getValue().Gen[i];
			}
			Changed.add(entry.getValue());
		}
		int i = 0;
		BasicBlock iBB;
		int total=Changed.size();
		while(true){
			iBB = Changed.elementAt(i);
			i++;
			if(iBB==null) {
				break;
			}
			int[] addNew = iBB.cululateInOut(lf);
			if(addNew!=null){
				for (Integer j:addNew){
					if(!Changed.contains(FunctionBB.get(j))) {
						Changed.add(FunctionBB.get(j));
						total++;
					}
				}
			}
			if(i>=total)break;
		}
	}
	public void printRD(){
		for (Map.Entry<Integer, BasicBlock> entry : FunctionBB.entrySet()) {
			entry.getValue().print_In_Out();
		}
	}
	//活跃变量分析
	public void live_Va(LabFile lf){
		Vector<BasicBlock> Changed = new Vector<BasicBlock>();
		Stack<BasicBlock> stack = new Stack<BasicBlock>();
		for (Map.Entry<Integer, BasicBlock> entry : FunctionBB.entrySet()) {
			for(int i= 0 ;i<entry.getValue().maxInstru;i++) {
				entry.getValue().RD_OUT[i] = entry.getValue().Gen[i];
			}
			stack.add(entry.getValue());
			//Changed.add(entry.getValue());
		}
		while (stack.size()>0){
			Changed.add(stack.pop());
		}
		int i = 0;
		BasicBlock iBB;
		int total=Changed.size();
		while(true){
			iBB = Changed.elementAt(i);
			i++;
			if(iBB==null) {
				break;
			}
			int[] addNew = iBB.cal_liveVable(lf,i);
			if(addNew!=null){
				iBB.printlive_In_Out();
				for (Integer j:addNew){
					if(!Changed.contains(FunctionBB.get(j))) {
						Changed.add(FunctionBB.get(j));
						total++;
					}
				}
			}
			if(i>=total)break;
		}
	}
	public void printlive(){
		for (Map.Entry<Integer, BasicBlock> entry : FunctionBB.entrySet()) {
			entry.getValue().printlive_In_Out();
		}
	}
	//常数传播
	public Vector<PropagationList> const_Propagation(Instruction[] Ins){
		String ver;
		Instruction instruction = null;
		int instr;
		int num;
		prolist =new Vector<PropagationList>();
		for (Map.Entry<String, Set<VariableTable.constPair>> entry : vt.ConstDef.entrySet()) { //对于所有被常数定义过的字符串
			ver = entry.getKey();
			Set<VariableTable.constPair> conset = entry.getValue();
			Set<Integer> useSet = vt.useSet.get(ver);
			for(VariableTable.constPair p: conset){  //对于a的所有常数赋值<instr,num>
				instr = p.instr;
				num = p.number;
				if(useSet == null) {
					//System.out.println("use no "+ver);
					break;
				}
				for(Integer i:useSet){//对于a的所有被使用行
					instruction = Ins[i];
					if(instruction.RD_IN[instr]==1 && instruction.RD_OUT[instr]==1){//如果这一行中instr的入出都是1
						int flag = 0;
						for(VariableTable.constPair b: conset){
							if(instruction.RD_IN[b.instr]==1 && b.instr !=instr){ //并且没有别的可能进入的a的定值
								flag = 1;
								break;
							}
						}
						if(flag==0){ //就加入对<instr,v,number>
							PropagationList tem = new PropagationList();
							tem.instr = i;
							tem.number = num;
							if(instruction.use[0]!=null && instruction.use[0].equals(ver)){
								tem.v = instruction.user[0];
							}
							else {
								if(instruction.use[1]!=null && instruction.use[1].equals(ver)){
									tem.v = instruction.user[1];//v要保留原始的字符串,
								}
							}
							System.out.print(tem.toString());
							prolist.add(tem);//加入对
						}
					}
				}
			}

		}
		return prolist;
	}
	//死代码消除
	public Vector<Integer> deadCodeEliminate(Instruction[] Ins){
		System.out.println("Global Variable and Array: "+vt.isArray);
		Vector<Integer> result = new Vector<Integer>();
		for (Map.Entry<String,Set<Integer>> entry : vt.defSet.entrySet()) {
			if(!vt.isArray.contains(entry.getKey())){
				for(Integer index:entry.getValue()){
					if(!Ins[index].live_OUT.contains(entry.getKey())){
						result.add(index);
						System.out.println("Eliminate "+index+" "+ entry.getKey());
					}
				}
			}
		}
		return result;
	}
}