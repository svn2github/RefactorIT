package StringToString;


public class A {
  /** 
   * @audit StringToString
   */
  public void test1(){
    "test".toString();
  }

  /**
   * @audit StringToString
   */
  public void test2(){
    new String().toString();
  }

  /**
   * @audit StringToString
   */
  public void test3(){
    stringMethod().toString();
  }

  /**
   */
  private String stringMethod(){
    return "";
  }
}
