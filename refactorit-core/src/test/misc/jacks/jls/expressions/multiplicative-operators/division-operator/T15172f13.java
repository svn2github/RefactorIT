
class T15172f13 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1e-30f / 1e30f == 0) ? 1 : 0):
            case ((1/(1e-30f / 1e30f) == Float.POSITIVE_INFINITY) ? 2 : 0):
            case ((1e-30f / -1e30f == 0) ? 3 : 0):
            case ((1/(1e-30f / -1e30f) == Float.NEGATIVE_INFINITY) ? 4 : 0):
        }
    }
}
