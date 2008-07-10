class Test1 { int a; }
class Test2
{}

class Test3 {


} // Test3

class
  Test4
    implements Runnable {  public void run() {
    }
}

class Test5 {

  // Hello

}

class Test6 /** Commented out*/ {
  int b;

  void g() {
    // Hi!
  }
}

public abstract class Test {
  Test() { System.out.println("Hi!");}
  Test(int a) {
    super();
  }
  void a() { System.out.println("test"); }

  void b()
  {}

  void c() {

  }

  static
  void
  d() {
    System.out.println("Hi!");
  }

  int e()
    throws Exception {

    new Object().toString();

    return 12;


  }


  int f() {
    // Hi!
    return 1;
  }

  int g() /** Commented out*/{
    /*
     * Multiline comment
     * 
     */
    System.out.println(
      "Hello"
      + " World!");
    return 3;
  } 

  void h() {
   /**
    * Javadoc comment
    */

    System.out.println("Testing"
      + "aaaaaa");
  }

  /**
   * This is a javadoc for this method.
   */
  abstract
  int
  i()
  throws RuntimeException
  ;

  void j() {
   /**
    * Javadoc comment
    */

    System.out.println("Testing"
      + "aaaaaa"); System.out.println(
    "Test");

    System.out.println("Testing"
      + "aaaaaa");
  }
}
