/*
 * TestModifierOrder.java
 *
 * Created on September 2, 2004, 12:25 PM
 */

/**
 *
 * @author  ars
 * @violations 7
 */
public class TestModifierOrder {
  public final Object aaa = new Object() { int buj = 5; }, 
      bbb = new Object() { int buj = 3; };
  public int mahjong = 15;
  public final int asja = 3, vasja = 1;
  static final int zed = 12345;
  private static final float zjuzja = 2;
  /** Creates a new instance of TestModifierOrder */
  public TestModifierOrder() {
  }
  
  public static void kata(){
    
  }
  
  public static void mata(){
    
  }
  
  private static final void zeta(){
    
  }
  
  public static final class InnerStaticFinalCorrect{
    
  }
  
  public static final class InnerStaticFinalWrong{
    
  }
}


