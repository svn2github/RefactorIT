import java.util.*;
/**
* @violations 19
*/
public class Test1{
  final static String str1 = "Bu! ";
  final static String str2 = "Zju!";
  final String field1 = "bu!";
  final String field2 = "bu!";
  final String field3 = "Bu!";
  final String field4 = "Bu!", field5 = "Bu!";
  String aa;
  String field6 = field3;
  String field7 = str1;

  public static void main(String[] args) {
    String c;
    String a = new String(field3);
    a += field3;
    System.out.println(a);
    a = str1 + str2;
    System.out.println(a);
    String b = new String(str1 + a);
    System.out.println(b);
    MultiTargetCorrectiveAction action = new MultiTargetCorrectiveAction() {
      final static String str3 = "b";
      protected Set process() {
        System.out.println(str3 + str3 + str3);
        return null;
      }
    };
  }


  class NestedClass {
    final static String str4 = "a";

    public void method1() {
      String aaa = str4 + str4 + str4;
      System.out.println(field3);
      String a = new String(field3);
      a += field3;
      a = str1 + str2;
      String b = new String(str1 + a);
      b += "aaa";
    }
  }
}

class MultiTargetCorrectiveAction {}
