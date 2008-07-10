package p1;

public class Class1 {
	int a;
}

class Class2 extends Class1 {

	class Inner {
		{
			a = 1;
		}
	}
}
