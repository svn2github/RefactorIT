
class T15172i2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((0x80000000 / -1 == 0x80000000) ? 1 : 0):
        }
    }
}
