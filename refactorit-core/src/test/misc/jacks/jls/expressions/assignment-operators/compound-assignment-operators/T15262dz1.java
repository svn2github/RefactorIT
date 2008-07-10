
class T15262dz1 {
    
        int i = 1;
        void foo() {
            i %= 0;
            i %= 0L;
        }
    
}
