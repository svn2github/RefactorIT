public class A {
  protected InnerClass1 ic;
  protected int val;

  public A() {
    ic = new InnerClass1();
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

  public class AStaticNestedClass {
  }

  protected class InnerClass1 {
    public String getAnotherString() {
      return "InnerClass1: getAnotherString invoked";
    }
  }
}

class C {

  public void displayStrings(A a) {
    System.out.println(a.getString() + ".");
    System.out.println(a.ic.getAnotherString() + ".");
    doSomethingWithVal(a);
  }

  int doSomethingWithVal(A a) {
    return a.val*2;
  }
}
