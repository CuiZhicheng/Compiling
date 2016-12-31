package basic;

import java.util.regex.Pattern;

public class Instruction{
	public int index;
	public int opnum;
	public String opcode;
	public String[] Type;
	public String[] Op;
	public String[] Var;
	public int[] kind;
	public String FinalVar;
	public String INS;
	public int notoutput;
	public int eliminated;
	public int def;
	
	public void clear(){
		kind[0] = kind[1] = 0;
		def = notoutput = eliminated = index = opnum = 0;
		FinalVar = INS = opcode = Op[0] = Op[1] = Var[0] = Var[1] = Type[0] = Type[1] = "";
	}
	
	public Instruction(){
		kind = new int[2];
		Op = new String[2];
		Var = new String[2];
		Type = new String[2];
		def = notoutput = eliminated = index = opnum = 0;
		FinalVar = INS = opcode = Op[0] = Op[1] = Var[0] = Var[1] = Type[0] = Type[1] = "";
	}	
	
	public void setParam(int i, String t, String V, String O) {
		this.Type[i] = t;
		this.Var[i] = V;
		this.Op[i] = O;
	}
	
	public static boolean isNum(String s) {    
	    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");    
	    return pattern.matcher(s).matches();    
	  }  
	
	public void parseParam(int i, String param) {
		int pos = -1;
		String p = param;
		if (p.equals("GP")) 
			setParam(i, "GP", "", "");
		else if (p.equals("FP"))
			setParam(i, "FP", "", "");
		else if (isNum(p))
			setParam(i, "CONST", p, p);
		else if (p.charAt(0) == '(' && p.charAt(p.length() - 1) == ')')
				setParam(i, "REG", p.substring(1, p.length() - 1), p.substring(1, p.length() - 1));
		else if (p.charAt(0) == '[' && p.charAt(p.length() - 1) == ']')
			setParam(i, "LABEL", p.substring(1, p.length() - 1), p.substring(1, p.length() - 1));
		else if ((pos = p.indexOf("#")) > 0) {
			String left = p.substring(0, pos);
			String right = p.substring(pos + 1);
			int _pos = left.indexOf("_base");
			if (_pos >= 0)
				setParam(i, "BASE", right, left.substring(0, _pos));
			else {
				_pos = left.indexOf("_offset");
				if (_pos >= 0)
					setParam(i, "OFFSET", right, left.substring(0, _pos));
				else {
					int var = Integer.valueOf(right);
					if (var < 0)
						setParam(i, "LOCAL_VAR", right, left);
					else 
						setParam(i, "PARAM", right, left);
				}
					
			} 
		}
		else 
			setParam(i, "", "", "");			 
	}	
}