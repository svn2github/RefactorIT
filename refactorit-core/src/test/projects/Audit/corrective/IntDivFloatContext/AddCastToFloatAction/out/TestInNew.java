package corrective.IntDivFloatContext.AddCastToFloatAction.in;

/**
 * @violations 2
 */
public class TestInNew {

public TestInNew (float x){
    float y = x;
  }

  public TestInNew (float x, int z, int f){
    this(((float) f)/z);    
  }
  
  void test6(){
    int b = 1;
    int c = 2;
    TestInNew a = new TestInNew(((float) b)/c, c, b);
  }
}
