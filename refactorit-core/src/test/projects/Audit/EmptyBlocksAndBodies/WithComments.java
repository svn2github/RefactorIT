/*
 * EmptyBlocks.java
 *
 * Created on February 22, 2005, 6:21 PM
 */

package audits;

/**
 *
 * @author  Arseni Grigorjev
 */
public class WithComments {
  
  /**
   * @audit EmptyBlocksAndBodies
   */
  public void a(){
    
  }
  /**
   *
   */  
  public void b(){
    // comment1
  }
  /**
   *
   */  
  public void c(){ /*comment2*/ }
  /**
   *
   */  
  public void d(){  /** comment3 */ }
  /**
   * @audit EmptyBlocksAndBodies
   */  
  public void e() /* comment4 */ { }
  /**
   * @audit EmptyBlocksAndBodies
   */  
  public void f() { }/* comment5 */ 
  /**
   *
   */  
  public void g() {/**comment6*/ }
  
}
