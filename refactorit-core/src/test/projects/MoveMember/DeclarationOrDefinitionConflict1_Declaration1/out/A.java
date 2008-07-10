
abstract class B {
	abstract void f2();
  abstract void f1();
}

public class A extends B {
	public void f1() {
	}

	public void f2() {
	}
}

class C extends B {
	public void f2() {
	}

  void f1() {
    throw new RuntimeException("method f1 is not implemented");
  }
}
