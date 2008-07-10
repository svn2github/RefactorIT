package p1;

public class A {

  public static void method() {
    anotherMethod();
  }

  public static void anotherMethod() {
    method();
  }
}
