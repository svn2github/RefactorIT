
class T1528i2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" instanceof String) ? 1 : 0):
        }
    }
}
