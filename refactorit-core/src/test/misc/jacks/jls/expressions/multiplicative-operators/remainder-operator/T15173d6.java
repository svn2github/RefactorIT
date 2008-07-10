
class T15173d6 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Double.NEGATIVE_INFINITY % -1d != Double.NEGATIVE_INFINITY % -1d) ? 1 : 0):
            case ((Double.POSITIVE_INFINITY % -1d != Double.POSITIVE_INFINITY % -1d) ? 2 : 0):
        }
    }
}
