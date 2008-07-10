
class T15171d9 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1.0e160 * 1.0e160 == Double.POSITIVE_INFINITY) ? 1 : 0):
            case ((1.0e160 * -1.0e160 == Double.NEGATIVE_INFINITY) ? 2 : 0):
        }
    }
}
