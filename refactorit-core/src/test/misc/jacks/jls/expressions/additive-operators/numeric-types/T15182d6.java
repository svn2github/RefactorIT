
class T15182d6 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Double.NEGATIVE_INFINITY + -1d == Double.NEGATIVE_INFINITY) ? 1 : 0):
        }
    }
}
