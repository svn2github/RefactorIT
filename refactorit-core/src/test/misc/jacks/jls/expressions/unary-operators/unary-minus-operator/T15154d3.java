
class T15154d3 {
    void foo(int i) {
        switch (i) {
            case 0:
            case (((1/0.0) != (1/-0.0)) ? 1 : 0):
        }
    }
}
