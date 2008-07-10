public class A {
  protected InnerClass1 ic;
  protected int val;

  public A() {
    ic = new InnerClass1();
  }

  public void displayStrings() {
    System.out.println(getString() + ".");
    System.out.println(ic.getAnotherString() + ".");
    B.doSomethingWithVal(this);
  }

  public String getString() {
    return "InnerClass1: getString invoked";
  }

  public int value() {
    return this.val;
  }

  public void proov2() {
  }

  public void proov() {
    int val=value();
  }

  public static void main(String[] args) {
    A c1 = new A();
    B.doSomethingWithVal(c1);
    c1.displayStrings();
    c1.proov();
    c1.proov2();
  }

  public class AStaticNestedClass {
  }

  protected class InnerClass1 {
    public String getAnotherString() {
      return "InnerClass1: getAnotherString invoked";
    }
  }
}
