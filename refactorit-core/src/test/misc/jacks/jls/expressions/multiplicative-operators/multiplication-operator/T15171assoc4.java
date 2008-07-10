
class T15171assoc4 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1e308 * 100.0 * .01 == (1e308 * 100.0) * .01) ? 1 : 0):
        }
    }
}
