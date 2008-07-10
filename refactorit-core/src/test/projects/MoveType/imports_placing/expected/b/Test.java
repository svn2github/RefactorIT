package b;

import a.X;

public class Test {
  
  public Test() {
  }
  
  static {
    new Test();
    
    b.Test test = new b.Test();

    X x = new a.X(); // should be imported
    
    a.Y y = new a.Y(); // needn't to be imported
  }
  
}
