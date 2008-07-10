
class T15172d11 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1.0e160 / 1.0e-160 == Double.POSITIVE_INFINITY) ? 1 : 0):
            case ((1.0e160 / -1.0e-160 == Double.NEGATIVE_INFINITY) ? 2 : 0):
        }
    }
}
