
class T15182f8 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Float.NEGATIVE_INFINITY + Float.POSITIVE_INFINITY !=
	    Float.NEGATIVE_INFINITY + Float.POSITIVE_INFINITY) ? 1 : 0):
        }
    }
}
