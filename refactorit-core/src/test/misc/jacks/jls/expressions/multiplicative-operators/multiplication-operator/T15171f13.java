
class T15171f13 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((0x800001 * 3f == 0x1800004) ? 1 : 0):
            case ((0x800001 * 5f == 0x2800004) ? 2 : 0):
            case ((0x800003 * 5f == 0x2800010) ? 3 : 0):
            case ((0x800002 * 5f == 0x2800008) ? 4 : 0):
        }
    }
}
