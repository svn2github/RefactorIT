package FinalMethodProposal;

public class A {
  /**
   *
   */
  public void method1(int a, int b){
    
  }
  /**
   *
   */
  public int method2(int a, int b){
    
  }
  
}

class B extends A {
/**
 * @audit FinalMethodProposal
 */
  public void method1(int c, int d){
    
  }
  public void method3(){
    
  }
}

/**
 * @audit FinalClassProposal
 */
class C extends B {
/**
 * @audit FinalMethodProposal
 */
  public void method1(){
    
  }
  
/**
 * @audit FinalMethodProposal
 */
  public int method2(int e, int f){
    
  }
  
/**
 * @audit FinalMethodProposal
 */
  public void method3(){
    
  }
}
