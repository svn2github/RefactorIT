public class TestRecursive {
	double method1(double a) {
		double result;
		if (a < 0) {
			result = a;
		} else {
			double result1;
			if (a - 1 < 0) {
				result1 = a - 1;
			} else {
				result1 = method1(a - 1 - 1);
			}
			result = result1;
		}
		return result;
	}
}