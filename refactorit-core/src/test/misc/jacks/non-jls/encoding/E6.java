
class E6 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (('é' == '\u00E9') ? 1 : 0):
        }
    }
}
