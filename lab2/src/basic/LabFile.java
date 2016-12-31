package basic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;
/*
/Users/liangjingyue/Desktop/鐮旂┒鐢熻绋�/楂樼骇缂栬瘧/3ddr/3ddr
/Users/liangjingyue/Desktop/鐮旂┒鐢熻绋�/楂樼骇缂栬瘧/compile/result
/Users/liangjingyue/Desktop/鐮旂┒鐢熻绋�/楂樼骇缂栬瘧/3ddr/test/
/Users/liangjingyue/Desktop/鐮旂┒鐢熻绋�/楂樼骇缂栬瘧/compile/testResult/
/Users/liangjingyue/Desktop/鐮旂┒鐢熻绋�/楂樼骇缂栬瘧/3ddr/test/
/Users/liangjingyue/Desktop/鐮旂┒鐢熻绋�/楂樼骇缂栬瘧/3ddr/result2/
/Users/liangjingyue/Desktop/鐮旂┒鐢熻绋�/楂樼骇缂栬瘧/3ddr/sort
/Users/liangjingyue/Desktop/鐮旂┒鐢熻绋�/楂樼骇缂栬瘧/compile/result
 */
public class LabFile{
	
	public String FilePath;
	public String FileName;
	public String FileOutputPath;
	
	public Instruction[] Ins;
	public String[] EasyInstruction;
	public Map<Integer, BasicBlock> FileBB;//Map閲岄潰璁拌浇鐨勬槸浠�1寮�濮嬫妧鏈殑key
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

	private boolean[] visited;
	private Stack<Integer> stack;
	private Vector<Set<Integer>> scr;
	private Set<Integer> scrline = new HashSet<Integer>();
	int current=0;

	public VariableTable VT=new VariableTable();
	
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
			// changed
			Ins[x].Op[1] = src.substring(0, pos + 6);
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
	    String[] InitInstr = new String[100000];
		String[] temp = FileName.split("\\.");
		String outputpath = FileOutputPath + temp[0] + "_easy.3ddr";
		try {
			File f = new File(FilePath);
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
	        Ins = new Instruction[num];
	        EasyInstruction = new String[num];
	        br.close();
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
				//System.out.println("warning"+InitInstr[i]);
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
						//杩欓噷鎴戝笇鏈涙妸*(r16)鍙樻垚*b16
	        			String target = Instr[1].substring(1, Instr[1].length() - 1);
	        			Ins[i].Op[0] = target;
	        			res = res + " r" + Ins[i].index + " = *(r" + Ins[i].Op[0] + ")\r\n";
						addtoUse(Ins[i].index,Ins[i],"r"+Ins[i].Op[0],null,true);
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
	        				if (Ins[i].kind[1] == -1) {
								res = res + " r" + InstrId + " = " + Ins[i].Op[0] + "\r\n"; //a=b鐨勫舰寮�
								//addtoUse(Integer.parseInt(InstrId), Ins[i], "1", Ins[i].Op[0]);
								if(Ins[i].Op[0].indexOf(0)<'0' || Ins[i].Op[0].indexOf(0)>'9') { //涓嶈兘鏄暟瀛�
									VT.add2Name(Ins[i].Op[0],Integer.parseInt(InstrId));
									/*if (!VT.name.containsKey(Ins[i].Op[0])) { //鎶婂彉閲忓悕瀛楀姞鍏ョ鍙疯〃
										Set<Integer> R = new HashSet<Integer>(); //濡傛灉Integer涓嶈灏辨敼鎴怱tring!
										R.add(Integer.parseInt(InstrId));
										VT.name.put(Ins[i].Op[0], R);
									} else {
										if (!VT.name.get(Ins[i].Op[0]).contains(InstrId)) {
											VT.name.get(Ins[i].Op[0]).add(Integer.parseInt(InstrId));
										}
									}*/
								}
							}
	        				else {
								res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " + " + Ins[i].Op[1] + "\r\n";
								addtoUse(Integer.parseInt(InstrId),Ins[i],Ins[i].Op[0],Ins[i].Op[1],false);
							}
	        				break;
	        			case "sub":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " - " + Ins[i].Op[1] + "\r\n";
							addtoUse(Integer.parseInt(InstrId),Ins[i],Ins[i].Op[0],Ins[i].Op[1],false);
	        				break;
	        			case "mul":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " * " + Ins[i].Op[1] + "\r\n";
							addtoUse(Integer.parseInt(InstrId),Ins[i],Ins[i].Op[0],Ins[i].Op[1],false);
	        				break;
	        			case "div":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " / " + Ins[i].Op[1] + "\r\n";
							addtoUse(Integer.parseInt(InstrId),Ins[i],Ins[i].Op[0],Ins[i].Op[1],false);
	        				break;
	        			case "mod":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " % " + Ins[i].Op[1] + "\r\n";
							addtoUse(Integer.parseInt(InstrId),Ins[i],Ins[i].Op[0],Ins[i].Op[1],false);
	        				break;
	        			case "cmple":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " <= " + Ins[i].Op[1] + "\r\n";
							addtoUse(Integer.parseInt(InstrId),Ins[i],Ins[i].Op[0],Ins[i].Op[1],false);
	        				break;
	        			case "cmplt":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " < " + Ins[i].Op[1] + "\r\n";
							addtoUse(Integer.parseInt(InstrId),Ins[i],Ins[i].Op[0],Ins[i].Op[1],false);
	        				break;
	        			case "cmpeq":
	        				res = res + " r" + InstrId + " = " + Ins[i].Op[0] + " == " + Ins[i].Op[1] + "\r\n";
							addtoUse(Integer.parseInt(InstrId),Ins[i],Ins[i].Op[0],Ins[i].Op[1],false);
	        				break;
	        			case "blbc":
	        				res = res + " if( " +  Ins[i].Op[0] + " == 0 )" + " goto " + Ins[i].Op[1] + "\r\n";
							addtoUse(Integer.parseInt(InstrId),Ins[i],"0",Ins[i].Op[0],false);
	        				break;
	        			case "blbs":
	        				res = res + " if( " +  Ins[i].Op[0] + " == 1 )" + " goto " + Ins[i].Op[1] + "\r\n";
							addtoUse(Integer.parseInt(InstrId),Ins[i],"0",Ins[i].Op[0],false);
	        				break;
	        			case "store":
	        				if (Ins[i].kind[0] == 6) {
								res = res + " *(" + Ins[i].Op[1] + ") = " + Ins[i].Op[0] + "\r\n";
								addtoDef(Ins[i].index,Ins[i],Ins[i].Op[1],Ins[i].Op[0]);
							}
	        				else {
								res = res + " *(" + Ins[i].Op[1] + ") = " + Ins[i].Op[0] + "\r\n";
								addtoDef(Ins[i].index,Ins[i],Ins[i].Op[1],Ins[i].Op[0]);
							}
	        				break;
	        			case "move":
	        				res = res + " "+ Ins[i].Op[1] + " = " + Ins[i].Op[0] + "\r\n";
							addtoDef(Integer.parseInt(InstrId),Ins[i],Ins[i].Op[1],Ins[i].Op[0]);
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
		initAllInstrINOUT();
	}

	private void initAllInstrINOUT(){
		for(Instruction i:Ins){
			i.RD_IN = new int[Ins.length+1];
			i.RD_OUT = new int[Ins.length+1];
			i.live_IN = new HashSet<String>();
			i.live_OUT = new HashSet<String>();
		}
	}
	private void addtoUse(int instrnum,Instruction instr,String op1,String op2,boolean star) {
		//涓�鑸槸涓暟瀛楁垨鑰呮槸缁撴瀯浣撻噷闈㈢殑鍋忕Щ.
		int i=0;
		if(op1.indexOf("r")==0  && op1.length()>1&& (op1.charAt(1)>='0'&& op1.charAt(1)<='9')) {
			System.out.println(FileName+" u11: "+instrnum+" "+op1+" "+op2);
			int lastinstr1 = Integer.parseInt(op1.substring(1));
			//杩欎簺閮芥槸琚玼se鐨勮鍙�,鎵�浠ユ渶鍚庝竴涓彉閲忔槸false
			instr.use[i] = VT.findAndAddto(instrnum, lastinstr1, 1,true,star);
			instr.user[i] = op1;
			i++;
		}
		else {
			if((op1.charAt(0)>'9'||op1.charAt(0)<'0') && op1.charAt(0)!='-') {
				System.out.println(FileName + " u12: " + instrnum + " " + op1 + " " + op2);
				VT.addtoUse(instrnum, op1);
				instr.use[i] = op1;
				instr.user[i] = op1;
				i++;
			}
		}
		if (op2 != null) {
			if (op2.indexOf("r") == 0 && op2.length()>1 && (op2.charAt(1)>='0'&& op2.charAt(1)<='9')) { //op2鏄瘎瀛樺櫒鐨勫��
				System.out.println(FileName+" u21: "+instrnum+" "+op1+" "+op2);
				int lastinstr2 = Integer.parseInt(op2.substring(1));
				//绗簩涓氨鏄槑纭鐢ㄤ簡,
				instr.use[i] = VT.findAndAddto(instrnum, lastinstr2, 1,false,false);
				instr.user[i] = op2;
				i++;
			}
			else{
				if((op2.charAt(0)>'9' || op2.charAt(0)<'0') && op2.charAt(0)!='-' && op2.indexOf("offset")==-1) {
					System.out.println(FileName+" u22: "+instrnum+" "+op1+" "+op2);
					VT.addtoUse(instrnum, op2);
					instr.use[i] = op2;
					instr.user[i] = op2;
					i++;
				}
			}
		}

	}
	private void addtoDef(int instrnum,Instruction instr,String op1,String op2){
		//涓句緥 *(r6) = r7
		Integer lastinstr1 = null;
		int i=0;
		if(op1.indexOf("r")==0  && op1.length()>1&& (op1.charAt(1)>='0'&& op1.charAt(1)<='9')) {
			System.out.println(FileName+" d11: "+instrnum+" "+op1+" "+op2);
			lastinstr1 = Integer.parseInt(op1.substring(1)); //get 6
			if(instrnum==12)
				System.out.println("array a"+instrnum+" "+lastinstr1);
			instr.gen = VT.findAndAddto(instrnum, lastinstr1, 0,true,true); //鏄瀹氬�肩殑鐨�,鎵�浠ュ氨鏄痶rue
			instr.genr = "r"+instrnum;
			String last2 = VT.findAndAddto(instrnum, lastinstr1-1, 2,false,false);
			if(last2==null || !last2.equals(instr.gen)){
				lastinstr1 = null;
			}
		} else {
			if ((op1.charAt(0) > '9' || op1.charAt(0) < '0') && op1.charAt(0) != '-') {
				System.out.println(FileName + " d12: " + instrnum + " " + op1 + " " + op2);
				VT.addtoDef(instrnum, op1);
				instr.gen = op1;
			}
		}
		if (op2.indexOf("r") ==0 && op2.length()>1&& (op2.charAt(1)>='0'&& op2.charAt(1)<='9')) { //op2鏄瘎瀛樺櫒鐨勫��
			System.out.println(FileName+" "+instrnum+" d21: "+op1+" "+op2);
			int lastinstr2 = Integer.parseInt(op2.substring(1));//get 7
			instr.use[i] = VT.findAndAddto(instrnum, lastinstr2, 1,false,false);
			instr.user[i] = op2;
			i++;
		}
		else{
			if((op2.charAt(0)>'9'||op2.charAt(0)<'0')&&op2.charAt(0)!='-') {
				System.out.println(FileName+" "+instrnum+" d22: "+op1+" "+op2);
				VT.addtoUse(instrnum, op2);
				instr.use[i] = op2;
				instr.user[i] = op2;
				i++;
			}
			else {
				if(lastinstr1==null&&op1!=null)
					VT.add2ConstSet(instr.gen,instrnum,Integer.parseInt(op2));
			}
		}
		instr.use[i]=null;


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
	    		FunctionNum++; //鏄粠1寮�濮嬫帓鍒楃殑
	    		GotoNum = 0;
	    		//Ft for this Function
	    		Ft = new Function(i);//i鐨勬槸琛屽彿鍛�!
				Ft.setInstr(Ins,VT);
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
						if (BlockNum > 1) {//Todo: 鏄笉鏄笉鐢ㄨ繖涔堥夯鐑�
							BasicBlock tempBB = FileBB.get(BlockNum - 1);
							tempBB.BlockEndPoint = j - 1;
							FileBB.put(BlockNum - 1, tempBB);
						}
						BB = new BasicBlock();
						BB.createArray(Ins.length+1, VT.name.size());
						BB.setInstr(Ins, VT);
						BB.BlockStartPoint = j;
						BB.BlockEndPoint = j;
						FileBB.put(BlockNum, BB);
						Ft.BlockNumInFunction++;
						Ft.FunctionBB.put(Ft.BlockNumInFunction, BB);
					}
					j++;
				}
				j = 1;
        		k = 1;
        		while (j < Ft.BlockNumInFunction) {
					//BasicBlock temB =null;
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

	public void addPredecessor(){
		//File涓璮unction鏄粠1寮�濮嬫帓鍒楃殑,FunctionBB涓殑BB鏄粠1寮�濮嬫帓鍒楃殑.
		int i=1,k=0;
		BasicBlock temB;
		while (i <= BlockNum) {
			for(k=0;k<FileBB.get(i).SeccessorNum;k++) {
				int tem = FileBB.get(i).Successor[k];
				//System.out.print(i+" "+FileBB.get(i).BlockStartPoint+" "+tem+"\n");
				//temB = FileBB.get(FindBlockIndexByStartPoint(tem));
				temB = FileBB.get(tem);
				if (temB == null) {
					System.out.println("null not Found!!");
				}
				else {
					temB.Predecessor[temB.PredecessorNum] = i;
					temB.PredecessorNum++;
				}

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
					res = res + "(" + FileFunction.get(i).FunctionBB.get(j).BlockStartPoint+","+
							FileFunction.get(i).FunctionBB.get(j).BlockEndPoint+")";
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
	//++++++++++++++++++++++++++++++++
	//dfs
	void dfs1(int v){
		visited[v] = true;
		int k;
		for(k=0;k<FileBB.get(v).SeccessorNum;k++) {
			int tem = FileBB.get(v).Successor[k];
			if(!visited[tem]){
				dfs1(tem);
			}
		}
		stack.push(v);
	}
	void dfs2(int v,Set<Integer> set){
		visited[v] = true;
		set.add(FileBB.get(v).BlockStartPoint);
		int k;
		for(k=0;k<FileBB.get(v).PredecessorNum;k++) {
			int tem = FileBB.get(v).Predecessor[k];
			if(!visited[tem]){
				dfs2(tem, set);
			}
		}
	}
	//SCR
	public void getSCR(){
		visited = new boolean[BlockNum+2];
		scr = new Vector<Set<Integer>>();
		stack = new Stack<Integer>();
		for(int i=0;i<visited.length;i++){
			visited[i]=false;
		}
		for(int i=1;i<=BlockNum;i++){
			if(!visited[i])
				dfs1(i);
		}
		for(int i=0;i<visited.length;i++){
			visited[i]=false;
		}
		while (!stack.empty()){
			int i=stack.pop();
			if(!visited[i]) {
				this.current++;
				Set<Integer> temp = new HashSet<Integer>();
				dfs2(i,temp);
				scr.add(temp);
			}
		}
	}
	public void printSCR(){
		System.out.println(FileName+" SCR:");
		for(int i=0;i<scr.size();i++){
			if(scr.elementAt(i).size()>=2) {
				for(Integer block:scr.elementAt(i)) {
					BasicBlock t = FileBB.get(FindBlockIndexByStartPoint(block));
					for (int j = t.BlockStartPoint; j <= t.BlockEndPoint; j++) {
						scrline.add(j);
					}
				}
				System.out.println(scr.elementAt(i).toString());
			}

		}
		System.out.println(scrline);
	}

	//++++++++++++++++++++++++++++++++
	//RD鍜孡V閮芥湁
	public void getReachDefine(){
		int i=1,j=1;

		System.out.println(FileName);
		VT.printTable();
		//Todo: 鍒濆鍖栨墍鏈夌殑BB鐨凣en,Kill
		while (j < BlockNum) {
			FileBB.get(j).cal_Kill_Gen();
			FileBB.get(j).print_Kill_Gen();
			//FileFunction.get(i).FunctionBB.get(j).print_Kill_Gen();
			j++;
		}
		i=1;
		while (i <= FunctionNum) {
			FileFunction.get(i).ReachDef(this);
			System.out.println();
			FileFunction.get(i).printRD();
			FileFunction.get(i).live_Va(this);
			System.out.println();
			FileFunction.get(i).printlive();
			System.out.println("const Propagation:");
			System.out.println("Function: " + FileFunction.get(i).FunctionStartPoint);
			FileFunction.get(i).const_Propagation(Ins);
			System.out.println("Number of constants propagated:"+FileFunction.get(i).prolist.size());

			System.out.println("dead Code Eliminate:");
			Vector<Integer> de= FileFunction.get(i).deadCodeEliminate(Ins);
			int scrin = 0;
			for ( j=0;j<de.size();j++){
				if(scrline.contains(de.get(j)))
					scrin++;

			}
			System.out.println("Number of statements eliminated in SCR: "+scrin);
			System.out.println("Number of statements eliminated not in SCR: "+(de.size()-scrin));
			System.out.println("~\\(鈮р柦鈮�)/~=========================================End of Analyse of "+FileName+"======================================~\\(鈮р柦鈮�)/~\n");
			i++;
		}
	}

}