public class Test {
  {
    Test test = new Test();
  }

  public Test() {
  }
}

class Other {
  {
    new Test() {
    };
  }
}
