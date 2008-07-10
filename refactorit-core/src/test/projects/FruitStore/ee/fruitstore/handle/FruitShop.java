/*
 * FruitShop.java
 *
 * Created on April 29, 2005, 1:23 PM
 */

package ee.fruitstore.handle;

import ee.fruitstore.fruits.Apple;
import ee.fruitstore.fruits.Fruit;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  Arseni Grigorjev
 */
public class FruitShop {
  
  class FruitShelf {
    
  }
  
  List fruits = new ArrayList(10);
  /** Creates a new instance of FruitShop */
  public FruitShop() {
  }
  
  public void printFruits(){
    for (int i = 0; i < fruits.size(); i++){
      Fruit fruit = (Fruit) fruits.get(i);
      System.out.println(fruit);
    }
  }
  
  public void throwAwayApples(){
    for (int i = 0; i < fruits.size(); i++){
      Fruit fruit = (Fruit) fruits.get(i);
      if (fruit instanceof Apple){
        fruits.remove(i--);
      }
    }
  }
}

class PaperBag {
  private List fruits = new ArrayList();
  
  public void addToBag(Fruit fruit){
    fruits.add(fruit);
  }
}
