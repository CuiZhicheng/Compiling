import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import basic.BasicBlock;
import basic.Function;
import basic.Instruction;

public class LabFile{
	
	public String FilePath;
	public String FileName;
	public String FileOutputPath;
	
	public String[] InitInstr = new String[100000];
	public int InstructionNum;
	
	public Instruction[] Ins;
	public Vector<String> Statement;
	public int Entry;
	public boolean IsMain;
	
	public String[] EasyInstruction;
	public Map<Integer, BasicBlock> FileBB;
	public Map<Integer, Function> FileFunction;
	public BasicBlock BB;
	public Function Ft;
	//IsGoto[i] = j means ith goto is on jth line
	public int[] IsGoto;
	//IsBlock[i] = true means ith line is a block start point
	public boolean[] IsBlock;
	public int BlockNum;
	public int RetLine;
	public int FunctionNum;
	public int GotoNum;
	
	
	public void InputInstruction(){
		try {
			File f = new File(FilePath);
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
	        String s;
	        int num = 1;
	        
	        while ((s = br.readLine()) != null) {
	        	InitInstr[num] = s;
	        	num++;
	        }
	        InstructionNum = num;
	        br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public void trans_3ddr_to_c() {
		Trans translator = new Trans(this);
		translator.translate();
	}
	
	
	public void trans(String src, int i, int x){
		int pos = src.indexOf("base");
		if (pos >= 0) {
			Ins[x].kind[i] = 1;
			Ins[x].Op[0] = src.substring(0, pos - 1);
			return;
		}
		if (src.equals("GP") || src.equals("FP")) {
			Ins[x].kind[i] = -1;
			return;			
		}
		pos = src.indexOf("offset");
		if (pos >= 0) {
			Ins[x].kind[i] = 2;
			Ins[x].Op[1] = src.substring(0, pos - 1);	
			return;
		}
		pos = src.indexOf("#");
		if (pos >= 0) {
			Ins[x].kind[i] = 3;
			Ins[x].Op[i] = src.substring(0, pos);
			return;
		}
		pos = src.indexOf('(');
		if (pos >= 0) {
			Ins[x].kind[i] = 4;
			Ins[x].Op[i] = "r" + src.substring(pos + 1, src.length() - 1);
			return;
		}
		pos = src.indexOf('[');
		if (pos >= 0) {
			Ins[x].kind[i] = 5;
			Ins[x].Op[i] = src.substring(pos + 1, src.length() - 1);
			return;
		}
		Ins[x].kind[i] = 6;
		Ins[x].Op[i] = src;
		return;
	}
	
	public void trans_3ddr_to_easy(){		
		String res;
		int temp1;
		String[] Instr;
		String InstrId;
		String[] temp = FileName.split("\\.");
		String outputpath = FileOutputPath + temp[0] + "_easy.3ddr";
		try {
			FileOutputStream fos = new FileOutputStream(outputpath); 
	        OutputStreamWriter bw = new OutputStreamWriter(fos, "UTF-8");
	        int i = 0;
	        int num = 1;
	        Ins = new Instruction[num];
	        EasyInstruction = new String[num];
	        for (i = 0; i < Ins.length; i++)
	        	Ins[i] = new Instruction();
	        
	        /* analyze 3-address-code, divide it into different domain
	         * index (instr x)
			 * opcode (add / sub / mul ...)
			 * opnum (operator number)
			 * Op[] (operators)
			 * kind[] (register or address or num)
			 * def (whether a start, 1 if exist "base" or "offset", otherwise 0)
	         */
	        for (i = 1; i < num; i++) {
	        	Ins[i].index = i;
	        	temp = InitInstr[i].split(":");
	        	temp1 = temp[0].lastIndexOf(" ");
	        	//get Instruction index
	        	InstrId = temp[0].substring(temp1 + 1);
	        	res = String.valueOf(Ins[i].index);
	        	temp[1] = temp[1].substring(1);
	        	Instr = temp[1].split(" ");
	        	//get opcode and operator number
	        	Ins[i].opcode = Instr[0];
	        	Ins[i].opnum = Instr.length - 1;
	        	//nop wrl...
	        	if (Ins[i].opnum == 0) {
	        		res = res + " " + Ins[i].opcode + "\r\n";
	        		bw.write(res);
	        		EasyInstruction[i] = res.substring(0, res.length() - 2);
	        		continue;
	        	}
	        	//br write call store load...
	        	else if (Ins[i].opnum == 1){
	        		trans(Instr[1], 0, i);
	        		if (Ins[i].opcode.equals("br")) {
	        			String target = Instr[1].substring(1, Instr[1].length() - 1);
	        			Ins[i].opcode = "goto";
	        			res = res + " goto " +  target + "\r\n";
	        		} else if ( Ins[i].opcode.equals("call")) {
	        			String target = Instr[1].substring(1, Instr[1].length() - 1);
	        			res = res + " call " +  target + "\r\n";
	        		} else if (Instr[0].equals("load")) {
	        			String target = Instr[1].substring(1, Instr[1].length() - 1);
	        			Ins[i].Op[0] = target;
	        			res = res + " r" + Ins[i].index + " = *(r" + Ins[i].Op[0] + ")\r\n";
	        		} else {
	        			res = res + " " + temp[1] + "\r\n"; 
	        		}
	        		bw.write(res);
	        		EasyInstruction[i] = res.substring(0, res.length() - 2);
        			continue;
	        	}
	        	//others
	        	else {
	        		//simplify operator and identify instruction kind
	        		trans(Instr[1], 0, i);
	        		trans(Instr[2], 1, i);	        		
	        		switch (Instr[0]) {
	        			case "add":
	        				if (Ins[i].kind[1] == -1) 
	        					res = res + " r" + InstrId + " = " + Ins[i].Op[0] + "\r\n";
	        				else 
	        					res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " + " + Ins[i].Op[1] + "\r\n";
	        				break;
	        			case "sub":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " - " + Ins[i].Op[1] + "\r\n";
	        				break;
	        			case "mul":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " * " + Ins[i].Op[1] + "\r\n";
	        				break;
	        			case "div":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " / " + Ins[i].Op[1] + "\r\n";
	        				break;
	        			case "mod":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " % " + Ins[i].Op[1] + "\r\n";
	        				break;
	        			case "cmple":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " <= " + Ins[i].Op[1] + "\r\n";
	        				break;
	        			case "cmplt":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " < " + Ins[i].Op[1] + "\r\n";
	        				break;
	        			case "cmpeq":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " == " + Ins[i].Op[1] + "\r\n";
	        				break;
	        			case "blbc":
	        				res = res + " if( " +  Ins[i].Op[0] + " == 0 )" + " goto " + Ins[i].Op[1] + "\r\n";
	        				break;
	        			case "blbs":
	        				res = res + " if( " +  Ins[i].Op[0] + " == 1 )" + " goto " + Ins[i].Op[1] + "\r\n";
	        				break;
	        			case "store":
	        				if (Ins[i].kind[0] == 6)
	        					res = res + " *(" + Ins[i].Op[1] + ") = " + Ins[i].Op[0] + "\r\n";
	        				else 
	        					res = res + " *(" + Ins[i].Op[1] + ") = " + Ins[i].Op[0] + "\r\n";
	        				break;
	        			case "move":
	        				res = res + " "+ Ins[i].Op[1] + " = " + Ins[i].Op[0] + "\r\n";
	        				break;		        			
	        		}
	        		bw.write(res);
	        		EasyInstruction[i] = res.substring(0, res.length() - 2);
	        	}
	        	
	        }
	        bw.close();
	        fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public int FindBlockIndexByStartPoint(int startpoint) {
		int i;
		int res = 0;
		for (i = 1; i <= BlockNum; i++)
			if (FileBB.get(i).BlockStartPoint == startpoint) {
				res = i;
				break;
			}
		return res;
	}

	public void getCFG(){
		int i, j, k, l;
		String[] temp;
		//System.out.println(FileName);
		int instructionnum = EasyInstruction.length;
		IsBlock = new boolean[instructionnum];
		FileFunction = new HashMap<Integer, Function>();
		IsGoto = new int[instructionnum];
		FileBB = new HashMap<Integer, BasicBlock>();
		Arrays.fill(IsBlock, false);
		BlockNum = 0;
	    GotoNum = 0;
	    RetLine = 0;
	    FunctionNum = 0;
	    i = 1;
	    while (i < instructionnum) {
	    	if (Ins[i].opcode.equals("enter")) { 
	    		FunctionNum++;
	    		GotoNum = 0;
	    		//Ft for this Function
	    		Ft = new Function(i);
	    		//FileFunction.put(FunctionNum, Ft);
	    		IsBlock[i] = true;
	    		Arrays.fill(IsGoto, 0);
	    		j = i + 1;
	    		while (true) {
        			if (Ins[j].opcode.equals("ret")) {
        				RetLine = j;
        				break;
        			}
        			k = EasyInstruction[j].indexOf("goto");
        			l = EasyInstruction[j].indexOf("call");
        			if (k >= 0 || l >= 0) {
        				GotoNum++;
        				IsGoto[GotoNum] = j;
        				temp = EasyInstruction[j].split(" ");
        				int t = Integer.valueOf(temp[temp.length - 1]);
        				IsBlock[t] = true;
        				IsBlock[j + 1] = true;
        			}
        			j++;
        		}
	    		j = i;
        		while (j <= RetLine) {
        			if (IsBlock[j] == true) {
        				BlockNum++;
        				BB = new BasicBlock();
        				BB.BlockStartPoint = j;
        				FileBB.put(BlockNum, BB);
        				Ft.BlockNumInFunction++;
        	    		Ft.FunctionBB.put(Ft.BlockNumInFunction, BB);
        			}
        			j++;
        		}
        		j = 1;
        		k = 1;
        		while (j < Ft.BlockNumInFunction) {
        			int start = Ft.FunctionBB.get(j + 1).BlockStartPoint;
        			if (k <= GotoNum && start > IsGoto[k]) {
        				if (Ins[IsGoto[k]].opcode.equals("call")) {
        					Ft.FunctionBB.get(j).SeccessorNum = 1;
        					Ft.FunctionBB.get(j).Successor[0] = FindBlockIndexByStartPoint(IsGoto[k] + 1);
        				} else if (Ins[IsGoto[k]].opcode.equals("goto")) {
        					Ft.FunctionBB.get(j).SeccessorNum = 1;
        					Ft.FunctionBB.get(j).Successor[0] = FindBlockIndexByStartPoint(Integer.valueOf(Ins[IsGoto[k]].Op[0]));
        				} else {
        					temp = EasyInstruction[IsGoto[k]].split(" ");
        					int a = Integer.valueOf(temp[temp.length - 1]);
        					int b = IsGoto[k] + 1;
        					int c = a < b ? a : b;
        					int d = a < b ? b : a;
        					Ft.FunctionBB.get(j).SeccessorNum = 2;
        					Ft.FunctionBB.get(j).Successor[0] = FindBlockIndexByStartPoint(c);
        					Ft.FunctionBB.get(j).Successor[1] = FindBlockIndexByStartPoint(d);
        				}
        				k++;
        			} else {
        				Ft.FunctionBB.get(j).SeccessorNum = 1;
        				Ft.FunctionBB.get(j).Successor[0] = FindBlockIndexByStartPoint(start);
        			}
        			j++;
        		}
        		Ft.FunctionBB.get(j).SeccessorNum = 0;
        		FileFunction.put(FunctionNum, Ft);
        		i = RetLine;
        	}
	    	i++;
	    }
	    
	    
	}

	public void publishCFG(){
		int i, j, k;
		
		String[] temp = FileName.split("\\.");
		String outputpath = FileOutputPath + temp[0] + ".cfg";
		try {
			FileOutputStream fos = new FileOutputStream(outputpath); 
	        OutputStreamWriter bw = new OutputStreamWriter(fos, "UTF-8");
	        String res;
			i = 1;
			while (i <= FunctionNum) {
				res = "Function: " + FileFunction.get(i).FunctionStartPoint + "\r\n";
				//System.out.println(res);
				bw.write(res);
				res = "Basic blocks:";
				for (j = 1; j <= FileFunction.get(i).BlockNumInFunction; j++)
					res = res + " " + FileFunction.get(i).FunctionBB.get(j).BlockStartPoint;
				res = res + "\r\n";
				//System.out.println(res);
				bw.write(res);
				res = "CFG:\r\n";
				//System.out.println(res);
				bw.write(res);
				for (j = 1; j <= FileFunction.get(i).BlockNumInFunction; j++) {
					res = FileFunction.get(i).FunctionBB.get(j).BlockStartPoint + " ->";
					for (k = 1; k <= FileFunction.get(i).FunctionBB.get(j).SeccessorNum; k++) {
						res = res + " " + FileBB.get(FileFunction.get(i).FunctionBB.get(j).Successor[k - 1]).BlockStartPoint;
					}
					res = res + "\r\n";
					//System.out.println(res);
					bw.write(res);
				}
				i++;
			}
			bw.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}