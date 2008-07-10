package NonStaticReference;


public class A {
  static int COUNTER = 0;

  /**
   */
  public void test1(){
    COUNTER += 1;
  }

  /**
   */
  public void test2(){
    COUNTER++;
  }

  /**
   * @audit NonStaticFieldAccess
   */
  public void test3(){
    this.COUNTER += 1;
  }

  /**
   * @audit NonStaticFieldAccess
   */
  public void test4(){
    this.COUNTER++;
  }

  /**
   */
  public void test5(){
    A.COUNTER += 1;
  }

  /**
   */
  public void test6(){
    A.COUNTER++;
  }
}

class AA extends A {
  /**
   * @audit NonStaticFieldAccess
   */
  public void test7(){
    super.COUNTER += 1;
  }

  /**
   * @audit NonStaticFieldAccess
   */
  public void test8(){
    super.COUNTER++;
  }

  /**
   */
  public void test9(){
    A.COUNTER++;
  }

  /**
   */
  public void test10(){
    AA.COUNTER++;
  }
}
