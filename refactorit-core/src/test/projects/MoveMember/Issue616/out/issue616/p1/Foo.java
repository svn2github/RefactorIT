package issue616.p1;

import issue616.Bar;

public class Foo {
  
  void usesBar() {
    Foo.bar();
  }
  
  public static void bar() {
  }
}