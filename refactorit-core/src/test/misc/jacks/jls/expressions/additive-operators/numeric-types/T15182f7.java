
class T15182f7 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Float.NEGATIVE_INFINITY + Float.NEGATIVE_INFINITY ==
	    Float.NEGATIVE_INFINITY) ? 1 : 0):
        }
    }
}
