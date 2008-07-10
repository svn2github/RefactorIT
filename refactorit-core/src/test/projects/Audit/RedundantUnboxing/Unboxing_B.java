package RedundantUnboxing;

/**
 *
 * @author  ars
 */
public class Unboxing_B {
  
  /**
   * @audit RedundantUnboxing
   */
  public void a(){
    int temp;
    
    for (int i = 0; i < 5; i++){
      Integer b = 4;
      temp = b.intValue();
      Unboxing_A.takesPrimitiveInt(temp);
    }
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void b(){
    Integer b = 4;
    
    for (int i = 0 ; i < 5; i++){
      int temp = b.intValue();
      Unboxing_A.takesPrimitiveInt(temp);
    }
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void c(){
    Integer b = 4;
    
    for (int i = 0 ; i < 5; i++){
      int temp = b.intValue();
      Unboxing_A.takesPrimitiveInt(temp);
      temp++;
    }
  }
  
  /**
   * @audit RedundantUnboxing
   */
  public void d(){
    Integer b = 4;
    int temp = 0;
    
    for (int i = 0 ; i < 5; temp++){
      temp = b.intValue();
      Unboxing_A.takesPrimitiveInt(temp);
    }
  }
}
