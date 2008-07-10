
class T1528s12 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + (0xaa & 0xa5) == "160") ? 1 : 0):
            case (("" + (0xaa ^ 0xa5) == "15") ? 2 : 0):
            case (("" + (0xaa | 0xa5) == "175") ? 3 : 0):
        }
    }
}
