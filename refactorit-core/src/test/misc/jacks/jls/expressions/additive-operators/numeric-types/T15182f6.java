
class T15182f6 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Float.NEGATIVE_INFINITY + -1f == Float.NEGATIVE_INFINITY) ? 1 : 0):
        }
    }
}
