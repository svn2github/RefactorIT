package corrective.NotUsed.RemoveNotUsedType;

import java.io.Serializable;

//--------------------------------------------------------------------------------------
//New
//--------------------------------------------------------------------------------------
/**
 * @violations 25
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

  

  class D15 extends C15 {
  void test() {}
  }

  

//
//Used inside only
//



//
//Used inside only 2
//



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

class E {
  
  public static void main(String[] args) {
  }
  
  private static String UNUSED_VAR1 = "lala.lala.gala";
  private static String UNUSED_VAR2 = "lala.lala.pala";
  private static String UNUSED_VAR3 = "lala.lala.fala";
  
  private static String USED_VAR1 = "lala.lala.usedvara";
  private static String USED_VAR2 = "lala.lala.usedvarb";
  
  public void test1(){
    // only assigned, never read
    UNUSED_VAR1 = "ttt";
    
    // used on READ, but assigned to itself - in fact unused
    UNUSED_VAR2 = UNUSED_VAR2.substring(0, UNUSED_VAR2.indexOf('.'));
    
    // used on READ properly, so not unused
    System.out.println(USED_VAR1.substring(0, USED_VAR1 .indexOf('.')));
    
    // unused variable UNUSED_VAR3
    UNUSED_VAR3 = USED_VAR2 + "";
  }
}


