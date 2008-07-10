
class T1515p4 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((~0xffffffff*2 == 0) ? 1 : 0):
            case ((~(0xffffffff*2) == 1) ? 2 : 0):
        }
    }
}
