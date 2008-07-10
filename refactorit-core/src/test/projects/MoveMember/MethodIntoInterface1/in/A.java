
interface Inter {
}

public class A implements Inter {
	private void f1() throws C, D {
	}
}

class B implements Inter {
}

class C extends Exception {
}

class D extends Exception {
}

