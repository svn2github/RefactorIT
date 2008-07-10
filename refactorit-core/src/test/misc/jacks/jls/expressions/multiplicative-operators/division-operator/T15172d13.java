
class T15172d13 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1e-170 / 1e170 == 0) ? 1 : 0):
            case ((1/(1e-170 / 1e170) == Double.POSITIVE_INFINITY) ? 2 : 0):
            case ((1e-170 / -1e170 == 0) ? 3 : 0):
            case ((1/(1e-170 / -1e170) == Double.NEGATIVE_INFINITY) ? 4 : 0):
        }
    }
}
