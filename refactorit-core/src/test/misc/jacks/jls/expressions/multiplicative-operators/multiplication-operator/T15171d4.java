
class T15171d4 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((-1.0 * 2.0 < 0.0) ? 1 : 0):
            case ((1.0 * -2.0 < 0.0) ? 2 : 0):
        }
    }
}
