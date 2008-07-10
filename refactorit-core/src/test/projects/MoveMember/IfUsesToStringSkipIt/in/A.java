class B {
}

public class A {
  public void f() {
    System.out.println("A:" + (new A()));
  }

  public String toString() {
    return "";
  }
}
