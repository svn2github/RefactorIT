public class TestRecursive3 {
	double foo(double a) {
    return a < 0 ? a : /*[*/foo(a - 1)/*]*/; 
	}
}