package corrective.StringEqualsOrder.ChangeEqualsOrder.in;
/**
 * @violations 7
 */

public class TestStringEquals {

  private String f = "f";

  private boolean areEqual = f.equals("f");

  public void meth1() {
    String a = "a";

    if (a.concat("bc").equals("abc")) {
      // do smth
    }
  }

  public void meth2() {
    String b = "b";
    boolean isEqual = b.equals("b");
  }


  public void meth3() {
    String[] a = new String[4] { "a", "b", "c", "d" };
    if (a[0].equals("a")) { 
      // do smth
    }
  }


  public void meth4() {
    if (String.valueOf(true).equals("true")) {
      // do smth
    }
  }


  public void meth5() {
    String z = "ab";
    if (z.equals("a" + "b")) {
      // do smth
    }
  }


  public void meth6() {
    String z = "ab";
    if (z.equals("a" + "b" + "c" + "d")) {
      // do smth
    }
  }


  public void meth7() {
    String z = "ab";
    if (z.equals("a" + "b" + "c" + z)) { // No violation
      // do smth
    }
  }
}
