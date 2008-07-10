public class Test {
  public void test() {
    try {
      throw new B();
    } catch (B e) {
    }
  }
}

class A extends Exception {
}

class B extends A {
}
