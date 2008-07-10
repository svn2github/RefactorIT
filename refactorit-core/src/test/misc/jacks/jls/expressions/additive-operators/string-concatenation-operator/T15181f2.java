
class T15181f2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + Float.NaN == "NaN") ? 1 : 0):
            case (("" + (-Float.NaN) == "NaN") ? 2 : 0):
        }
    }
}
