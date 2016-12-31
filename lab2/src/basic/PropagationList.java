package basic;

/**
 * Created by liangjingyue on 12/8/15.
 */
public class PropagationList {
    int instr;
    String v;
    int number;
    public String toString(){
        String result = "line";
        result += instr;
        result +=", "+v;
        result +="=>"+number +"\n";
        return result;
    }
}
