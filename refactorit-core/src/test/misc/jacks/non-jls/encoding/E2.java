
class E2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (('ä' == '\u00e4') ? 1 : 0):
        }
    }
}
