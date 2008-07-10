
class T1528div02 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + (0%0) == "0") ? 1 : 0):
        }
    }
}
