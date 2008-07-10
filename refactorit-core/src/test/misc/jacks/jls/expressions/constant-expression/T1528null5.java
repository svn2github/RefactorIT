
class T1528null5 {
    
        static final String s = null;
        void foo(int i) {
            switch (i) {
                case 0:
                case (("" != s) ? 1 : 0):
            }
        }
    
}
