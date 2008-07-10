package BooleanLiteralComparison;


public class A {
  /**
   * @audit BooleanLiteralComparison true
   */
  public void test1(){
    if(condition() == true){
      return;
    }
  }

  /**
   */
  public void test2(){
    if(condition()){
      return;
    }
  }

  /**
   * @audit BooleanLiteralComparison false
   */
  public void test3(){
    if(condition() == false){
      return;
    }
  }

  /**
   */
  public void test4(){
    if(!condition()){
      return;
    }
  }
  
  /**
   */
  private boolean condition(){
    return (System.currentTimeMillis() % 1000L) > 500L;
  }
}
