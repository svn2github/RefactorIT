
class T1528s2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + 1 == "1") ? 1 : 0):
            case (("" + 1L == "1") ? 2 : 0):
            case (("" + 1f == "1.0") ? 3 : 0):
            case (("" + 1.0 == "1.0") ? 4 : 0):
            case (("" + '1' == "1") ? 5 : 0):
            case (("" + true == "true") ? 6 : 0):
        }
    }
}
