
class T15181f8 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + .001f == "0.001") ? 1 : 0):
            case (("" + .000999998f == "9.99998E-4") ? 2 : 0):
        }
    }
}
