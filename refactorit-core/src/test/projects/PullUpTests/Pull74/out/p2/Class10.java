package p2;
import p1.InterFace1;

public class Class10 implements InterFace1 {

	public final synchronized strictfp void f() {
		throw new RuntimeException("method f is not implemented");
	}
}
