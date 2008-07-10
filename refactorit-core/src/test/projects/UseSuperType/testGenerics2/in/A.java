import java.util.*;

public class A {
  
  G2<String, Integer, Float> foo() {
    throw new RuntimeException("not implemented");
  }
  
  static class G1<T1, T2> {
    
  }
  
  static class G2<T2, T3, T1> extends G1<T1, T2> {
    
  }
}