package basic;

import java.util.*;

/**
 * Created by liangjingyue on 12/3/15.
 */
public class VariableTable {
    class constPair{
        public int instr;
        public int number;
    }
    public Map<String,Set<Integer>> name = null;
    public Map<String,Set<Integer> > defSet=null;
    public Map<String,Set<Integer> > useSet=null;
    public Map<String,Set<constPair> > ConstDef = null;
    public Set<String> isArray = null;

    public VariableTable(){
        name = new HashMap<String,Set<Integer>>();
        defSet = new HashMap<String,Set<Integer> >();
        useSet = new HashMap<String,Set<Integer> >();
        ConstDef= new HashMap<String,Set<constPair>>();
        isArray = new HashSet<String>();
    }

    public String findAndAddto(int instr,int lastInstr,int defOrUse,boolean setName,boolean setArray) {
        String ver = null;
        for (Map.Entry<String, Set<Integer>> entry : name.entrySet()) {
            if (entry.getValue().contains(lastInstr)) { //濡傛灉鍦ㄧ鍙疯〃涓壘鍒颁簡涓婁竴琛屽湪绗﹀彿琛ㄤ腑鐨勪綅缃殑璇�.
                ver = entry.getKey();
                if(setName) {//濡傛灉闇�瑕�(setName涓簍rue),灏嗚繖涓�琛屼篃鎻掑叆绗﹀彿琛�
                    name.get(ver).add(instr);

                        System.out.println(ver +" was wrong at " +instr+" and "+lastInstr);
                    if(setArray)
                     isArray.add(ver);
                }
                if (defOrUse==0) { //true is for def
                    add2DefSet(ver, instr);
                } else { //use
                    if (defOrUse==1)
                    add2UseSet(ver, instr);

                }
                break;
            }
        }
        return ver;
    }

    public String addtoDef(int instr,String ver){
        add2Name(ver,instr);
        add2DefSet(ver, instr);
        return ver;
    }
    public String addtoUse(int instr,String ver){
        add2Name(ver,instr);
        add2UseSet(ver, instr);
        return ver;
    }
    public void add2Name(String vName,int i){
        if(!name.containsKey(vName)){
            Set<Integer> temp= new TreeSet<Integer>();
            temp.add(i);
            name.put(vName, temp);
            return;
        }
        name.get(vName).add(i);
    }
    public void add2DefSet(String vName,int i){
        if(!defSet.containsKey(vName)){
            Set<Integer> temp= new TreeSet<Integer>();
            temp.add(i);
            defSet.put(vName, temp);
            return;
        }
        defSet.get(vName).add(i);
    }
    public  void add2UseSet(String vName,int i){
        if(!useSet.containsKey(vName)){
            Set<Integer> temp= new TreeSet<Integer>();
            temp.add(i);
            useSet.put(vName, temp);
            return;
        }
        useSet.get(vName).add(i);
    }
    public void add2ConstSet(String vName,int i,int num){

       // 鐒跺悗鍐嶄繚瀛�<鍙橀噺,<琛屽彿,鏁板��>>瀵�
        if(!ConstDef.containsKey(vName)){
            constPair tem = new constPair();
            tem.instr = i;
            tem.number = num;
            Set< constPair> temset = new HashSet<constPair>();
            temset.add(tem);
            ConstDef.put(vName,temset);
        }
        else {
            constPair tem = new constPair();
            tem.instr = i;
            tem.number = num;
            ConstDef.get(vName).add(tem);
        }
    }
    public void printTable(){
        System.out.println("Name:");
        for (Map.Entry<String, Set<Integer>> entry : name.entrySet()) {
            System.out.println(entry.getKey() +": " +entry.getValue());
        }
        System.out.println("Def:");
        for (Map.Entry<String, Set<Integer>> entry : defSet.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Use:");
        for (Map.Entry<String, Set<Integer>> entry : useSet.entrySet()) {
            System.out.println(entry.getKey() +": " +entry.getValue());
        }
        System.out.println("Const");
        for (Map.Entry<String, Set<constPair>> entry : ConstDef.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            for(constPair tem:entry.getValue()){
                System.out.print("("+tem.instr+","+tem.number+"), ");
            }
            System.out.println();
        }
    }
    public  Integer isConstant(String v,int instr){
        Integer number = null;
        if(ConstDef.containsKey(v))
        {
            for(constPair tem:ConstDef.get(v)) {
                if (tem.instr==instr) {
                    number = tem.number;
                    break;
                }
            }
        }
        return  number;

    }
}
