
class T15172assoc1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((10 / 3 / 2 == 1) ? 1 : 0):
            case ((10 / (3 / 2) == 10) ? 2 : 0):
        }
    }
}
