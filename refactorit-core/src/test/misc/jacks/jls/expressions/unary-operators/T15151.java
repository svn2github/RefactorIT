
class T15151 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((-~0xffffffff == 0) ? 1 : 0):
            case ((~-0xffffffff == 0xfffffffe) ? 2 : 0):
        }
    }
}
