
class T15173d8 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1d % Double.NEGATIVE_INFINITY == 1d) ? 1 : 0):
            case ((1d % Double.POSITIVE_INFINITY == 1d) ? 2 : 0):
            case ((-1d % Double.NEGATIVE_INFINITY == -1d) ? 3 : 0):
            case ((-1d % Double.POSITIVE_INFINITY == -1d) ? 4 : 0):
        }
    }
}
