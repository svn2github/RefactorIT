
class T15181d13 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + 1e-323 == "9.9E-324") ? 1 : 0):
            case (("" + 1e-322 == "9.9E-323") ? 2 : 0):
        }
    }
}
