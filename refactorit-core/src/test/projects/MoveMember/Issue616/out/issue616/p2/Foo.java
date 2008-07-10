package issue616.p2;

import issue616.Bar;

public class Foo {
  
  void usesBar() {
    issue616.p1.Foo.bar();
  }
}