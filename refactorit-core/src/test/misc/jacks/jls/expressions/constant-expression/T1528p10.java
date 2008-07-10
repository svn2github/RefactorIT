
class T1528p10 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((1 < 2) ? 1 : 0):
            case ((2 <= 2) ? 2 : 0):
            case ((2 > 1) ? 3 : 0):
            case ((1 >= 1) ? 4 : 0):
        }
    }
}
