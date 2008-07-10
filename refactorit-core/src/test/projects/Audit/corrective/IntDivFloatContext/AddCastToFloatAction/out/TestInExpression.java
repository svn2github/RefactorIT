package corrective.IntDivFloatContext.AddCastToFloatAction.in;

/**
 * @violations 3
 */
public class TestInExpression {

  public TestInExpression(){
    
  }
  
  public void test1(){
    int a = 5;
    int b = 10;
    float c = 20;
    
    c = ((float) a)/b + c;
  }
  
  void test2(){
    int a = 3;
    int b = 4;
    float c = 3;
    
    float d = c * (((float) a) / b) + c;
  }
  
  void test3(){
    int b = 1;
    float h = 2;
    float a = (((float) b)/2) * h;
    
  }
}
