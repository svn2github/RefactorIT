
class T15951e1 {
    
        T15951e1() throws InterruptedException {}
        void foo() {
            try {
                new T15951e1() {};
            } catch (InterruptedException e) {
            }
        }
    
}
