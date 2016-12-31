import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import basic.Instruction;

public class Trans{
	
	public Instruction[] Ins = new Instruction[100000];
	public String[] InitInstr = new String[100000];

	public void analyze(String src, int i, int x){
		if (src.equals("")) {
			Ins[x].kind[i] = 0;
			return;
		}
		int pos = src.indexOf("base");
		if (pos >= 0) {
			// i = 0
			Ins[x].kind[i] = 1;
			Ins[x].Var[i] = src.substring(0, pos - 1);
			return;
		}
		if (src.equals("GP") || src.equals("FP")) {
			Ins[x].kind[i] = -1;
			return;			
		}
		pos = src.indexOf("offset");
		if (pos >= 0) {
			// i = 1
			Ins[x].kind[i] = 2;	
			Ins[x].Var[i] = src.substring(0, pos - 1);
			return;
		}
		pos = src.indexOf("#");
		if (pos >= 0) {
			Ins[x].kind[i] = 3;
			Ins[x].Var[i] = src.substring(0, pos);
			return;
		}
		pos = src.indexOf('(');
		if (pos >= 0) {
			Ins[x].kind[i] = 4;
			Ins[x].Var[i] = "r" + src.substring(pos + 1, src.length() - 1);
			return;
		}
		pos = src.indexOf('[');
		if (pos >= 0) {
			Ins[x].kind[i] = 5;
			Ins[x].Var[i] = src.substring(pos + 1, src.length() - 1);
			return;
		}
		Ins[x].kind[i] = 6;
		Ins[x].Var[i] = src;
		return;
	}

	public void go(){
		String filepath = "G:/研一/高级编译技术/lab2/3ddr/";
		try {
			File dir = new File(filepath);
			File[] files = dir.listFiles();
			for (File f : files) {
				String name = f.getName();
				String[] temp = name.split("\\.");
				String[] Instr;
				String outputpath = "G:/研一/高级编译技术/lab2/3ddr_simple/" + temp[0] + "_simple.3ddr";
				//System.out.println(outputpath);
				BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
				FileOutputStream fos = new FileOutputStream(outputpath); 
		        OutputStreamWriter bw = new OutputStreamWriter(fos, "UTF-8");
		        String s;
		        int i = 0;
		        int j = 0;
		        int num = 1;
		        for (i = 0; i < Ins.length; i++)
		        	Ins[i] = new Instruction();
		        //save 3-address-code line by line to InitInstr
		        while ((s = br.readLine()) != null) {
		        	InitInstr[num] = s;
		        	num++;
		        }
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
		        	temp[1] = temp[1].substring(1);
		        	Instr = temp[1].split(" ");
		        	Ins[i].opcode = Instr[0];
		        	Ins[i].opnum = Instr.length - 1;
		        	for (j = 0; j < Ins[i].opnum; j++) {
		        		Ins[i].Op[j] = Instr[j + 1];
		        		analyze(Ins[i].Op[j], j, i);
		        		if (Ins[i].kind[j] == 1 || Ins[i].kind[j] == 2)
		        			Ins[i].def = 1;		        		
		        	}
		        }

		        // i = index of Ins
		        // j = index of output
		        i = 1;
		        j = 1;
		        while (i <= num) {
		        	// res  = output string
		        	String res = "";
		        	if (Ins[i].def == 0) {
		        		
		        		switch (Ins[i].opcode) {
		        			case "nop":
		        			case "entrypc":
		        			case "wrl":
		        			case "write":
		        			case "ret":
		        			case "enter":
		        				res = j + ":" + Ins[i].opcode + " " + Ins[i].Var[0] + "\r\n";
		        				break;
		        			case "add":
		        				res = j + ":" + "r" + j + " = " + Ins[i].Var[0] + " + " + Ins[i].Var[1] + "\r\n";
		        				break;
		        			case "sub":
		        				res = j + ":" + "r" + j + " = " + Ins[i].Var[0] + " - " + Ins[i].Var[1] + "\r\n";
		        				break;
		        			case "mul":
		        				res = j + ":" + "r" + j + " = " + Ins[i].Var[0] + " * " + Ins[i].Var[1] + "\r\n";
		        				break;
		        			case "div":
		        				res = j + ":" + "r" + j + " = " + Ins[i].Var[0] + " / " + Ins[i].Var[1] + "\r\n";
		        				break;
		        			case "mod":
		        				res = j + ":" + "r" + j + " = " + Ins[i].Var[0] + " % " + Ins[i].Var[1] + "\r\n";
		        				break;
		        			case "cmple":
		        				res = j + ":" + "r" + j + " = " + Ins[i].Var[0] + " <= " + Ins[i].Var[1] + "\r\n";
		        				break;
		        			case "cmplt":
		        				res = j + ":" + "r" + j + " = " + Ins[i].Var[0] + " < " + Ins[i].Var[1] + "\r\n";
		        				break;
		        			case "cmpeq":
		        				res = j + ":" + "r" + j + " = " + Ins[i].Var[0] + " == " + Ins[i].Var[1] + "\r\n";
		        				break;
		        			case "blbc":
		        				res = j + ":" +  "if( " +  Ins[i].Var[0] + " == 0 )" + " goto " + Ins[i].Var[1] + "\r\n";
		        				break;
		        			case "blbs":
		        				res = j + ":" +  "if( " +  Ins[i].Var[0] + " == 1 )" + " goto " + Ins[i].Var[1] + "\r\n";
		        				break;
		        			case "move":
		        				res = j + ":" + Ins[i].Var[1] + " = " + Ins[i].Var[0] + "\r\n";
		        				break;       			
		        			case "br":
		        			case "call":
		        				res = j + ":" + "goto " + Ins[i].Var[0];
		        				break;
		        		}
		        		bw.write(res);
		        		j++;
		        		i++;
		        	}
		        	else {
		        		String Variable = "";
		        		if (Ins[i].kind[0] == 1) {
		        			res = j + ":";
		        			Variable = Ins[i].Var[0];
		        			i++;
		        			while (true) {
		        				if (Ins[i].opcode.equals("load")) {
		        					res = j + ":" + Variable + "\r\n";
		        					bw.write(res);
		        					j++;i++;
		        					break;
		        				}
		        				else if (Ins[i].opcode.equals("store")) {
		        					res = j + ":" + Variable + " = " + Ins[i].Var[0] + "\r\n";
		        					bw.write(res);
		        					j++;i++;
		        					break;
		        				}
		        				else {
		        					if (Ins[i].kind[1] == 2) 
		        						Variable = Variable + "." + Ins[i].Var[1];
		        					else 
		        						Variable = Variable + " + " + Ins[i].Var[1];
		        				}
		        				i++;
		        			} 
		        		}		        		
		        	}
		        }
			    bw.close();
			    fos.close();
			    br.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void main(String argv[]){
		Trans t = new Trans();
		t.go();
	}
}