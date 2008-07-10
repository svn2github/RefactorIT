package p1;
import java.util.List;
import java.util.ArrayList;

public class Class1 {
}

class Class2 extends Class3 {
// in the same package, no import is necessary
	public void func1() {
		List a = new ArrayList();
		Class0.tmpStat = 1;
	}
}

class Class0 {
	public Class0() { }
	static public int tmpStat;
}

