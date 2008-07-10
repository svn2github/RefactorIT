
class T15154f2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((-Float.POSITIVE_INFINITY == Float.NEGATIVE_INFINITY) ? 1 : 0):
        }
    }
}
