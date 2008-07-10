package p1;

interface InterFace1 {
	int b = 1;
// move b also
	int a = b;
}

public class Class1 {
}

class Class2 implements InterFace1 {
}
