
class T1528s11 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("1" == "1") ? 1 : 0):
            case (("1" != "2") ? 2 : 0):
        }
    }
}
