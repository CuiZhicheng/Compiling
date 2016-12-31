package basic;
import java.util.Set;
import java.util.TreeSet;

public class BasicBlock{
	static int maxPre = 5;
	public Instruction[] Ins;
	public VariableTable vt;
	int maxInstru;
	int maxVa;
	public int BlockStartPoint;
	public int BlockEndPoint;
	public int SeccessorNum;
	public int PredecessorNum;
	public int[] Successor;//successor閲岄潰瀛樼潃鐨勬槸1-FileBB涓殑搴忓彿
	public int[] Predecessor;

	public int[] Kill;
	public int[] Gen;
	public int[] RD_IN;
	public int[] RD_OUT;

	public Set<Integer> Def;
	public Set<Integer> Use;
	public Set<String> live_IN;
	public Set<String> live_OUT;

	BasicBlock(){
		Predecessor = new int[maxPre];
		Successor = new int[2];
		BlockStartPoint = SeccessorNum = PredecessorNum= 0; //璁℃暟涓�寮�濮嬮兘璁句负0
		createArray(100,30);
	}
	public void createArray(int maxInstr, int maxV){
		maxInstru = maxInstr;
		maxVa = maxV;

		Kill = new int[maxInstr];
		Gen = new int[maxInstr];
		RD_IN = new int[maxInstr];
		RD_OUT = new int[maxInstr];

		for(int i= 0 ;i<maxInstr;i++) {
			Kill[i] = 0;
			Gen[i] = 0;
			RD_IN[i] = 0;
			RD_OUT[i] = 0;
		}

		Def = new TreeSet<Integer>();
		Use = new TreeSet<Integer>();
		live_IN = new TreeSet<String>();
		live_OUT = new TreeSet<String>();
		return;
	}
	public void setInstr(Instruction[] In,VariableTable v){
		Ins = In;
		vt = v;
	}
	//============================Reach Defination==========================================
	public void cal_Kill_Gen(){ //姹備竴涓狟B鐨凨ill闆嗗拰Gen闆�
		for(int i= BlockStartPoint ;i<=BlockEndPoint;i++){
			Instruction in = Ins[i];
			if(in.gen !=null) {
				Set<Integer> instrs =vt.defSet.get(in.gen);//i涓�鍙ュ畾鍊煎彉閲忚瀹氬�肩殑鎵�鏈夎鍙ill浣嶇疆璁句负1
				for(Integer j:instrs){
					Kill[j]=1;
				}
				Kill[i]=0; //浣嗘槸鏈鍙ヨ璁句负0
			}
		}
		for(int i= BlockEndPoint; i>=BlockStartPoint;i--){
			Instruction in = Ins[i];
			if(in.gen !=null) {
				int j;
				for(j= i+1;j<=BlockEndPoint;j++){ //濡傛灉i璇彞鐨勫畾鍊煎湪涔嬪悗鐨勮鍙ヤ腑娌℃湁琚埆鐨勫畾鍊艰鐩�
					if(Ins[j].gen!=null && Ins[j].gen.equals(in.gen))
						break;
				}
				if(j>BlockEndPoint) //Gen[i]灏卞彲浠ヨ璁句负0
					Gen[i]=1;
			}
		}
		return;
	}
	public void culutateInnerIO(){
		Ins[BlockStartPoint].RD_IN = RD_IN;
		for(int i= BlockStartPoint;i<=BlockEndPoint;i++) {
			Instruction in = Ins[i];
			if(i>BlockStartPoint)
				in.RD_IN = Ins[i-1].RD_OUT;

			for(int j= 1 ;j<maxInstru;j++) { //Out鍒濆涓篒n
				in.RD_OUT[j]=in.RD_IN[j];
			}
			if(in.gen !=null) {
				Set<Integer> instrs = vt.defSet.get(in.gen);//i涓�鍙ュ畾鍊煎彉閲忚瀹氬�肩殑鎵�鏈夎鍙ill浣嶇疆璁句负1
				for (Integer j : instrs) {
					if(in.RD_IN[j]==1)
						in.RD_OUT[j] = 0; //鍑忓幓kill闆嗗悎鐨勮繃绋�.
				}
				in.RD_OUT[i]=1;
			}
		}

	}
	public int[] cululateInOut(LabFile lf){
		for(int i= 1 ;i<maxInstru;i++) { //In鍒濆涓�0
			RD_IN[i]=0;
		}
		for(int i=1;i<PredecessorNum;i++){
			union_IN(lf.FileBB.get(Predecessor[i]).RD_OUT);
		}
		culutateInnerIO();
		int[] newOut = GenPlusInMinusKill();
		for(int i= 0 ;i<maxInstru;i++) {
			if(RD_OUT[i]!=newOut[i]) {
				RD_OUT = newOut;
				return Successor;
			}
		}
		return null;
	}

	public void union_IN(int[] Pout){
		for(int i= 0 ;i<maxInstru;i++) {
			if(RD_IN[i]==1 || Pout[i]==1)
				RD_IN[i]=1;
		}
	}
	public int[] GenPlusInMinusKill() {
		int[] result = new int[maxInstru];
		for (int i = 0; i < maxInstru; i++) {
			if(Gen[i]==1)
				result[i]=1;
			else {
				if(RD_IN[i]==1) {
					if(Kill[i]!=1)
						result[i]=1;
					else
						result[i]=0;
				}
				else
					result[i]=0;
			}
		}
		return result;
	}
	public void print_Kill_Gen(){
		System.out.print(BlockStartPoint+":\nKill: ");
		for(int i=1;i<maxInstru;i++){
			if(Kill[i]==1) System.out.print(i+", ");
		}
		System.out.print("\nGen: ");
		for(int i=1;i<maxInstru;i++){
			if(Gen[i]==1) System.out.print(i+", ");
		}
		System.out.println();
	}
	public void print_In_Out(){
		System.out.print("Block:" + BlockStartPoint + ":=========================\nRD_IN: ");
		for(int i=1;i<maxInstru;i++){
			if(RD_IN[i]==1) System.out.print(i+", ");
		}
		System.out.println();
		for(int i= BlockStartPoint;i<=BlockEndPoint;i++) {
			Instruction in = Ins[i];
			System.out.print(i+":****************\n\tRD_IN: ");
			for(int j=1;j<maxInstru;j++){
				if(in.RD_IN[j]==1) System.out.print(j+", ");
			}
			System.out.print("\n\tRD_OUT: ");
			for(int j=1;j<maxInstru;j++){
				if(in.RD_OUT[j]==1) System.out.print(j+", ");
			}
			System.out.println();
		}
		System.out.print("Block:" + BlockStartPoint + "=========================\nRD_OUT: ");
		for(int i=1;i<maxInstru;i++){
			if(RD_OUT[i]==1) System.out.print(i+", ");
		}
		System.out.println();
		System.out.println();
	}
	//============================Live Analysis===============================================
	public int[] cal_liveVable(LabFile lf,int index){
		if(index != 0) {
			live_OUT.clear();
			for (int i = 0; i < SeccessorNum; i++) {
				for (String s : lf.FileBB.get(Successor[i]).live_IN) {
					live_OUT.add(s);
				}
			}
		}
		else
		{
			live_OUT.add("all");
		}
		culutateInnerliveIO();
		if(live_IN.size() != Ins[BlockStartPoint].live_IN.size()) {
			live_IN = Ins[BlockStartPoint].live_IN;
			return Predecessor;
		}
		for(String v:live_IN){
			if(!Ins[BlockStartPoint].live_IN.contains(v)){
				live_IN = Ins[BlockStartPoint].live_IN;
				return Predecessor;
			}
		}

		return null;
	}
	public void culutateInnerliveIO(){
		Ins[BlockEndPoint].live_OUT = live_OUT;
		for(int i=BlockEndPoint;i>=BlockStartPoint;i--){
			Instruction in = Ins[i];
			if(i<BlockEndPoint)
				in.live_OUT = Ins[i+1].live_IN;
			for(String j:in.live_OUT){
				if(in.gen ==null || !in.gen.equals(j)) //x-def
					in.live_IN.add(j);
			}
			int j=0;
			while(in.use[j]!=null){
				in.live_IN.add(in.use[j]);
				j++;
			}
		}
	}
	public void printlive_In_Out(){
		System.out.print("Block:" + BlockStartPoint + ":=========================\nLive_IN: ");
		for(String in:live_IN){
			System.out.print(in+", ");
		}
		System.out.println();
		for(int i= BlockStartPoint;i<=BlockEndPoint;i++) {
			Instruction in = Ins[i];
			System.out.print(i+":****************\n\tLive_IN: ");
			for(String jin:in.live_IN){
				System.out.print(jin+", ");
			}
			System.out.print("\n\tLive_OUT: ");
			for(String jin:in.live_OUT){
				System.out.print(jin+", ");
			}
			System.out.println();
		}
		System.out.print("******************\nLive_OUT: ");
		for(String in:live_OUT){
			System.out.print(in+", ");
		}
		System.out.println();
		System.out.println();
	}
}
