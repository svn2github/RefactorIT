import java.util.*;

public class A {
  
  G1<Float, String> foo() {
    throw new RuntimeException("not implemented");
  }
  
  static class G1<T1, T2> {
    
  }
  
  static class G2<T2, T3, T1> extends G1<T1, T2> {
    
  }
}