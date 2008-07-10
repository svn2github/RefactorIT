
class T15172d10 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((0d / 1d == 0) ? 1 : 0):
            case ((1/(0d / 1d) == Double.POSITIVE_INFINITY) ? 2 : 0):
            case ((-0d / 1d == 0) ? 3 : 0):
            case ((1/(-0d / 1d) == Double.NEGATIVE_INFINITY) ? 4 : 0):
            case ((0d / -1d == 0) ? 5 : 0):
            case ((1/(0d / -1d) == Double.NEGATIVE_INFINITY) ? 6 : 0):
            case ((-0d / -1d == 0) ? 7 : 0):
            case ((1/(-0d / -1d) == Double.POSITIVE_INFINITY) ? 8 : 0):
        }
    }
}
