package a;

import A;
import B;
import Base;

class Test /* +1 Ce for this package (Object) */ {
  private Test(A a /* +1 Ca for default package, +1 Ce for this package */) {}
}

public class Test2 extends Base /* +1 Ca for default package, +1 Ce for this package */ {}

class Test3 {
  {
    B b = null; // +1 Ce for this package, +1 Ca for default package
  }
}

class Test4 {
  {
    B b = null;
    b.test(); // +1 Ca for default package
  }
}

public interface Hello {
  interface HelloInner {}
  
  void test();
}

public class Test5 {
  public static final int A = 1;
}

public class Test6 {
  public static void main(String[] params /* +1 Ce for this package */ ) {}
}

public class Test7 {}

public class Test8 {
  public static int a = 13;
}

public class Test9 {
  public static class Test10 {}
}