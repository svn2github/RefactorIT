
class T15182d7 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Double.NEGATIVE_INFINITY + Double.NEGATIVE_INFINITY ==
	    Double.NEGATIVE_INFINITY) ? 1 : 0):
        }
    }
}
