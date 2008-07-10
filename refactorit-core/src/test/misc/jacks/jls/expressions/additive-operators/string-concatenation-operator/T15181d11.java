
class T15181d11 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + 9.999999999999999e22 == "1.0E23") ? 1 : 0):
        }
    }
}
