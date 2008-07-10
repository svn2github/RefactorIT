
class T15171f9 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1.0e30f * 1.0e30f == Float.POSITIVE_INFINITY) ? 1 : 0):
            case ((1.0e30f * -1.0e30f == Float.NEGATIVE_INFINITY) ? 2 : 0):
        }
    }
}
