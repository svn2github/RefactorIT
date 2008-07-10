
class T8323lfi10 {
    
        final int i = j; // j is static
        static final int j = 1;
        void foo(int n) {
            switch (n) {
                case 0:
                case i: // i == 1
            }
        }
    
}
