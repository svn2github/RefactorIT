package NestedBlock;


public class A {
  /**
   */
  {
  }

  /** 
   * @audit NestedBlock
   */
  public void test1(){
    {
    }
  }

  /**
   */
  public void test2(){
    class Test2Local {
    }
  }
  
  /**
   * @audit NestedBlock
   */
  public void test3(){
    switch(System.currentTimeMillis()){
      case 0L:
        {
          System.out.println("It is the midnight, January 1, 1970 UTC");
        }
        break;
      default:  
        break;
    }
  }

  /**
   */
  public void test4(){
    label:
    {
    }
  }
}
