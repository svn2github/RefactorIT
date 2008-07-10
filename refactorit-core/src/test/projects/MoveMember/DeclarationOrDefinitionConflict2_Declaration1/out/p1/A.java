package p1;

public class A extends B {
	protected void f1() {
	}
}

class C extends B {

  protected void f1() {
    throw new RuntimeException("method f1 is not implemented");
  }
}
