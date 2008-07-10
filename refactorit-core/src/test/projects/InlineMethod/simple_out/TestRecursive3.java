public class TestRecursive3 {
	double foo(double a) {
    return a < 0 ? a : (a - 1 < 0 ? a - 1 : foo(a - 1 - 1)); 
	}
}