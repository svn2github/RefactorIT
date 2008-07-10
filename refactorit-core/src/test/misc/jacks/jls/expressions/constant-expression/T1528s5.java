
class T1528s5 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + +2 == "2") ? 1 : 0):
            case (("" + -0x80000000 == "-2147483648") ? 2 : 0):
            case (("" + ~0 == "-1") ? 3 : 0):
            case (("" + !false == "true") ? 4 : 0):
        }
    }
}
