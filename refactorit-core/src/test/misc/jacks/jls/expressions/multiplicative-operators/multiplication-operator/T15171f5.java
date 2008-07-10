
class T15171f5 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((Float.POSITIVE_INFINITY * 0f != Float.POSITIVE_INFINITY * 0f) ? 1 : 0):
        }
    }
}
