
class T15181d6 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + Double.NaN == "NaN") ? 1 : 0):
            case (("" + 12.0 == "12.0") ? 2 : 0):
        }
    }
}
