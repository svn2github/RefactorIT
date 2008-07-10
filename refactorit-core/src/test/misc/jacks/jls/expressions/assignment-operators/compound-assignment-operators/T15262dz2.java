
class T15262dz2 {
    
        long l = 1;
        void foo() {
            l %= 0;
            l %= 0L;
        }
    
}
