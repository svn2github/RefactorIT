package a;

import a.Test;

public class Test {
  
  public Test() {
  }
  
  static {
    new Test();
    
    a.Test test = new a.Test();

    X x = new a.X(); // should be imported
    
    a.Y y = new a.Y(); // needn't to be imported
  }
  
}
