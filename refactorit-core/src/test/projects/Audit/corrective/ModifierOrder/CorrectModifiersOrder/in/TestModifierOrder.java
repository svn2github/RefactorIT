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
  final public Object aaa = new Object() { int buj = 5; }, 
      bbb = new Object() { int buj = 3; };
  public int mahjong = 15;
  final public int asja = 3, vasja = 1;
  final static int zed = 12345;
  final private static float zjuzja = 2;
  /** Creates a new instance of TestModifierOrder */
  public TestModifierOrder() {
  }
  
  public static void kata(){
    
  }
  
  static public void mata(){
    
  }
  
  final static private void zeta(){
    
  }
  
  public static final class InnerStaticFinalCorrect{
    
  }
  
  final public static class InnerStaticFinalWrong{
    
  }
}


