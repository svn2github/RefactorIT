
class T1528p9 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((2 << 1 == 4) ? 1 : 0):
            case ((2 >> 1 == 1) ? 2 : 0):
            case ((-2 >>> 1 == 0x7fffffff) ? 3 : 0):
        }
    }
}
