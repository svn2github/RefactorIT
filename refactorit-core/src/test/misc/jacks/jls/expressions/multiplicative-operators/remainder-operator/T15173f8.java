
class T15173f8 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1f % Float.NEGATIVE_INFINITY == 1f) ? 1 : 0):
            case ((1f % Float.POSITIVE_INFINITY == 1f) ? 2 : 0):
            case ((-1f % Float.NEGATIVE_INFINITY == -1f) ? 3 : 0):
            case ((-1f % Float.POSITIVE_INFINITY == -1f) ? 4 : 0):
        }
    }
}
