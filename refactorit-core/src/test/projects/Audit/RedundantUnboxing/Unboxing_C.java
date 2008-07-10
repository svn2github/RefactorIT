package RedundantUnboxing;

/**
 *
 * @author  ars
 */
public class Unboxing_C {
  public static void takesPrimitiveInt(int a){
    
  }

  /**
   * @audit RedundantUnboxing
   */
  public void a(){
    Integer wrapperName = 7;
    final int temp = wrapperName.intValue();
        
    new Object() {
      int wrapperName = 9;
      
      public boolean equals(Object o){
        takesPrimitiveInt(temp);
        return true;
      }
    };
  }
  
}
