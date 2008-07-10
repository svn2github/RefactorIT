
class T1528sns1 {
    
        void foo (int j) {
            final String s = "1";
            switch (j) {
                case 0:
                case ((s == "1") ? 1 : 0):
            }
        }
    
}
