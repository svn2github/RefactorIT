
class T15951e2 {
    
        T15951e2() throws RuntimeException, Error {}
        void foo() {
            try {
                new T15951e2() {};
            } catch (RuntimeException re) {
            } catch (Error e) {
            }
        }
    
}
