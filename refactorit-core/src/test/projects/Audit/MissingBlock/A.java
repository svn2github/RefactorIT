package MissingBlock;


public class A {
  /**
   * @audit BlocklessForStatement
   */
  public void test1(){
    for(int i = 0; i < 10; i++);
  }
  
  /**
   */
  public void test2(){
    for(int i = 0; i < 10; i++){
    }
  }

  /**
   * @audit BlocklessIfStatement
   * @audit BlocklessIfStatement
   * @audit BlocklessIfStatement
   * @audit BlocklessElseStatement
   */
  public void test3(){
    int i = 0;
    
    int next = next();

    if(next > 8) i++; // bug
    else
    if(next > 6); // bug
    else
    if(next > 4) --i; // bug
    else; // bug
  }

  /**
   */
  public void test4(){
    int i = 0;
    
    int next = next();

    if(next > 8){
      i++;
    } else
    if(next > 6){
    } else
    if(next > 4){
      --i;
    } else {
    }
  }
  
  /*
   * FIXME: change into javadoc if needed, BlocklessLabeledStatement 
   * violation code is commented out in MissingBlockRule
   * @audit BlocklessLabeledStatement label
   */
  public void test5(){
    int i = 0;
    
    label: i++;
  }

  /**
   */
  public void test6(){
    int i = 0;
    
    label:
      {
        i++;
      }
  }
  
  /**
   * @audit BlocklessWhileStatement
   */
  public void test7(){
    int i = 0;
    
    int next = next();

    while(next-- > 0) i++;
  }

  /**
   */
  public void test8(){
    int i = 0;
    
    int next = next();

    while(next-- > 0){
      i++;
    }
  }

  /**
   * @audit BlocklessDoStatement
   */
  public void test9(){
    int i = 0;
    
    int next = next();

    do
      i++;
    while(next-- > 0);
  }

  /**
   */
  public void test10(){
    int i = 0;
    
    int next = next();

    //
    do {
      ++i;
    } while(next-- > 0);
  }

  /**
   */  
  private int next(){
    return RANDOM.nextInt(10);
  }
  
  //
  //
  //
  
  private static final java.util.Random RANDOM = new java.util.Random();
}
