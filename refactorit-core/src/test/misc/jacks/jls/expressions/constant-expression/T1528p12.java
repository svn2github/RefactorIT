
class T1528p12 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((0xaa & 0xa5) == 0xa0) ? 1 : 0):
            case (((0xaa ^ 0xa5) == 0x0f) ? 2 : 0):
            case (((0xaa | 0xa5) == 0xaf) ? 3 : 0):
        }
    }
}
