package NumericLiterals;

/**
 * @audit NumericLiteralField
 * @audit NumericLiteralField
 * @audit NumericLiteralField
 * @audit NumericLiteral
 * @audit NumericLiteral
 * @audit NumericLiteral
 *
 * @author  ars
 */
public class A {
  
  /**
   * 
   */
  public static int a = 4;
  
  /**
   * 
   */
  public final int b = 3;
  
  /**
   * 
   */
  public int c = 345;
  
  
  public static final int MINUS_FIVE = -5;
  
  // -1, 0 and 1 are skiped by default
  public int d0 = -1;
  public int d1 = 0;
  public int d2 = 1;
  
  /**
   * 
   */
  public int test1(){
    return 5;
  }
  
  /**
   */
  public int test2(){
    int a = 6;
    if (a == 3){
      return a;
    }
  }
  
}
