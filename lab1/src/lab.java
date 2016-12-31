import java.io.File;



public class lab{
	public String DirectoryPath;
	public String OutputPath;
	public Trans_3ddr_To_Easy T3TE;
	public int FileNum;
	public LabFile[] LF;
	
	public void getDirectoryPath(String fir, String sec){
		int i;
		//Scanner Console=new Scanner(System.in);
		String DirectoryPath = fir;
		String OutputPath = sec;
		if (!DirectoryPath.endsWith("/"))
			DirectoryPath = DirectoryPath + "/";
		File directory = new File(DirectoryPath); 
		File[] files = directory.listFiles();
		FileNum = files.length;
		LF = new LabFile[FileNum];
		i = 0;
		for (File f : files) {
			LF[i] = new LabFile();
			LF[i].FilePath = f.getPath();
			LF[i].FileName = f.getName();
			if (OutputPath.endsWith("/"))
				LF[i].FileOutputPath = OutputPath;
			else 
				LF[i].FileOutputPath = OutputPath + "/";	
			File tempf = new File(LF[i].FileOutputPath);
			if (!tempf.exists() || ! tempf.isDirectory()) tempf.mkdirs();
			i++;
			
		}
	}
	
	public void Init() {
		int i;
		for (i = 0; i < FileNum; i++) {
			LF[i].InputInstruction();
		}
	}
	
	public void lab1(){
		int i;
		for (i = 0; i < FileNum; i++) {
			LF[i].trans_3ddr_to_c();
			//break;
		}
	}
	
	public void trans_3ddr_to_easy(){
		int i;
		for (i = 0; i < FileNum; i++)
			LF[i].trans_3ddr_to_easy();
	}
	
	public void getCFG(){
		int i;
		for (i = 0; i < FileNum; i++) { 
			LF[i].getCFG();
			LF[i].publishCFG();
		}
		//LF[1].publishCFG();
	}
	
	static public void main(String[] argv) {
		lab labobj = new lab();
		if (argv.length != 2)
			System.out.println("please input 2 paramters: input-path and output-path");
		labobj.getDirectoryPath(argv[0], argv[1]);
		labobj.Init();
		labobj.lab1();
		//labobj.trans_3ddr_to_easy();
		//labobj.getCFG();

	}
}