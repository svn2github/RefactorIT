package p1;
import java.util.List;
import java.util.ArrayList;
import p2.Class10;

public class Class1 {
}

class Class2 extends Class10 {
// in different packages, import is necessary
	public void func1() {
		List a = new ArrayList();
		Class0.tmpStat = 1;
	}
}
