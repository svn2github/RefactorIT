package genericsrefact.test5_1;

import java.util.List;
import java.util.Map;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Test5_1 {
  
  public void testMethod() {
    TB tb1 = new TB();
    TB tb2 = new TB();
    TB tb = new TB();
    
    (Bar<String, Double>) tb1.getB1();
    (Bar<String, Integer>) tb2.getB1();
    
    //(String) tb.getB2();
    
    tb = tb2;
    tb = tb1;
  }
}

class TA <A1, A2> {
  
}

class TB <B1, B2> extends TA<Map<B1, B2>, List<B1>>{
  B1 getB1(){
    return null;
  }
  
  B2 getB2(){
    return null;
  }
}

class Foo<F> {
  
}

class Bar<B1, B2>{
  
}
