package basic;


import java.util.Set;


public class Instruction{
	public int index;
	public int opnum;
	public String opcode;
	public String[] Op;
	public String[] Var;
	public int[] kind;
	public String FinalVar;
	public String INS;
	public int notoutput;
	public int eliminated;
	public int def;
	public String gen;//琚畾鍊肩殑鍙橀噺鍚�
	public String[] use = new String[3];
	public String genr = null;
	public String[] user = new String[3];
	public int[] RD_IN;
	public int[] RD_OUT;
	public Set<String> live_IN;
	public Set<String> live_OUT;
	
	public void clear(){
		kind[0] = kind[1] = 0;
		def = notoutput = eliminated = index = opnum = 0;
		FinalVar = INS = opcode = Op[0] = Op[1] = Var[0] = Var[1] = "";
	}
	
	public Instruction(){
		kind = new int[2];
		Op = new String[2];
		Var = new String[2];
		def = notoutput = eliminated = index = opnum = 0;
		FinalVar = INS = opcode = Op[0] = Op[1] = Var[0] = Var[1] = "";
	}
	//todo: 寰楀埌涓�鏉¤鍙ョ殑gen鍙橀噺鍜寀se鍙橀噺!!!
	public void getGenAUse(){
		gen = null;
		use[0]=null;
		use[1]=null;
	}
}