
class T15171f7 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Float.NEGATIVE_INFINITY * Float.NEGATIVE_INFINITY == Float.POSITIVE_INFINITY) ? 1 : 0):
            case ((Float.POSITIVE_INFINITY * Float.POSITIVE_INFINITY == Float.POSITIVE_INFINITY) ? 2 : 0):
        }
    }
}
