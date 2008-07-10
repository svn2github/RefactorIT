public class A {
  public A() {}

  public A(int a) {}
}

class B extends A {
  public B() {
    super(1);
  }
  
  public B(String tmp) {
    super();
  }
}