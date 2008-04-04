/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query;

import net.sf.refactorit.classfile.ClassData;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.common.util.Assert;



/**
 * The parent class for every type of indexers.
 */
public class AbstractIndexer extends BinItemVisitor {
  protected static ProgressMonitor.Progress progress;

  /**
   * The Bin(CI)Type we are visiting at the moment
   */
  private BinTypeRef currentType;

  /**
   * The Bin(CI)Type member we currenlty reside in
   * (BinInitializer, BinMethod, BinConstructor)
   */
  private BinItem currentLocation;

  public AbstractIndexer() {
  }

  public AbstractIndexer(final boolean skipSynthetic) {
    super(skipSynthetic);
  }

  public static ProgressMonitor.Progress getProgress() {
    return progress;
  }

  public static void runWithProgress(
      ProgressMonitor.Progress newProgress, Runnable runnable) {
    ProgressMonitor.Progress oldProgress = progress;
    try {
      progress = newProgress;
      runnable.run();
    } finally {
      progress = oldProgress;
    }
  }

  /** null for no progress */
  /*public void setProgress(ProgressMonitor.Progress progress) {
    this.progress = progress;
     }*/

  /** If you call construct.accept() without this, the location and type will not be set */
  public final void invokeAcceptOn(SourceConstruct construct) {
    setCurrentLocation(construct.getParentMember());
    setCurrentType(construct.getOwner());

    construct.accept(this);
  }

  //
  // Track location
  //

  public void visit(BinCIType type) {
    //System.err.println("AbstractIndexer - Visit BinCIType: "
    //    + type.getQualifiedName());
    BinTypeRef curType = getCurrentType();
    BinItem curLocation = null;
    if (curType != null) {
      curLocation = getCurrentLocation();
    }

    setCurrentType(type.getTypeRef());
    setCurrentLocation(null);

    super.visit(type);

    setCurrentType(curType);
    setCurrentLocation(curLocation);
  }

  public void visit(BinConstructor constructor) {
    setCurrentLocation(constructor);

    // Dispatch to super
    super.visit(constructor);

    setCurrentLocation(null);
  }

  public void visit(BinMethod method) {
    setCurrentLocation(method);

    // Dispatch to super
    super.visit(method);

    setCurrentLocation(null);
  }

  public void visit(BinInitializer initializer) {
    setCurrentLocation(initializer);

    // Dispatch to super
    super.visit(initializer);

    setCurrentLocation(null);
  }

  //
  // Accessor methods
  //

  protected BinItem getCurrentLocation() {
    if (this.currentLocation == null) {
      if (this.currentType == null) {
        return null;
      }

      final BinCIType type = this.currentType.getBinCIType();
      if (type.isClass() || type.isEnum()) {
        /*
         * Class that represents default constructor.
         * Example:
         * 	The source code IRL:
         * 	<PRE>
         * 	class Example {
         * 		private String example = "Example";
         * 	}
         * 	</PRE>
         *
         *	This is how the compiler sees it:
         *	  <PRE>
         *	  class Example {
         *	  	String example;
         *
         *		  Example(){
         *			  example = "Example";
         *		  }
         *	  }
         *	  </PRE>
         */
        this.currentLocation = ((BinClass) type)
            .getAccessibleConstructor(type, BinTypeRef.NO_TYPEREFS);

        // FIXME: is it correct? who initializes fields when we don't have
        // default constructor? whatever constructor?
        if (this.currentLocation == null) {
          final BinConstructor[] constructors
              = ((BinClass) type).getConstructors();
          if (Assert.enabled) {
            Assert.must(constructors.length > 0,
                "No constructors found to attach field initialization to: "
                + getCurrentType());
          }

          this.currentLocation = constructors[0];
        }
      } else {
        // NOTE actually javac creates inner static class to init such members
        BinInitializer init
            = new BinInitializer(ClassData.STATIC_INIT_NAME, BinModifier.STATIC);
        init.setParent(type);
        init.setOwner(type.getTypeRef());
        this.currentLocation = init;
      }
    }

    return this.currentLocation;
  }

  public final void setCurrentLocation(BinItem currentLocation) {
    this.currentLocation = currentLocation;
  }

  public BinTypeRef getCurrentType() {
    if (this.currentType == null) {
      if (this.currentLocation != null) {
        if (Assert.enabled) {
          Assert.must(this.currentLocation instanceof BinMember,
              "Location is not a member: " + this.currentLocation.getClass()
              .getName());
        }
        this.currentType = ((BinMember)this.currentLocation).getOwner();
      }
    }

    return this.currentType;
  }

  public final void setCurrentType(BinTypeRef currentType) {
    this.currentType = currentType;
  }
}
