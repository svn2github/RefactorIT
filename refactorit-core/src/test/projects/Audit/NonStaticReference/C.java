package NonStaticReference;


public class C {
  static void usefulMethod(){
  }
  
  /**
   */
  void test1(){
    usefulMethod();
  }

  /**
   * @audit NonStaticMethodAccess
   */
  void test2(){
    this.usefulMethod();
  }

  /**
   * @audit NonStaticMethodAccess
   */
  void test3(){
    C interim = new C();
    interim.usefulMethod();
  }
}
