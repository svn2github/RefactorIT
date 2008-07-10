
class T1528nsns3 {
    
        final String s = String.valueOf(0L);
        void foo (int j) {
            switch (j) {
                case 0:
                case ((s == "0") ? 1 : 0):
            }
        }
    
}
