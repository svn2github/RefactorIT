public class Test {
  {
    Test test = createTest();
  }

  protected Test() {
  }

  public static Test createTest() {
    return new Test();
  }
}

class Other {
  {
    new Test() {
    };
  }
}
