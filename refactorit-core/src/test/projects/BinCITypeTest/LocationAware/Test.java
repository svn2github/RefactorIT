// Indentation is intentionally wrong.
// This makes sure that start column and end column of types are different.
public class Test {
  static {
    ;
  }

  {
    System.out.println("Hello");
  }
  public static final void main(String[] params) {
    final Object tmp = new Integer(13);
    System.out.println("Hello" + tmp + (13 + 15));
    System.out.println(tmp + "Hello" + (13 + 15));
    System.out.println((String) null + null);
    System.out.println((String) null + (String) null);
    System.out.println(null + (String) null);

    Runnable r =
      new Runnable()
      {
        public void run() {}
      }
      ;
  }

  interface InnerInterface {
   }
}

abstract class Test3 {
  abstract void main();

  static class Inner {
    int a ;

    class B {
            }
  }

  void test() {
    class A {
      void test() {}
    }

    A a = new A();
    a.test();
  }
}

interface Hello {
  int a, c = 3;
  void b();
  }

class Point {
  int x, y;
  Point(int x, int y) { this.x = x; this.y = y; }
}

abstract class Test4 implements Hello {}