
class T1528nsns1 {
    
        String s = "1";
        void foo (int j) {
            switch (j) {
                case 0:
                case ((s == "1") ? 1 : 0):
            }
        }
    
}
