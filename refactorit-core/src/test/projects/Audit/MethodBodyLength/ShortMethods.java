package MethodBodyLength;

/**
 */
public class ShortMethods {
/* */
private int apple = 5;
private int peach = 4;
public int fire = 2;


/**
 *
 */
public int getApple(){
	return apple;
}

/**
 *
 */
public int getRedApple(){

	if (apple == 5) {
		return apple;
	}
}

/**
 * @audit MethodBodyLength
 */
public int getPeach(){

	return apple;
}

/**
 * @audit MethodBodyLength
 */
public void nothingDo(){

}

/**
 * @audit MethodBodyLength
 */
public int getFire(int fire){

	return fire;
}

/**
 *
 */
public void setFire(int fire){
	this.fire = fire;
}

/**
 * @audit MethodBodyLength
 */
public void oneStatement(){
	return;
}

}
