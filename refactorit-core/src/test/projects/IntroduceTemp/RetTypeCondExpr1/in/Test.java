public class Test {

  public void explTest(boolean flag) {
    (flag ? new A() : new B()).hello();
    (flag ? new B() : new A()).run();
  }

  class X {
    public void hello() {
      System.out.println("Hello World!");
    }
  }

  class A
      extends X
      implements Runnable {
    public void run() {}
  }

  class B
      extends X
      implements Runnable {
    public void run() {}
  }
}
