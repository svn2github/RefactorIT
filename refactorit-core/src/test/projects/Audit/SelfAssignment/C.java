package SelfAssignment;


public class C {
  /**
   * @audit SelfAssignmentOnVariable value 
   */
  public void test1(){
    String value = null;
    value = value;
  }

  /** 
   * @audit SelfAssignmentOnVariable value
   */
  public void test2(){
    String value = null;
    value = (condition() ? value : null);
  }

  /**
   * @audit SelfAssignmentOnVariable param
   */
  public void test3(String param){
    param = param;
  }
  
  /**
   * @audit SelfAssignmentOnVariable param
   */
  public void test4(String param){
    param = (condition() ? (param = null) : (condition() ? param : null));
  }
  
  /**
   */
  private boolean condition(){
    return (System.currentTimeMillis() % 1000L) < 500L;
  }
}
