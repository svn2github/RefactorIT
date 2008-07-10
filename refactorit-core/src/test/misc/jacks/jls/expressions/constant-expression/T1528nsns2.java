
class T1528nsns2 {
    
        String s1 = "1";
        final String s2 = s1;
        void foo (int j) {
            switch (j) {
                case 0:
                case ((s2 == "1") ? 1 : 0):
            }
        }
    
}
