package p1;

public class Class1 {
}

class Class2 extends Class1 {
	private int a;

	class Inner {
		{
			a = 1;
		}
	}
}
