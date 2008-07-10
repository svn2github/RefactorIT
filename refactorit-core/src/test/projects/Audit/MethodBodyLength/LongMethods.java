package MethodBodyLength;

/**
 * 
 */
public class LongMethods {
/** 
 *
 */

public void test1 () {
	int a = 5;  // 1
	if (a == 5){ // 2
		a = 4; // 3
		a++; // 4
		a++; // 5
		a--; // 6
		a++; // 7
		a--; // 8
		a++; // 9
	} else {
		a--; // 10
		a--; // 11
		a--; // 12
		a--; // 13
		a--; // 14
		a--; // 15
		a--; // 16
		a--; // 17
		a--; // 18
		a--; // 19
		a++; // 20
	}

}

/** 
 *@audit MethodBodyLength
 */
public void test2 () {
	int a = 5;  // 1
	if (a == 5){ // 2
		a = 4; // 3
		a++; // 4
		a++; // 5
		a--; // 6
		a++; // 7
		a--; // 8
		a++; // 9
	} else {
		a--; // 10
		a--; // 11
		a--; // 12
		a--; // 13
		a--; // 14
		a--; // 15
		a--; // 16
		a--; // 17
		a--; // 18
		a--; // 19
		a++; // 20
	}
	return; // 21
}

}
