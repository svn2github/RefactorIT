
class T1515p5 {
    void foo(int i) {
        switch (i) {
            case 0:
            case ((!true + "" == "false") ? 1 : 0):
        }
    }
}
