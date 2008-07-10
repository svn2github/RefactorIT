package a.b;

import a.*;

public class PackageB {
  public PackageB() {
    new PackageA().testValue();
  }
}
