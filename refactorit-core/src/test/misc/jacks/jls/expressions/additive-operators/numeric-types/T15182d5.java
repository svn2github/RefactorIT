
class T15182d5 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Double.POSITIVE_INFINITY + 0d == Double.POSITIVE_INFINITY) ? 1 : 0):
        }
    }
}
