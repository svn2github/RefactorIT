package StringEqualsOrder;

public class StringEquals {

  private String f = "f";
  /**
   * @audit StringEqualsOrder
   */
  private boolean areEqual = f.equals("f");

  /**
   * @audit StringEqualsOrder
   */
  public void meth1() {
    String a = "a";

    if (a.concat("bc").equals("abc")) {
      // do smth
    }
  }

  /**
   * @audit StringEqualsOrder
   */
  public void meth2() {
    String b = "b";
    boolean isEqual = b.equals("b");
  }

  /**
   * @audit StringEqualsOrder
   */
  public void meth3() {
    String[] a = new String[4] { "a", "b", "c", "d" };
    if (a[0].equals("a")) {
      // do smth
    }
  }

  /**
   * @audit StringEqualsOrder
   */
  public void meth4() {
    if (String.valueOf(true).equals("true")) {
      // do smth
    }
  }

  /**
   * @audit StringEqualsOrder
   */
  public void meth5() {
    String z = "ab";
    if (z.equals("a" + "b")) {
      // do smth
    }
  }

  /**
   * @audit StringEqualsOrder
   */
  public void meth6() {
    String z = "ab";
    if (z.equals("a" + "b" + "c" + "d")) {
      // do smth
    }
  }

  /**
   * 
   */
  public void meth7() {
    String z = "ab";
    if (z.equals("a" + "b" + "c" + z)) {
      // do smth
    }
  }
}
