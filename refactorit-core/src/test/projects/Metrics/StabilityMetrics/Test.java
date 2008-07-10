import a.*;

public class A /* +1 Ce for this package (Object)*/ {
  public static void test(B b) {
    b.test();
    Test2 t = null; // +1 Ce for this package
    t.test(); // +0 Ce for this package
    Hello h = null; // +1 Ce for this package
    h.test(); // +0 Ce for this package
    System.out.println("Hello" + "a"); // +3 Ce for this package
                                       // System, PrintWriter, String.toString
  }
}

public class B extends A {
  public void test() {}
}

public abstract class Base {
  public void test() {}
}

public interface Doable {}

class C {
  static {
    int a = Test5.A; // +1 Ce for this package
    Test6.main(null); // +1 Ce for this package
    Object tmp = (Test7) null; // +1 Ce for this package
    Test8.a = 14; // +1 Ce for this package
  }
}

class D implements Hello.HelloInner /* +1 Ce for this package */ {
  public void test() {}
}

class E extends Test9.Test10 /* +2 Ce for this package */ {}