
class T1528s10 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + (1 < 2) == "true") ? 1 : 0):
            case (("" + (2 <= 2) == "true") ? 2 : 0):
            case (("" + (2 > 1) == "true") ? 3 : 0):
            case (("" + (1 >= 1) == "true") ? 4 : 0):
        }
    }
}
