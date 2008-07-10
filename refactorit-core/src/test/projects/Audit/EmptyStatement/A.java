package EmptyStatement;


public class A {
  /**
   * @audit EmptyStatement
   */
  public void test1(){
    ;
  }
  
  /** 
   * @audit EmptyStatement
   */
  public void test2(){
    class Inner {
    };
  }
  
  /** 
   * @audit EmptyStatement
   */
  public void test3(){
    for(;;) {
      ;
    }
  }
  
  /** 
   * 
   */
  public void test3(){
    int i=1;
    while(i++ < 10);
  }
  
  /** 
   * 
   */
  public void test4(){
    for(int i = 0; i < 10; i++);
  }
  
  
  
  
}
