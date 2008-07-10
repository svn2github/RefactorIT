
class T15171d8 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Double.NEGATIVE_INFINITY * Double.POSITIVE_INFINITY == Double.NEGATIVE_INFINITY) ? 1 : 0):
            case ((Double.POSITIVE_INFINITY * Double.NEGATIVE_INFINITY == Double.NEGATIVE_INFINITY) ? 2 : 0):
        }
    }
}
