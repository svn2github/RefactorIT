
class T1528csn2 {
    
        final boolean t = true;
        void foo (int j) {
            switch (j) {
                case 0:
                case ((boolean) t ? 1 : 0):
            }
        }
    
}
