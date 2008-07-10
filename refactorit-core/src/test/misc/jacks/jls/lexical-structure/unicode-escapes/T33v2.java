
class T33v2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (('\uABcD' == '\uabCd') ? 1 : 0):
        }
    }
}
    