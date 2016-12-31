import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import basic.Instruction;

public class Trans{
	
	public Instruction[] Ins;
	public String[] InitInstr;
	public int[] LabelLine;
	public int LabelNum;
	public int TabNum;
	public Vector<String> Statement;
	public Map<Integer, String>Function;
	public int Entry;
	public boolean IsMain;
	LabFile LF2Trans;
	public int InstructionNum;
	public int FunctionNum;
	
	Trans(LabFile lf) {
		LF2Trans = lf;
	}
	
	public void translate(){
		InputInstruction();
		trans_3ddr_to_c();
	}
	
	public void InputInstruction(){
		try {
			File f = new File(LF2Trans.FilePath);
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
	        String s;
	        int i = 0;
	        int num = 1;
	        String[] Str = new String[1000000];
	        while ((s = br.readLine()) != null) {
	        	Str[num] = s;
	        	num++;
	        }
	        InstructionNum = num - 1;
	        InitInstr = new String[num];
	        for (i = 1; i <= InstructionNum; i++)
	        	InitInstr[i] = Str[i];
	        Ins = new Instruction[num];
	        for (i = 0; i < Ins.length; i++)
	        	Ins[i] = new Instruction();
	        TabNum = 0;
	        FunctionNum = 0;
	        Function = new HashMap<Integer, String>();
	        LabelLine = new int[num];
	        Arrays.fill(LabelLine, 0);
	        LabelNum = FunctionNum = 0;
	        br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void trans_3ddr_to_c() {
		String res = "";
		Statement = new Vector<String>();
		parse(InitInstr);
		String[] temp = LF2Trans.FileName.split("\\.");
		String outputpath = LF2Trans.FileOutputPath + temp[0] + ".c";
		try {
			File f = new File(LF2Trans.FileOutputPath);
			if (!f.exists())
				f.mkdirs();
			FileOutputStream fos = new FileOutputStream(outputpath); 
	        OutputStreamWriter bw = new OutputStreamWriter(fos, "UTF-8");
	        res = "#include <stdio.h>\r\n";
	        res = res + "#define WriteLine() printf(\"\\n\");\r\n";
	        res = res + "#define WriteLong(x) printf(\" %lld\", (long)x);\r\n";
	        res = res + "#define ReadLong(a) if (fscanf(stdin, \"%lld\", &a) != 1) a = 0;\r\n";
	        res = res + "#define ReadLong(a) if (fscanf(stdin, \"%lld\", &a) != 1) a = 0;\r\n";
	        res = res + "#define long long long\r\n";
	        Statement.add(res);
	        //bw.write(res);
	        Entry = 1;
	        findGlobalVar();
	        
	        IsMain = false;
	        //check();
	        while (Entry <= InstructionNum && translateboolean()) {
	        	//System.out.println("right");
	        	//Output(bw);
	        	IsMain = false;
	        }
	        Output(bw);
	        bw.close();
	        fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void check(){
		int i;
		for (i = Entry; i <= InstructionNum; i++)
			if (LabelLine[i] != 0) 
				System.out.println(i);
	}
	
	public void Output(OutputStreamWriter bw) throws IOException{
		int i;
		String res = "";
		for (i = 0; i < Statement.size(); i++) {
			 res = Statement.get(i);
			 bw.write(res);
		}
	}
	
	public void parse(String[] Is){
		int index = 1;
		while (index <= InstructionNum) {
			//System.out.println(Is[index]);
			String[] temp = Is[index].split(" ");
			Ins[index].index = index;
			Ins[index].opcode = temp[6];
			//System.out.println(temp.length);
			Ins[index].opnum = temp.length - 7;
			//System.out.println(Ins[index].opnum);
			if (Ins[index].opcode.equals("enter")) {
				Function.put(index, "Function" + FunctionNum++);
			}
			switch (Ins[index].opnum) {
				case 0:
					Ins[index].setParam(0, "", "", "");
					Ins[index].setParam(1, "", "", "");
					break;
				case 1:
					Ins[index].parseParam(0, temp[7]);
					Ins[index].setParam(1, "", "", "");
					if (Ins[index].Type[0].equals("LABEL")) { 
						// br [x] jump up or down...
						int fir = Integer.valueOf(Ins[index].Var[0]);
						if (fir > index) {
							//jump down
							//System.out.println("br down:" + fir);
							LabelLine[fir]++;
						}
						
					}
					break;
				case 2:
					//System.out.println(temp[3] + " " + temp[4]);
					Ins[index].parseParam(0, temp[7]);
					Ins[index].parseParam(1, temp[8]);
					if (Ins[index].Type[1].equals("LABEL"))
						//blbc blcs jump down
						LabelLine[Integer.valueOf(Ins[index].Var[1])]++;
					break;
			}
			//System.out.println(Is[index] +" 1:" + Ins[index].Op[0] + "| 2:" + Ins[index].Op[1]);
			index++;
		}
	}
	
	public void findGlobalVar(){
		//Statement.clear();
		String res = "\r\n";
		String[] GlobalVar;
		int GlobalNum = 0;
		int num = 0;
		int[] GlobalAddr;
		int lastaddr = 32768;
		int curaddr = 0;
		int i = 0, j = 0;
		for (i = 1; i <= InstructionNum; i++) 
			if (Ins[i].Type[1].equals("GP"))
				num++;
		if (num == 0 ) return;
		GlobalVar = new String[num];
		GlobalAddr = new int[num];
		int ischanged = 1;
		while (ischanged == 1) {
			curaddr = 0;
			ischanged = 0;
			for (i = 1; i <= InstructionNum; i++) {
				if (Ins[i].Type[1].equals("GP")) {
					
					int addr = Integer.valueOf(Ins[i].Var[0]);
					//System.out.println(addr);
					if (addr < lastaddr && addr > curaddr) {
						curaddr = addr;
						j = i;
						ischanged = 1;
					}
				}
			}
			if (ischanged == 1) {
				GlobalVar[GlobalNum] = Ins[j].Op[0];
				GlobalAddr[GlobalNum++] = Integer.valueOf(Ins[j].Var[0]);
				lastaddr = curaddr;
			}
		}
		lastaddr = 32768;
		for (i = 0; i < GlobalNum; i++) {
			res = res + "long " + GlobalVar[i];
			j = (lastaddr - GlobalAddr[i]) / 8;
			if (j == 1)
				res = res + ";\r\n";
			else 
				res = res + "[" + j + "];\r\n";	
			lastaddr = GlobalAddr[i];
		}
		res = res + "\r\n";
		//System.out.println(res);
		Statement.add(res);
		return;
	}
	
	public void findLocalVar(int start, int end){
		//Statement.clear();
		String res = "";
		String[] LocalVar;
		int LocalNum = 0;
		int num = 0;
		int[] LocalAddr;
		int lastaddr = 0;
		int curaddr = -1000000;
		int i = 0, j = 0;
		String tab = "";
		for (j = 0; j < TabNum; j++)
			tab = tab + "	";
		for (i = start; i <= end; i++) 
			if (Ins[i].Type[1].equals("FP") || Ins[i].Type[0].equals("LOCAL_VAR") || Ins[i].Type[1].equals("LOCAL_VAR"))
				num++;
		if (num == 0 ) return;
		LocalVar = new String[num];
		LocalAddr = new int[num];
		int ischanged = 1;
		while (ischanged == 1) {
			curaddr = -1000000;
			ischanged = 0;
			String op = "";
			for (i = start; i <= end; i++) {
				if (Ins[i].Type[1].equals("FP")) {
					int addr = Integer.valueOf(Ins[i].Var[0]);
					if (addr < lastaddr && addr > curaddr) {
						op = Ins[i].Op[0];
						curaddr = addr;
						j = i;
						ischanged = 1;
					}
				}
				if (Ins[i].Type[0].equals("LOCAL_VAR")) {
					int addr = Integer.valueOf(Ins[i].Var[0]);
					if (addr < lastaddr && addr > curaddr) {
						op = Ins[i].Op[0];
						curaddr = addr;
						j = i;
						ischanged = 1;
					}
				}
				if (Ins[i].Type[1].equals("LOCAL_VAR")) {
					int addr = Integer.valueOf(Ins[i].Var[1]);
					if (addr < lastaddr && addr > curaddr) {
						op = Ins[i].Op[1];
						curaddr = addr;
						j = i;
						ischanged = 1;
					}
				}
			}
			if (ischanged == 1) {
				LocalVar[LocalNum] = op;
				LocalAddr[LocalNum++] = curaddr;
				lastaddr = curaddr;
			}
		}
		lastaddr = 0;
		for (i = 0; i < LocalNum; i++) {
			res = res + tab + "long " + LocalVar[i];
			j = (lastaddr - LocalAddr[i]) / 8;
			if (j == 1)
				res = res + ";\r\n";
			else 
				res = res + "[" + j + "];\r\n";	
			lastaddr = LocalAddr[i];
		}
		//System.out.println(res);
		Statement.add(res);
		return;
	}
	
	@SuppressWarnings("unused")
	public String findParam(int start, int end) {
		String res = "";
		String[] GlobalVar;
		int GlobalNum = 0;
		int num = 0;
		int[] GlobalAddr;
		int lastaddr = 100000;
		int curaddr = 0;
		int i = 0, j = 0;
		for (i = start; i <= end; i++) { 
			if (Ins[i].Type[0].equals("PARAM") || Ins[i].Type[1].equals("PARAM"))
				num++;
		}
		if (num == 0 ) return res;
		GlobalVar = new String[num];
		GlobalAddr = new int[num];
		int ischanged = 1;
		String op = "";
		while (ischanged == 1) {
			curaddr = 0;
			ischanged = 0;
			for (i = start; i <= end; i++) {
				if (Ins[i].Type[0].equals("PARAM")) {
					int addr = Integer.valueOf(Ins[i].Var[0]);
					if (addr < lastaddr && addr > curaddr) {
						op = Ins[i].Op[0];
						curaddr = addr;
						j = i;
						ischanged = 1;
					}
				}
				if (Ins[i].Type[1].equals("PARAM")) {
					int addr = Integer.valueOf(Ins[i].Var[1]);
					if (addr < lastaddr && addr > curaddr) {
						op = Ins[i].Op[1];
						curaddr = addr;
						j = i;
						ischanged = 1;
					}
				}
			}
			if (ischanged == 1) {
				GlobalVar[GlobalNum] = op;
				GlobalAddr[GlobalNum++] = curaddr;
				lastaddr = curaddr;
			}
		}
		for (i = 0; i < GlobalNum - 1; i++) {
			res = res + "long " + GlobalVar[i] + ", ";
		}
		res = res + "long " + GlobalVar[i];
		return res;
	}
	
	@SuppressWarnings("unused")
	boolean translateboolean(){
		Vector<String> VParam = new Vector<String>();
		int vparam = 0;
		int i = Entry;
		int j = 0;
		int End = 0;
		int nVar = 0;
		int nParam = 0;
		TabNum = 0;
		int[] jumptime = new int[InstructionNum];
		String res = "";
		// find function entry and return line
		while (i <= InstructionNum && !Ins[i].opcode.equals("enter")) {
			if (Ins[i].opcode.equals("entrypc"))
				IsMain = true;
			i++;
		}

		if (i > InstructionNum)
			return false;
		else {
			nVar = Integer.valueOf(Ins[i].Var[0]) / 8;
			Entry = i;
		}
		while (i <= InstructionNum && !Ins[i].opcode.equals("ret")) {
			i++;
		}

		if (i > InstructionNum)
			return false;
		else {
			nParam = Integer.valueOf(Ins[i].Var[0]) / 8;
			End = i;
		}
		
		//find function param
		String param = findParam(Entry, End);
		
		i = Entry;
		if (IsMain) 
			res = "void main() {\r\n";
		else 
			res = "void " + Function.get(i) + "(" + param + ") {\r\n";
		TabNum++;
		Statement.add(res);
		i++;
		
		// find all local variables
		findLocalVar(Entry, End);
		
		Map<Integer, String> TempVar = new HashMap<Integer, String>(); 
		
		res = "";
		while (i  <= End) {
			res = "";
			for (j = 1; j <= TabNum; j++)
				res = res + "	";
			
			int fir;
			int sec;
			String op1, op2, op;
			
			
			
			if (LabelLine[i] > 0) {
				j = i - 1;
				if (!Ins[j].opcode.equals("br")) {
					for (int it = 1; it <= LabelLine[i]; it++) {
						TabNum--;
						res = "";
						for (j = 0; j < TabNum; j++)
							res = res + "	";
						res = res + "}\r\n";
						Statement.add(res);
					}
				}
			}
			
			res = "";
			//System.out.println(i + " " + res + "");
			for (j = 1; j <= TabNum; j++)
				res = res + "	";
			
			//System.out.println(i);
			switch (Ins[i].opcode) {
				case "add":
					//base address of array
					if (Ins[i].Type[0].equals("BASE")) {
						//instr 4: add c_basef-48 FP
						//op = "c["
						TempVar.put(i, Ins[i].Op[0]);
						i++;
						while (!Ins[i].opcode.equals("load") && !Ins[i].opcode.equals("store")) {
							//opcodes following "add x_base GP/FP" are "add" / "load" / "store"
							if (!Ins[i].opcode.equals("add")) break;
							if (Ins[i].Type[1].equals("OFFSET")) {
								//add (4) z_offset#16
								//sec = 2
								//fir = 4
								sec = Integer.valueOf(Ins[i].Var[1]) / 8;
								fir = Integer.valueOf(Ins[i].Var[0]); 
								op = TempVar.get(fir);
								//System.out.println(op);
								if (Ins[fir].Type[0].equals("BASE")) {
									//instr 4: add c_base#-48 FP
								    //instr 5: add (4) z_offset#16
									op = op + "[" + sec;
									//System.out.println(op);
									// op = "c[2"
								} else if (Ins[fir].Type[1].equals("OFFSET")) {
								    //instr 4: add c_base#-48 FP
								    //instr 5: add (4) z_offset#16
								    //instr 6: add (5) r_offset#8
									//op = "c[2"
									String[] temp = op.split("\\[");
									int lastoffset = Integer.valueOf(temp[temp.length - 1]);
									int newoffset = sec + lastoffset;
									op = temp[0] + "[" + newoffset;
									//op = "c[3"
								}
								else {
									op = op + "+" + sec;
								}
								TempVar.put(i, op);
							} else if (Ins[i].Type[1].equals("REG")) {
								//instr 34: add b_base#32608 GP
							    //instr 35: add (34) (33)
								sec = Integer.valueOf(Ins[i].Var[1]);
								fir = Integer.valueOf(Ins[i].Var[0]); 
								if (Ins[sec].opcode.equals("mul")) {
									//instr 33: mul (32) 40
									//instr 34: add b_base#32608 GP
								    //instr 35: add (34) (33)
									//fir = 34, sec = 33
									String lastop = TempVar.get(sec);
									//System.out.println(lastop);
									String[] temp = lastop.split("\\*");
									int lastnum = Integer.valueOf(temp[temp.length - 1]) / 8;
									int pos = lastop.lastIndexOf("*");
									lastop = lastop.substring(0, pos);
									op = TempVar.get(fir) + "[(" +lastop + ")*(" + lastnum + ")";
									//op = "b[(...0*(5)"
									TempVar.put(i, op);
								}
							}
							i++;
						}
						i--;
					}
					else {
						//instr 99: load (98)
					    //instr 100: mul (99) 256
					    //instr 101: add (100) 255
						
						//instr 25: add x#-64 1
						fir = Integer.valueOf(Ins[i].Var[0]);
						sec = Integer.valueOf(Ins[i].Var[1]);
						if (Ins[i].Type[0].equals("PARAM") || Ins[i].Type[0].equals("LOCAL_VAR") || Ins[i].Type[0].equals("CONST") )
							op1 = Ins[i].Op[0];
						else
							op1 = TempVar.get(fir);
						if (Ins[i].Type[1].equals("PARAM") || Ins[i].Type[1].equals("LOCAL_VAR") || Ins[i].Type[1].equals("CONST") )
							op2 = Ins[i].Op[1];
						else
							op2 = TempVar.get(sec);
						if (Ins[i].Type[1].equals("CONST"))
							op2 = String.valueOf(sec);
						op = op1 + "+" + op2;
						TempVar.put(i, op);
					}						
					break;
				case "sub":
					fir = Integer.valueOf(Ins[i].Var[0]);
					sec = Integer.valueOf(Ins[i].Var[1]);
					if (Ins[i].Type[0].equals("PARAM") || Ins[i].Type[0].equals("LOCAL_VAR") || Ins[i].Type[0].equals("CONST") )
						op1 = Ins[i].Op[0];
					else
						op1 = TempVar.get(fir);
					if (Ins[i].Type[1].equals("PARAM") || Ins[i].Type[1].equals("LOCAL_VAR") || Ins[i].Type[1].equals("CONST") )
						op2 = Ins[i].Op[1];
					else
						op2 = TempVar.get(sec);
					if (Ins[i].Type[1].equals("CONST"))
						op2 = String.valueOf(sec);
					op = "(" + op1 + ")-(" + op2 + ")";
					TempVar.put(i, op);
					break;
				case "mul":
					fir = Integer.valueOf(Ins[i].Var[0]);
					sec = Integer.valueOf(Ins[i].Var[1]);
					if (Ins[i].Type[0].equals("PARAM") || Ins[i].Type[0].equals("LOCAL_VAR") || Ins[i].Type[0].equals("CONST") )
						op1 = Ins[i].Op[0];
					else
						op1 = TempVar.get(fir);
					if (Ins[i].Type[1].equals("PARAM") || Ins[i].Type[1].equals("LOCAL_VAR") || Ins[i].Type[1].equals("CONST") )
						op2 = Ins[i].Op[1];
					else
						op2 = TempVar.get(sec);
					if (Ins[i].Type[1].equals("CONST"))
						op2 = String.valueOf(sec);
					op = op1 + "*" + op2;
					TempVar.put(i, op);
					break;
				case "div":
					fir = Integer.valueOf(Ins[i].Var[0]);
					sec = Integer.valueOf(Ins[i].Var[1]);
					if (Ins[i].Type[0].equals("PARAM") || Ins[i].Type[0].equals("LOCAL_VAR") || Ins[i].Type[0].equals("CONST") )
						op1 = Ins[i].Op[0];
					else
						op1 = TempVar.get(fir);
					if (Ins[i].Type[1].equals("PARAM") || Ins[i].Type[1].equals("LOCAL_VAR") || Ins[i].Type[1].equals("CONST") )
						op2 = Ins[i].Op[1];
					else
						op2 = TempVar.get(sec);
					if (Ins[i].Type[1].equals("CONST"))
						op2 = String.valueOf(sec);
					op = "(" + op1 + ")/(" + op2 + ")";
					TempVar.put(i, op);
					break;
				case "mod":
					fir = Integer.valueOf(Ins[i].Var[0]);
					sec = Integer.valueOf(Ins[i].Var[1]);
					if (Ins[i].Type[0].equals("PARAM") || Ins[i].Type[0].equals("LOCAL_VAR") || Ins[i].Type[0].equals("CONST") )
						op1 = Ins[i].Op[0];
					else
						op1 = TempVar.get(fir);
					if (Ins[i].Type[1].equals("PARAM") || Ins[i].Type[1].equals("LOCAL_VAR") || Ins[i].Type[1].equals("CONST") )
						op2 = Ins[i].Op[1];
					else
						op2 = TempVar.get(sec); 
					if (Ins[i].Type[1].equals("CONST"))
						op2 = String.valueOf(sec);
					op = "(" + op1 + ")%(" + op2 + ")";
					TempVar.put(i, op);
					break;
				case "load":
					//instr 30: add a_base#32728 GP
				    //instr 31: add (30) x_offset#0
					//instr 32: load (31)
					//fir = 31
					//op = "a[0"
					fir = Integer.valueOf(Ins[i].Var[0]);
					op = TempVar.get(fir);
					int pos = op.indexOf("[");
					if (pos > 0) {
						int spos = op.lastIndexOf("*");
						if (spos > 0) {
							//System.out.println(op);
							String ss = op.substring(spos + 1);
							int secss = ss.indexOf("(");
							if (secss < 0) {
								String ts = op.substring(0, spos);
								int tnum = Integer.valueOf(ss) / 8;
								ts = ts + "*" + tnum;
								op = ts;
							}
						}
						op = op + "]";
					}
					//op = "a[0]"
					TempVar.put(i, op);
					break;
				case "store":
					fir = Integer.valueOf(Ins[i].Var[0]);
					sec = Integer.valueOf(Ins[i].Var[1]);
					op2 = TempVar.get(sec);
					op = "";
					
					if (Ins[i].Type[0].equals("CONST")) {
						//instr 8: add a_base#32728 GP
					    //instr 9: add (8) x_offset#0
					    //instr 10: store 1 (9)
						//op = "a[0"
						pos = op2.indexOf("[");
						if (pos > 0) {
							int spos = op2.lastIndexOf("*");
							if (spos > 0) {
								//System.out.println(op2);
								String ss = op2.substring(spos + 1);
								int secss = ss.indexOf("(");
								if (secss < 0) {
									String ts = op2.substring(0, spos);
									int tnum = Integer.valueOf(ss) / 8;
									ts = ts + "*" + tnum;
									op = ts + "]=" + fir + ";\r\n";
								} else 
									op = op2 + "]=" + fir + ";\r\n";
							} else 
								op = op2 + "]=" + fir + ";\r\n";
						}
						else
							op = op2 + "=" + fir + ";\r\n";
					}
					else if (Ins[i].Type[0].equals("REG")) {
						//instr 62: add k_base#32736 GP
					    //instr 63: load (62)
					    //instr 64: add max_base#32728 GP
					    //instr 65: store (63) (64)
						int spos = 0;
						int secss = 0;
						int tnum = 0;
						String ss;
						String ts;
						op1 = TempVar.get(fir);
						pos = op2.indexOf("[");
						if (pos > 0) {
							spos = op2.lastIndexOf("*");
							if (spos > 0) {
								//System.out.println(op2);
								ss = op2.substring(spos + 1);
								secss = ss.indexOf("(");
								if (secss < 0) {
									ts = op2.substring(0, spos);
									tnum = Integer.valueOf(ss) / 8;
									ts = ts + "*" + tnum;
									op = ts + "]=" + op1 + ";\r\n";
								} else 
									op = op2 + "]=" + op1 + ";\r\n";
							} else 
								op = op2 + "]=" + op1 + ";\r\n";
						} else
							op = op2 + "=" + op1 + ";\r\n";
					}
					else {
						op1 = Ins[i].Op[0];
						pos = op2.indexOf("[");
						if (pos > 0) {
							int spos = op2.lastIndexOf("*");
							if (spos > 0) {
								//System.out.println(op2);
								String ss = op2.substring(spos + 1);
								int secss = ss.indexOf("(");
								if (secss < 0) {
									String ts = op2.substring(0, spos);
									int tnum = Integer.valueOf(ss) / 8;
									ts = ts + "*" + tnum;
									op = ts + "]=" + op1 + ";\r\n";
								} else 
									op = op2 + "]=" + op1 + ";\r\n";
							} else 
								op = op2 + "]=" + op1 + ";\r\n";
						} else
							op = op2 + "=" + op1 + ";\r\n";						
					}
					TempVar.put(i, op);
					res = res + op;
					//System.out.println("store:" + res);
					Statement.add(res);
					break;
				case "move":
					fir = Integer.valueOf(Ins[i].Var[0]);
					sec = Integer.valueOf(Ins[i].Var[1]);

					if (Ins[i].Type[1].equals("LOCAL_VAR") || Ins[i].Type[1].equals("PARAM"))
						op2 = Ins[i].Op[1];
					else {
						sec = Integer.valueOf(Ins[i].Var[1]);
						op2 = TempVar.get(sec);	
						if (Ins[i].Type[1].equals("CONST"))
							op2 = String.valueOf(sec);
					}
					if (Ins[i].Type[0].equals("CONST")) {
						pos = op2.indexOf("[");
						if (pos > 0)
							op = op2 + "]=" + fir + ";\r\n";
						else 
							op = op2 + "=" + fir + ";\r\n";
						TempVar.put(i, op);
						res = res + op;
					}
					else if (Ins[i].Type[0].equals("REG")) {
						//instr 62: add k_base#32736 GP
					    //instr 63: load (62)
					    //instr 64: add max_base#32728 GP
					    //instr 65: store (63) (64)
						op1 = TempVar.get(fir);
						pos = op2.indexOf("[");
						if (pos > 0)
							op = op2 + "]=" + op1 + ";\r\n";
						else 
							op = op2 + "=" + op1 + ";\r\n";
						TempVar.put(i, op);
						res = res + op;
					}
					else if (Ins[i].Type[0].equals("LOCAL_VAR") || Ins[i].Type[0].equals("PARAM")){
						op1 = Ins[i].Op[0];
						pos = op2.indexOf("[");
						if (pos > 0)
							op = op2 + "]=" + op1 + ";\r\n";
						else 
							op = op2 + "=" + op1 + ";\r\n";
						TempVar.put(i, op);
						res = res + op;
					}
					Statement.add(res);
					break;					
				case "cmpeq":
					fir = Integer.valueOf(Ins[i].Var[0]);
					sec = Integer.valueOf(Ins[i].Var[1]);
					if (Ins[i].Type[0].equals("PARAM") || Ins[i].Type[0].equals("LOCAL_VAR") || Ins[i].Type[0].equals("CONST") )
						op1 = Ins[i].Op[0];
					else 
						op1 = TempVar.get(fir);
					if (Ins[i].Type[1].equals("PARAM") || Ins[i].Type[1].equals("LOCAL_VAR") || Ins[i].Type[1].equals("CONST") )
						op2 = Ins[i].Op[1];
					else 
						op2 = TempVar.get(sec);
					if (Ins[i].Type[0].equals("CONST"))
						op1 = String.valueOf(sec);	
					if (Ins[i].Type[1].equals("CONST"))
						op2 = String.valueOf(sec);					
					op = op1 + "==" + op2;
					TempVar.put(i, op);
					i++;
					if (Ins[i].opcode.equals("blbc")) 
						op = op1 + "==" + op2;
					else if (Ins[i].opcode.equals("blbs")) 
						op = op1 + "!=" + op2;
					sec = Integer.valueOf(Ins[i].Var[1]);
					if (Ins[sec - 1].opcode.equals("br")) {
						jumptime[sec - 1]++;
						fir = Integer.valueOf(Ins[sec - 1].Var[0]);
						if (fir < sec - 1 && fir < i) {
							op = "while(" + op + ") {\r\n";
							//LabelLine[sec - 1] = 1;
						}
						else
							op = "if (" + op + ") {\r\n";
					}
					else
						op = "if (" + op + ") {\r\n";
					TabNum++;
					TempVar.put(i, op);
					res = res + op;
					Statement.add(res);
					break;					
				case "cmplt":
					fir = Integer.valueOf(Ins[i].Var[0]);
					sec = Integer.valueOf(Ins[i].Var[1]);
					if (Ins[i].Type[0].equals("PARAM") || Ins[i].Type[0].equals("LOCAL_VAR") || Ins[i].Type[0].equals("CONST") )
						op1 = Ins[i].Op[0];
					else 
						op1 = TempVar.get(fir);
					if (Ins[i].Type[1].equals("PARAM") || Ins[i].Type[1].equals("LOCAL_VAR") || Ins[i].Type[1].equals("CONST") )
						op2 = Ins[i].Op[1];
					else 
						op2 = TempVar.get(sec);
					if (Ins[i].Type[0].equals("CONST"))
						op1 = String.valueOf(sec);	
					if (Ins[i].Type[1].equals("CONST"))
						op2 = String.valueOf(sec);	
					op = op1 + "<" + op2;
					TempVar.put(i, op);
					i++;
					if (Ins[i].opcode.equals("blbc")) 
						op = op1 + "<" + op2;
					else if (Ins[i].opcode.equals("blbs")) 
						op = op1 + ">=" + op2;
					sec = Integer.valueOf(Ins[i].Var[1]);
					if (Ins[sec - 1].opcode.equals("br")) {
						jumptime[sec - 1]++;
						fir = Integer.valueOf(Ins[sec - 1].Var[0]);
						if (fir < sec - 1 && fir < i) {
							//LabelLine[sec - 1] = 1;
							op = "while(" + op + ") {\r\n";
						}
						else
							op = "if (" + op + ") {\r\n";
					}
					else
						op = "if (" + op + ") {\r\n";
					TabNum++;
					TempVar.put(i, op);
					res = res + op;
					Statement.add(res);
					break;
				case "cmple":
					fir = Integer.valueOf(Ins[i].Var[0]);
					sec = Integer.valueOf(Ins[i].Var[1]);
					if (Ins[i].Type[0].equals("PARAM") || Ins[i].Type[0].equals("LOCAL_VAR") || Ins[i].Type[0].equals("CONST") )
						op1 = Ins[i].Op[0];
					else 
						op1 = TempVar.get(fir);
					if (Ins[i].Type[1].equals("PARAM") || Ins[i].Type[1].equals("LOCAL_VAR") || Ins[i].Type[1].equals("CONST") )
						op2 = Ins[i].Op[1];
					else 
						op2 = TempVar.get(sec);
					op = op1 + "<=" + op2;
					TempVar.put(i, op);
					i++;
					if (Ins[i].opcode.equals("blbc")) 
						op = op1 + "<=" + op2;
					else if (Ins[i].opcode.equals("blbs")) 
						op = op1 + ">" + op2;
					sec = Integer.valueOf(Ins[i].Var[1]);
					if (Ins[sec - 1].opcode.equals("br")) {
						jumptime[sec - 1]++;
						fir = Integer.valueOf(Ins[sec - 1].Var[0]);
						if (fir < sec - 1 && fir < i) {
							op = "while(" + op + ") {\r\n";
							//LabelLine[sec - 1] = 1;
						}
						else
							op = "if (" + op + ") {\r\n";
					}
					else
						op = "if (" + op + ") {\r\n";
					TabNum++;
					TempVar.put(i, op);
					res = res + op;
					Statement.add(res);
					break;
				case "br":
					fir = Integer.valueOf(Ins[i].Var[0]);
					
					if (fir < i) {
						op = "}\r\n";
						//System.out.println("jumptime: "  + jumptime[i]);
						if (jumptime[i] > 0)
							for (j = 0; j < jumptime[i]; j++) {
								TabNum--;
								res = "";
								for (int k = 0; k < TabNum; k++)
									res = res + "	";
								res = res + op;
								Statement.add(res);
							}
						else {
							TabNum--;
							res = "";
							for (j = 0; j < TabNum; j++)
								res = res + "	";
							res = res + op;
							Statement.add(res);
						}
					}
					else {
						TabNum--;
						res = "";
						for (j = 0; j < TabNum; j++)
							res = res + "	";
						op = "} else {\r\n";
						TabNum++;
						if (LabelLine[i] == 3)
							LabelLine[i] = 4;
						else
							LabelLine[i] = 3;
						TempVar.put(i,  op);
						res = res + op;
						Statement.add(res);
					}
					
					break;
				case "wrl":
					op = "WriteLine();\r\n";
					TempVar.put(i,  op);
					res = res + op;
					Statement.add(res);
					break;
				case "write":
					fir = Integer.valueOf(Ins[i].Var[0]);
					if (Ins[i].Type[0].equals("PARAM") || Ins[i].Type[0].equals("LOCAL_VAR") || Ins[i].Type[0].equals("CONST") )
						op1 = Ins[i].Op[0];
					else
						op1 = TempVar.get(fir);
					op = "WriteLong(" + op1 + ");\r\n";
					TempVar.put(i,  op);
					res = res + op;
					Statement.add(res);
					break;
				case "param":
					fir = Integer.valueOf(Ins[i].Var[0]);
					if (Ins[i].Type[0].equals("PARAM") || Ins[i].Type[0].equals("LOCAL_VAR") || Ins[i].Type[0].equals("CONST") )
						op1 = Ins[i].Op[0];
					else
						op1 = TempVar.get(fir);
					TempVar.put(i, op1);
					VParam.add(op1);
					break;
				case "call":
					fir = Integer.valueOf(Ins[i].Var[0]);
					String functionname = Function.get(fir);
					op = functionname + "(";
					for (j = 0; j < VParam.size() - 1; j++)
						op = op + VParam.get(j) + ",";
					op = op + VParam.get(j) + ");\r\n";
					TempVar.put(i, op);
					VParam.clear();
					res = res + op;
					Statement.add(res);
					break;
				case "ret":
					res = "";
					for (j = 0; j < --TabNum; j++)
						res = res + "	";
					res = res + "}\r\n";
					Statement.add(res);
					break;
			}
//			if (LabelLine[i] == 2) {
//				//System.out.println("check " + i);
//				TabNum--;
//				res = "";
//				for (j = 0; j < TabNum; j++)
//					res = res + "	";
//				res = res + "}\r\n";
//				Statement.add(res);
//			}
			//System.out.println(res);
			//Statement.add(res);
			i++;
			
		}
		
		//for (; i <= End; i++)  {
		//	translateStatement(i, nVar, nParam);
		//}
		Entry = End + 1;
		return true;
	}
}