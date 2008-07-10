
class T1515p3 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((-0x80000000/2 == 0xc0000000) ? 1 : 0):
            case ((-(0x80000000/2) == 0x40000000) ? 2 : 0):
        }
    }
}
