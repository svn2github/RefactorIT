
class T15181d7 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + 12.0 == "12.0") ? 1 : 0):
            case (("" + Double.NaN == "NaN") ? 2 : 0):
        }
    }
}
