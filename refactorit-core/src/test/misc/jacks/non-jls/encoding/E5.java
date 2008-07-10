
class E5 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (('�' == '\uFFFD') ? 1 : 0):
        }
    }
}
