
class T1528sns3 {
    
        void foo (int j) {
            final String s = "1" + (int) 2.0D;
            switch (j) {
                case 0:
                case ((s == "12") ? 1 : 0):
            }
        }
    
}
