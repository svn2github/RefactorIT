public class TestRecursive {
	double method1(double a) {
		double result;
		if (a < 0) {
			result = a;
		} else {
			result = /*[*/method1(a - 1)/*]*/;
		}
		return result;
	}
}