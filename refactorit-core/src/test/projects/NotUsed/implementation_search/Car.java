/*
 * Car.java
 *
 * Created on July 29, 2002, 3:44 PM
 */

/**
 *
 * @author  gbridges
 */
public class Car extends Vehicle
{
   
    /** Creates a new instance of Car */
    public Car()
    {
    }
    
    protected void move()
    {
        System.err.println("Moving");
    }
    
    public void start()
    {
        System.err.println("Starting");
        move();
    }
    
    public static void main(String args[])
    {
        Car c = new Car();
        c.start();
    }
}
