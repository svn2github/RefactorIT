
class T1528s7 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + "test" + "ing" == "testing") ? 1 : 0):
            case (("a" + "b" == "\u0061b") ? 2 : 0):
            case (("" + ("1" + '2' + 3 +4L) == "1234") ? 3 : 0):
        }
    }
}
