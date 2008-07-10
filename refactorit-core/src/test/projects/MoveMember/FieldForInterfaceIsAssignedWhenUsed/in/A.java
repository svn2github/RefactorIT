
interface Inter {
}

public class A {
	public static int a = 1;
}

class B {
	{
		A.a = 2;
	}
}
