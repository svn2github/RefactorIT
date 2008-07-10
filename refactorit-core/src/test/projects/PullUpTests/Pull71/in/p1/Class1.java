package p1;
import p2.Interface10;

public class Class1 {
}

class Class2 implements Interface10 {
	public int a = Class3.b;
}

class Class3 {
	final static int b = 1;
}
