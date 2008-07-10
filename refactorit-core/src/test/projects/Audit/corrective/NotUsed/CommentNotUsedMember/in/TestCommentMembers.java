package corrective.NotUsed.CommentNotUsedMember;

import java.io.Serializable;

//--------------------------------------------------------------------------------------
//New
//--------------------------------------------------------------------------------------
/**
 * @violations 22
 */
public class Test {
  Test() {
   Client c = null;
  }
}

class Client {
  {
   Test t = new Test();
  }
}

//--------------------------------------------------------------------------------------
//Exclude public and protected
//--------------------------------------------------------------------------------------

class A {
  public int a;
  protected int b;
  int c;
  private int d;

  {
   B b;
  }

  public A() {
  }

  protected A(int a) {
  }

  A(int a, int b) {
  }

  private A(int a, int b, int c) {
  }

  public void f1() {
  }

  protected void f2() {
  }

  void f3() {
  }

  private void f4() {
  }
}

class B {
  {
   A a;
  }
}

//--------------------------------------------------------------------------------------
//Invocation in subclass
//--------------------------------------------------------------------------------------

class Test2 {
  static {
   B2 b = new C2();
   b.method2();
  }
}

interface A2 {
  void method();
}

abstract class B2 implements A2 {
  public abstract void method();

  public void method2() {
   this.method();
  }
}

class C2 extends B2 {
  public void method() {
  }
}

//
//Unused subclass A
//

class A3 {
  public static void main(String[] args) {
   A3 a = new A3();
   a.beMoreFunny();
  }

  public void beMoreFunny() {
   System.out.println("woosle wassle!");
  }
}

class B1 extends A {
}

//
//Unused subclass B
//

class A4 {
  public static void main(String[] args) {
   A4 a = new A4();
   a.beMoreFunny();
  }

  public void beMoreFunny() {
   System.out.println("woosle wassle!");
  }
}

class B4 extends A4 {
  public void beMoreFunny() {
   System.out.println("Why did the...");
  }
}

//
//Two branches
//

class A5 {
  void test() {}
  }

  class B15 extends A5 {}

  class C15 extends B15 {
  void test() {}
  }

  class B25 extends A5 {
  }

  class C25 extends B25 {
  void test() {}
  }

  class D15 extends C15 {
  void test() {}
  }

  class Test5 {
  Test5(int a) {}

  {
   B15 b = new C15();
   b.test();
  }
}

//
//Used inside only
//

class Test6 {
static final String NAME = Test6.class.getName();
}

//
//Used inside only 2
//

class Test7 {
  static final String NAME = Test7.class.getName();

  static int f = 13;

  Test7() {}

  static void test() {
   System.out.println(f);
   test2();
  }

  static void test2() {
   test();
  }

  void test3() {
   test3();
   Inner i = new Inner();
   i.abc();
   System.out.println(Inner.NAME);
  }

  static class Inner {
    static final String NAME = Inner.class.getName();
    void abc() {
      f = 14;
      test2();
      Test7 t = new Test7();
      t.test();
    }
  }
}

//
//Used private constructor
//

class Test8 {

  static final Test8 instance = new Test8();

  {
    Complimentary comp;
  }

  private Test8() {
  }
}

class Complimentary {
  {
   Test8 test;
   Test9 testmore;
   System.out.println(test.instance);
  }
}

class Test9 {
  public void meth1() {
    /**
     * javadoc
     */
  }
  
  public void meth2() {
    // single line
  }
  
  public void meth3() {/*simplecomment*/}
  
  public void meth4() {
    /*onemore*/
    /*onemore*/
    /**javadoc*/
  }
  
  public void meth5() {
    /* this method is really important
     * /*it contains additional inner opening comments
     * /** and javadoc opening also
     * */
  }
  
  public void meth6() {
    // /*should*/
    // /*not*/
    // /*replace*/
    // /*this*/
  }
  
  public void meth7() {
    /**
     * //should
     * //replace  
     * //this 
     */
  }
  
}

