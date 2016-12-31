import java.io.File;
import java.util.Scanner;

import basic.LabFile;



public class lab2{
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
		FileNum = 1;
		FileNum = files.length;
		LF = new LabFile[FileNum];
		i = 0;
		if (!OutputPath.endsWith("/"))
			OutputPath += "/";
		File ff = new File(OutputPath);
		if (!ff.exists() || !ff.isDirectory())
			ff.mkdirs();
		for (File f : files) {
			if(f.isDirectory() || f.getName().indexOf(".3ddr")==-1) {
				FileNum--;
				continue;
			}
			LF[i] = new LabFile(); //file鏄粠0寮�濮嬬殑
			LF[i].FilePath = f.getPath();
			LF[i].FileName = f.getName();
			LF[i].FileOutputPath = OutputPath;
				
			i++;
			if(i==FileNum)break;
		}
	}
	
	public void trans_3ddr_to_easy(){
		int i;
		for (i = 0; i < FileNum; i++) {
			LF[i].trans_3ddr_to_easy();

		}
	}
	
	
	@SuppressWarnings({ "resource", "unused" })
	public void getCFG(){
		int i;
		for (i = 0; i < FileNum; i++) { 
			LF[i].getCFG();
			LF[i].addPredecessor();
			LF[i].publishCFG();
			LF[i].getSCR();
			LF[i].printSCR();
			System.out.println("please input any key to comtinue");
			Scanner Console=new Scanner(System.in);
			String s = Console.next();
		}
		//LF[1].publishCFG();
	}

	@SuppressWarnings({ "resource", "unused" })
	public void getRE() {
		int i;
		for (i = 0; i < FileNum; i++) {
			LF[i].getReachDefine();
			System.out.println("please input any key to comtinue");
			Scanner Console=new Scanner(System.in);
			String s = Console.next();
		}
	}
	static public void main(String[] argv) {
		lab2 lab = new lab2();
		if (argv.length != 2) {
			System.out.println("please input 2 paramters : input-path and output-path");
		}
		lab.getDirectoryPath(argv[0], argv[1]);
		lab.trans_3ddr_to_easy();
		lab.getCFG();
		lab.getRE();

	}


}