import java.lang.reflect.Method;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Test {

  public static final void main(String[] params) {
    dumpMethods(Test.class);
    dumpMethods(InterfaceA.class);
    dumpMethods(InterfaceB.class);
    dumpMethods(A.class);
    dumpMethods(B.class);
    dumpMethods(C.class);
    dumpMethods(D.class);
    dumpMethods(G.class); dumpMethods(H.class); dumpMethods(I.class);
  }

  private static void dumpMethods(Class cls) {
    System.out.println("Methods of " + cls);
    final Method[] methods = cls.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      System.out.println(methods[i]);
    }
  }
}

interface InterfaceA {
  void a() throws IOException;
  void b();
}

interface InterfaceB extends InterfaceA {
  void c();
}

abstract class A implements InterfaceA {
  public void a() throws UnsupportedEncodingException {}
}

abstract class B extends A implements InterfaceA, InterfaceB {}

class C extends B {
  public void b() {}
  public void c() {}
}

abstract class D implements InterfaceB {}

abstract class E implements InterfaceA {}
abstract class F extends E {}
abstract class G extends E implements InterfaceA {}

class H {
  void c() {}
}

abstract class I extends H implements InterfaceA {
}