
class T15173f6 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Float.NEGATIVE_INFINITY % -1f != Float.NEGATIVE_INFINITY % -1f) ? 1 : 0):
            case ((Float.POSITIVE_INFINITY % -1f != Float.POSITIVE_INFINITY % -1f) ? 2 : 0):
        }
    }
}
