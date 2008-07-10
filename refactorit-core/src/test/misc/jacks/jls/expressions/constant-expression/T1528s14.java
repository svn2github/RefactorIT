
class T1528s14 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + (true && true) == "true") ? 1 : 0):
            case (("" + (true || false) == "true") ? 2 : 0):
        }
    }
}
