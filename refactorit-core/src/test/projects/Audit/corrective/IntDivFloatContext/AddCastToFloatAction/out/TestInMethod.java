package corrective.IntDivFloatContext.AddCastToFloatAction.in;

/**
 * @violations 1
 */
public class TestInMethod {

  public TestInMethod(){
    
  }
  
  public void test1(){
    int a = 5;
    int b = 10;
    float c = 20;
    
    needsFloat(c,(((float) (a+b)))/b,c);
  }
  
  public void needsFloat(float c1, float a, float c2){
    float b = a;
  }
}
