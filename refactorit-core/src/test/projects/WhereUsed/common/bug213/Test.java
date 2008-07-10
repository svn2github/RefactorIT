interface X {
  void method();
}

class Test implements X {
  void test() {
    ((X) this).method();
        
    Test test = new Test();
    ((X) test).method();
    
    X x = new Test();
    x.method();
  }
  
  public void method() {}
}