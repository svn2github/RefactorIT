
class T15182d8 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Double.NEGATIVE_INFINITY + Double.POSITIVE_INFINITY !=
	    Double.NEGATIVE_INFINITY + Double.POSITIVE_INFINITY) ? 1 : 0):
        }
    }
}
