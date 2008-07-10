
class T15154d2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((-Double.POSITIVE_INFINITY == Double.NEGATIVE_INFINITY) ? 1 : 0):
        }
    }
}
