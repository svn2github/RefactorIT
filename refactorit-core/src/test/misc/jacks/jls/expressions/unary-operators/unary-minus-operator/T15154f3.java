
class T15154f3 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((1/0.0f) != (1/-0.0f)) ? 1 : 0):
        }
    }
}
