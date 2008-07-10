
class T15171f4 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((-1f * 2f < 0f) ? 1 : 0):
            case ((1f * -2f < 0f) ? 2 : 0):
        }
    }
}
