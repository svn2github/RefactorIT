
class T1528sns4 {
    
        void foo (int j) {
            final String s1 = "1";
            final String s2 = s1;
            switch (j) {
                case 0:
                case ((s2 == "1") ? 1 : 0):
            }
        }
    
}
