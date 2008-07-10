class B {

  public void f() {
    System.out.println("A:" + (new A()));
  }
}

public class A {

  public String toString() {
    return "";
  }
}
