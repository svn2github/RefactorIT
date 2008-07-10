
class T15173d11 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((0d % Double.POSITIVE_INFINITY == 0) ? 1 : 0):
            case ((1/(0d % Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY) ? 2 : 0):
            case ((-0d % Double.POSITIVE_INFINITY == 0) ? 3 : 0):
            case ((1/(-0d % Double.POSITIVE_INFINITY) == Double.NEGATIVE_INFINITY) ? 4 : 0):
            case ((0d % Double.NEGATIVE_INFINITY == 0) ? 5 : 0):
            case ((1/(0d % Double.NEGATIVE_INFINITY) == Double.POSITIVE_INFINITY) ? 6 : 0):
            case ((-0d % Double.NEGATIVE_INFINITY == 0) ? 7 : 0):
            case ((1/(-0d % Double.NEGATIVE_INFINITY) == Double.NEGATIVE_INFINITY) ? 8 : 0):
        }
    }
}
