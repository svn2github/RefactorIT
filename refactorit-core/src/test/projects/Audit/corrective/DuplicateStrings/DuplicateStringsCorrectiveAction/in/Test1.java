import java.util.*;
/**
* @violations 19
*/
public class Test1{
  final String field1 = "bu!";
  final String field2 = "bu!";
  final String field3 = "Bu!";
  final String field4 = "Bu!", field5 = "Bu!";
  String aa;
  String field6 = "Bu!";
  String field7 = "Bu! ";

  public static void main(String[] args) {
    String c;
    String a = new String("Bu!");
    a += "Bu!";
    System.out.println(a);
    a = "Bu! " + "Zju!";
    System.out.println(a);
    String b = new String("Bu! " + a);
    System.out.println(b);
    MultiTargetCorrectiveAction action = new MultiTargetCorrectiveAction() {
      protected Set process() {
        System.out.println("b" + "b" + "b");
        return null;
      }
    };
  }


  class NestedClass {

    public void method1() {
      String aaa = "a" + "a" + "a";
      System.out.println("Bu!");
      String a = new String("Bu!");
      a += "Bu!";
      a = "Bu! " + "Zju!";
      String b = new String("Bu! " + a);
      b += "aaa";
    }
  }
}

class MultiTargetCorrectiveAction {}
