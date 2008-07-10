
class T15172d9 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1d / 0d == Double.POSITIVE_INFINITY) ? 1 : 0):
            case ((-1d / 0d == Double.NEGATIVE_INFINITY) ? 2 : 0):
            case ((1d / -0d == Double.NEGATIVE_INFINITY) ? 3 : 0):
            case ((-1d / -0d == Double.POSITIVE_INFINITY) ? 4 : 0):
        }
    }
}
