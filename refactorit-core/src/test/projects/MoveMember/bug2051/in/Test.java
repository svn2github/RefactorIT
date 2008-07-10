package a;

import b.Another;


public class Test {

  public static Inner method(Inner[] inner) throws Inner, Inner {
    Another.method();
    Object third = new Inner();
    String s = ((Inner) ((Inner) third)).toString();
    Object o = new Object() {
      public void anotherMethod() {
        Inner inner;
      }
    };
    if (third instanceof Inner) {
      throw new Inner();
    }

    class Local extends Inner {
    };

    try {
      throw third;
    } catch (Inner i) {
    }

    return third;
  }

  public static class Inner extends Exception {
  }
}
