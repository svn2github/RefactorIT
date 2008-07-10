
class T15182assoc2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((1L + 0x70000000) + 0x70000000 == 1L + (0x70000000 + 0x70000000)) ? 1 : 0):
        }
    }
}
