
class T15182assoc1 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((1 + 2) + 3 == 1 + (2 + 3)) ? 1 : 0):
        }
    }
}
