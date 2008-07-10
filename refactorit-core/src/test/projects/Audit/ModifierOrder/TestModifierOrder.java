/**
 * 
 * @author  ars
 */
public class TestModifierOrder {
/**
 * @audit ModifierOrder
 */
  final public Object aaa = new Object() { int buj = 5; }, 
      bbb = new Object() { int buj = 3; };
/**
 *
 */
  public int mahjong = 15;
/**
 * @audit ModifierOrder	
 */
  final public int asja = 3, vasja = 1;
/**
 * @audit ModifierOrder
 */
  final static int zed = 12345; 
/**
 * @audit ModifierOrder
 */
  final private static float zjuzja = 2;
  /** Creates a new instance of TestModifierOrder */
  public TestModifierOrder() {
  }
/**
 *
 */  
  public static void kata(){
    
  }
/**
 * @audit ModifierOrder	
 */  
  static public void mata(){
    
  }
 /**
 * @audit ModifierOrder	
 */ 
  final static private void zeta(){
    
  }
 /**
 *
 */ 
  public static final class InnerStaticFinalCorrect{
    
  }
 /**
 * @audit ModifierOrder	
 */ 
  final public static class InnerStaticFinalWrong{
    
  }
}


