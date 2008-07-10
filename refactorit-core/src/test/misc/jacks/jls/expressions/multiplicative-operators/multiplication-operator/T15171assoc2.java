
class T15171assoc2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((1L * 0x70000000) * 20 == 1L * (0x70000000 * 20)) ? 1 : 0):
        }
    }
}
