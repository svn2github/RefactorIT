
interface Inter {
  void f1() throws C, D;
}

public class A implements Inter {
	public void f1() throws C, D {
	}
}

class B implements Inter {

  public void f1() throws C, D {
    throw new RuntimeException("method f1 is not implemented");
  }
}

class C extends Exception {
}

class D extends Exception {
}

