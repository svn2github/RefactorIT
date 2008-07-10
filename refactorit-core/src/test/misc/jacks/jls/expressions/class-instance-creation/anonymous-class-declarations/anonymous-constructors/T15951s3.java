
class T15951s3 {
    
        T15951s3(int i, byte b) {}
        T15951s3(byte b, int i) {}
        byte b;
        Object o = new T15951s3(b, b) {};
    
}
