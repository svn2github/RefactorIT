
class T15181d10 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + .001 == "0.001") ? 1 : 0):
            case (("" + .0009999999999999998 == "9.999999999999998E-4") ? 2 : 0):
        }
    }
}
