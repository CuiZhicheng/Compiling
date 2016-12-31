import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import basic.Instruction;

public class Trans_3ddr_To_Easy{
	
	public Instruction[] Ins = new Instruction[100000];
    public String[] InitInstr = new String[100000];
	
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
	
	public void go(String fp){
		
		String filepath = fp;
		try {
			File dir = new File(filepath);
			File[] files = dir.listFiles();
			for (File f : files) {			
				String name = f.getName();
				int pos = name.indexOf("easy");
				if (pos >= 0) continue;
				String[] temp = name.split("\\.");
				int temp1;
				String InstrId = "";
				String[] Instr;
				String res = "";
				String outputpath = fp + temp[0] + "_easy.3ddr";
				System.out.println(outputpath);
				BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
				FileOutputStream fos = new FileOutputStream(outputpath); 
		        OutputStreamWriter bw = new OutputStreamWriter(fos, "UTF-8");
		        String s;
		        int i = 0;
		        int num = 1;
		        while ((s = br.readLine()) != null) {
		        	InitInstr[num] = s;
		        	num++;
		        }
		        br.close();
		        for (i = 0; i < Ins.length; i++)
		        	Ins[i] = new Instruction();
		        for (i = 1; i < num; i++) {
		        	Ins[i].index = i;
		        	temp = InitInstr[i].split(":");
		        	temp1 = temp[0].lastIndexOf(" ");
		        	InstrId = temp[0].substring(temp1 + 1);
		        	res = String.valueOf(Ins[i].index);
		        	temp[1] = temp[1].substring(1);
		        	Instr = temp[1].split(" ");
		        	Ins[i].opcode = Instr[0];
		        	Ins[i].opnum = Instr.length - 1;
		        	if (Ins[i].opnum == 0) {
		        		res = res + " " + Ins[i].opcode + "\r\n";
		        		bw.write(res);
		        		continue;
		        	}	
		        	else if (Ins[i].opnum == 1){
		        		if (Ins[i].opcode.equals("br")) {
		        			String target = Instr[1].substring(1, Instr[1].length() - 1);
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
	        			continue;
		        	}
		        	else {
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
		        	}
		        	
		        }
		        bw.close();
		        fos.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}