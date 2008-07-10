package corrective.BoxingUnboxing.RemoveRedundantUnboxingAction.in;

/**
 *
 * @author  ars
 * @violations 4
 */
public class TestUnboxing_B {
  
  public void a(){
    int temp;
    
    for (int i = 0; i < 5; i++){
      Integer b = 4;
      temp = b.intValue();
      TestUnboxing_A.takesPrimitiveInt(temp);
    }
  }
  
  public void b(){
    Integer b = 4;
    
    for (int i = 0 ; i < 5; i++){
      int temp = b.intValue();
      TestUnboxing_A.takesPrimitiveInt(temp);
    }
  }
  
  public void c(){
    Integer b = 4;
    
    for (int i = 0 ; i < 5; i++){
      int temp = b.intValue();
      TestUnboxing_A.takesPrimitiveInt(temp);
      temp++;
    }
  }
  
  public void d(){
    Integer b = 4;
    int temp = 0;
    
    for (int i = 0 ; i < 5; temp++){
      temp = b.intValue();
      TestUnboxing_A.takesPrimitiveInt(temp);
    }
  }
}
