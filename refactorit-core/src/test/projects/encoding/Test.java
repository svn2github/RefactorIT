/**
  Encoded strings and characters parsing test. Bug 1949
  @author Tonis Vaga
*/
public class Test {
    /* ""+"" 
     */ // testing multiline comments
    // ""+"" single line comments
  public static void main(String args[]) {
      // testing in strings and characters
      String str="proov\""+""+""+" end";
      char ch='';
      char ch2='\'';
      System.out.println("str="+str);
      System.out.println("ch="+ch);
  }
}
