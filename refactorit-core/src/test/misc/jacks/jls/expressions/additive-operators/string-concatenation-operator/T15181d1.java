
class T15181d1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + 1e-315 == "1.0E-315") ? 1 : 0):
        }
    }
}
