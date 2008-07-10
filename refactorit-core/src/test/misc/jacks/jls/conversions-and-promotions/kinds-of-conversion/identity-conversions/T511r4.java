
class T511r4 {
    
        int[] ia1 = {1, 2};
        int[] ia2 = (int[]) null;
        int[] ia3 = (int[]) ia1;
        int[] ia4 = (int[]) ia2;
        int[] foo() { return new int[0]; }
        int[] ia5 = (int[]) foo();
    
}
