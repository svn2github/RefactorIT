
class T15172f9 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1f / 0f == Float.POSITIVE_INFINITY) ? 1 : 0):
            case ((-1f / 0f == Float.NEGATIVE_INFINITY) ? 2 : 0):
            case ((1f / -0f == Float.NEGATIVE_INFINITY) ? 3 : 0):
            case ((-1f / -0f == Float.POSITIVE_INFINITY) ? 4 : 0):
        }
    }
}
