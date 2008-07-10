
class T15173i4 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((0x80000000 % -1 == 0) ? 1 : 0):
        }
    }
}
