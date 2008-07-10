
class T1420catch7 {
    void foo() {
        try {
            new Object();
        } catch (Exception e) {
        } catch (Throwable t) {
        }
    }
}
    