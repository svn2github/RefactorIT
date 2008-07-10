package p1;

public class A {
	public static int FOO = 1;

  void usesInner() {
    A.Inner inner = new Inner();
  }

  public class Inner {
  }
}