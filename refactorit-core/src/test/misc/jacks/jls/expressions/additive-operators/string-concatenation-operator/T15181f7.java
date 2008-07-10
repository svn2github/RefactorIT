
class T15181f7 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (("" + 9999999f == "9999999.0") ? 1 : 0):
            case (("" + 10000000f == "1.0E7") ? 2 : 0):
        }
    }
}
