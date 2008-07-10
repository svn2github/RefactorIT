package corrective.BoxingUnboxing.RemoveRedundantUnboxingAction.in;
/**
 *
 * @author  ars 
 * @violations 1
 */
public class TestUnboxing_C {
  public static void takesPrimitiveInt(int a){
    
  }

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
