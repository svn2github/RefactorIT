public class TestRecursive2 {
	double method1(double a) {
		a = a - 1;
		if (a < 0) {
			return a;
		}
		a = a - 1;
		if (a < 0) {
			return a;
		}
		return method1(a);
	}
}
