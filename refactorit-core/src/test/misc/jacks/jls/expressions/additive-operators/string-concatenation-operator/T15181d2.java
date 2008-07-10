
class T15181d2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + Double.NaN == "NaN") ? 1 : 0):
            case (("" + (-Double.NaN) == "NaN") ? 2 : 0):
        }
    }
}
