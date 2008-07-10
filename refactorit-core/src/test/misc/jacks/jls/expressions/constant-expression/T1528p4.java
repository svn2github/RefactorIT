
class T1528p4 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((+2 == 2) ? 1 : 0):
            case ((-0x80000000 == 0x80000000) ? 2 : 0):
            case ((~0 == 0xffffffff) ? 3 : 0):
            case ((!false) ? 4 : 0):
        }
    }
}
