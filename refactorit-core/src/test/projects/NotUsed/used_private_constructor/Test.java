public class Test {

  static final Test instance = new Test();

  {
    Complimentary comp;
  }

  private Test() {
  }
}

class Complimentary {
  {
    Test test;
    test.instance;
  }
}