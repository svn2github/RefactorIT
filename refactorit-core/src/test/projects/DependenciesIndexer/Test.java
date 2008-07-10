class Test1 {
  void test() {};
}

class Test2 {
  static {
    new Test1();
  }
}

class Test3 extends Test1 {
  
  public Test3() {
    super();
  }
  
  public Test3(Object o) {
    this();
  }
  
}

class Test4 extends Test3 {
  {
    System
        .out
        .println(
        "Hello" 
        + this);
  }
}
