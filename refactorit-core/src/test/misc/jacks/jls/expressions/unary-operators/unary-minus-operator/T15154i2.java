
class T15154i2 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((-0xffffffff == ~0xffffffff+1) ? 1 : 0):
        }
    }
}
