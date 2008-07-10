
class T1528null6 {
    
        void foo(int i) {
            final String s = null;
            switch (i) {
                case 0:
                case (("" != s) ? 1 : 0):
            }
        }
    
}
