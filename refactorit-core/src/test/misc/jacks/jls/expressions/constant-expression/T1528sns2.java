
class T1528sns2 {
    
        void foo (int j) {
            final String s = "1" + "2";
            switch (j) {
                case 0:
                case ((s == "12") ? 1 : 0):
            }
        }
    
}
