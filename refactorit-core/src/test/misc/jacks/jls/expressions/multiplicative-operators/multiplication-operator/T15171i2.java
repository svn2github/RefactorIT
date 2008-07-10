
class T15171i2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((-12300000 == 100000 * -123) ? 1 : 0):
            case ((-50000000 == 500 * -100000) ? 2 : 0):
        }
    }
}
