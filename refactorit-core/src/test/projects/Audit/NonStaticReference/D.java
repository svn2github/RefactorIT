package NonStaticReference;


public class D {
  /**
   */
  public void test1(){
    C.usefulMethod();
  }

  /**
   * @audit NonStaticMethodAccess
   */
  public void test2(){
    C interim = new C();
    interim.usefulMethod();
  }
}
