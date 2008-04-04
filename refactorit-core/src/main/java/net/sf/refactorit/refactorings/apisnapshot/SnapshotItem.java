/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.apisnapshot;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.source.format.BinModifierFormatter;
import net.sf.refactorit.ui.tree.UITreeNode;

import java.util.HashMap;


/** A class, package, method or constructor. Can serialize/deserialize itself. */
public class SnapshotItem {
  private Object binItem;

  static final String PACKAGE = BinPackage.getStaticMemberType();
  static final String CLASS = BinClass.getStaticMemberType();
  static final String METHOD = BinMethod.getStaticMemberType();
  static final String CONSTRUCTOR = BinConstructor.getStaticMemberType();
  static final String FIELD = BinField.getStaticMemberType();

  static final String COLUMN_SEPARATOR = "\t";

  public static final String SEPARATOR_BEFORE_HUMAN_READABLE_PART = "|%";

  private String type;
  private String description;
  private String owner;
  private String ownerOfOwner;
  private String access;

  private int precalculatedHashCode;

  private static HashMap iconsForType = new HashMap();
  static {
    iconsForType.put(BinPackage.getStaticMemberType(),
        new Integer(UITreeNode.NODE_PACKAGE));
    iconsForType.put(BinClass.getStaticMemberType(),
        new Integer(UITreeNode.NODE_CLASS));
    iconsForType.put(BinMethod.getStaticMemberType(),
        new Integer(UITreeNode.NODE_TYPE_METHOD));
    iconsForType.put(BinConstructor.getStaticMemberType(),
        new Integer(UITreeNode.NODE_TYPE_CNSTOR));
    iconsForType.put(BinField.getStaticMemberType(),
        new Integer(UITreeNode.NODE_TYPE_FIELD));
  }

  SnapshotItem(String serializedForm) {
    String[] columns = SnapshotStringUtil.split(serializedForm,
        COLUMN_SEPARATOR, 5);

    this.type = columns[0];
    this.description = columns[1];
    this.owner = columns[2];
    this.ownerOfOwner = columns[3];
    this.access = columns[4];

    precalculateHashCode();
  }

  SnapshotItem(BinItem item, boolean storeBinItem) {
    if (storeBinItem) {
      this.binItem = item;
    }

    if (item instanceof BinCIType) {
      init((BinCIType) item);
    } else if (item instanceof BinPackage) {
      init((BinPackage) item);
    } else if (item instanceof BinMethod) {
      if (item instanceof BinConstructor) {
        init((BinConstructor) item);
      } else {
        init((BinMethod) item);
      }
    } else if (item instanceof BinField) {
      init((BinField) item);
    } else {
      throw new IllegalArgumentException(
          "Snapshot can't be created for item of type " + item.getClass());
    }

    precalculateHashCode();
  }

  SnapshotItem(String type,
      String description,
      String descriptionForHumans,
      String owner,
      String ownerForHumans,
      String ownerOfOwner,
      String ownerOfOwnerForHumans,
      String access) {
    init(
        type,
        description, descriptionForHumans,
        owner, ownerForHumans,
        ownerOfOwner, ownerOfOwnerForHumans, access
        );

    precalculateHashCode();
  }

  private void init(String type,
      String description,
      String descriptionForHumans,
      String owner,
      String ownerForHumans,
      String ownerOfOwner,
      String ownerOfOwnerForHumans,
      String access) {
    this.type = type;
    this.description = description + SEPARATOR_BEFORE_HUMAN_READABLE_PART
        + descriptionForHumans;
    this.owner = owner + SEPARATOR_BEFORE_HUMAN_READABLE_PART + ownerForHumans;
    this.ownerOfOwner = ownerOfOwner + SEPARATOR_BEFORE_HUMAN_READABLE_PART
        + ownerOfOwnerForHumans;
    if (access.length() == 0) {
      access = "package private";
    }
    this.access = access;
  }

  private void init(BinCIType type) {
    init(
        CLASS,
        type.getQualifiedName(), getHumanReadableNameFor(type),
        type.getPackage().getQualifiedName(),
        getHumanReadableNameFor(type.getPackage()),
        "", "", new BinModifierFormatter(type.getModifiers()).print()
        );
  }

  private void init(BinPackage binPackage) {
    init(
        PACKAGE,
        binPackage.getQualifiedName(), getHumanReadableNameFor(binPackage),
        "", "",
        "", "", ""
        );
  }

  private void init(BinMethod method) {
    init(
        METHOD,
        method.toString(), getHumanReadableNameFor(method),
        method.getOwner().getBinCIType().getQualifiedName(),
        getHumanReadableNameFor(method.getOwner().getBinCIType()),
        method.getOwner().getPackage().getQualifiedName(),
        getHumanReadableNameFor(method.getOwner().getPackage()),
        new BinModifierFormatter(method.getModifiers()).print()
        );
  }

  private void init(BinConstructor constr) {
    init(
        CONSTRUCTOR,
        constr.toString(), getHumanReadableNameFor(constr),
        constr.getOwner().getBinCIType().getQualifiedName(),
        getHumanReadableNameFor(constr.getOwner().getBinCIType()),
        constr.getOwner().getPackage().getQualifiedName(),
        getHumanReadableNameFor(constr.getOwner().getPackage()),
        new BinModifierFormatter(constr.getModifiers()).print()
        );
  }

  private void init(BinField field) {
    init(
        FIELD,
        field.toString(), getHumanReadableNameFor(field),
        field.getOwner().getBinCIType().getQualifiedName(),
        getHumanReadableNameFor(field.getOwner().getBinCIType()),
        field.getOwner().getPackage().getQualifiedName(),
        getHumanReadableNameFor(field.getOwner().getPackage()),
        new BinModifierFormatter(field.getModifiers()).print()
        );
  }

  private static String getHumanReadableNameFor(BinPackage aPackage) {
    return aPackage.getQualifiedName();
  }

  private static String getHumanReadableNameFor(BinCIType type) {
    return new BinModifierFormatter(type.getModifiers()).print() + " " +
        (type.isInterface() ? "interface"
        : "class") + " " + type.getNameWithAllOwners();
  }

  private static String getHumanReadableNameFor(BinField field) {
    return new BinModifierFormatter(field.getModifiers()).print() + " " +
        field.getTypeRef().getQualifiedName() + " " + field.getName();
  }

  private static String getHumanReadableNameFor(BinMethod method) {
    StringBuffer result = new StringBuffer();

    result.append(new BinModifierFormatter(method.getModifiers()).print());
    result.append(" ");
    result.append(method.getReturnType().getBinType().getQualifiedName());
    result.append(" ");
    result.append(method.getName());
    result.append("(");

    for (int i = 0; i < method.getParameters().length; i++) {
      if (i != 0) {
        result.append(", ");

      }
      BinParameter parameter = method.getParameters()[i];
      result.append(parameter.getTypeRef().getBinType().getQualifiedName());
      result.append(" ");
      result.append(parameter.getName());
    }

    result.append(")");

    if (method.getThrows().length > 0) {
      result.append(" throws ");

      for (int i = 0; i < method.getThrows().length; i++) {
        if (i != 0) {
          result.append(", ");

        }
        BinMethod.Throws t = method.getThrows()[i];
        result.append(t.getException().getName());
      }
    }

    return result.toString();
  }

  static boolean canCreateSnapshotOf(BinItem item) {
    return
        item instanceof BinMethod || item instanceof BinConstructor ||
        item instanceof BinPackage || item instanceof BinCIType
        || item instanceof BinField;
  }

  static boolean shouldVisitContentsOf(BinItem item) {
    if (item instanceof BinMethod || item instanceof BinConstructor
        || item instanceof BinInitializer) {
      return false;
    } else {
      return true;
    }
  }

  public boolean isPackage() {
    return PACKAGE.equals(this.type);
  }

  public boolean isMethod() {
    return METHOD.equals(this.type);
  }

  public boolean isConstructor() {
    return CONSTRUCTOR.equals(this.type);
  }

  public boolean isField() {
    return FIELD.equals(this.type);
  }

  public boolean isClass() {
    return CLASS.equals(this.type);
  }

  public String getDescription() {
    return this.description;
  }

  public String getOwner() {
    return this.owner;
  }

  public String getOwnerOfOwner() {
    return this.ownerOfOwner;
  }

  public String getAccess() {
    return this.access;
  }

  public Integer getType() {
    return (Integer) iconsForType.get(type);
  }

  /**
   * @param snapshot snapshots
   * @param  startPos  used for a speed optimization; shows a position in the array
   *                   near witch this item might probably be.
   * @return true if contained
   */
  boolean containedIn(SnapshotItem[] snapshot, int startPos) {
    // Search goes simultaneously in two directions from the suggested startPos.
    // The reason is that the snapshot item is more likely to be near the suggested position
    // than away from it so searching is significantly faster this way.

    // This prevents ArrayIndexOutOfBoundsException:
    if (snapshot.length == 0) {
      return false;
    }

    int backwardLookupPos = startPos;

    if (backwardLookupPos >= snapshot.length || backwardLookupPos < 0) {
      backwardLookupPos = 0;
    }

    int forwardLookupPos = backwardLookupPos + 1;
    if (forwardLookupPos >= snapshot.length) {
      forwardLookupPos = 0;
    }

    for (int i = 0; i < snapshot.length / 2 + 1; i++) {
      // Search in the forward direction...
      if (this.equals(snapshot[forwardLookupPos])) {
        return true;
      }

      forwardLookupPos++;
      if (forwardLookupPos >= snapshot.length) {
        forwardLookupPos = 0;
      }

      // ... and in backwards direction.
      if (this.equals(snapshot[backwardLookupPos])) {
        return true;
      }

      backwardLookupPos--;
      if (backwardLookupPos < 0) {
        backwardLookupPos = snapshot.length - 1;
      }
    }

    return false;
  }

  public boolean equals(SnapshotItem otherPath) {
    // For speed only; speeds up API DIFF about 10 times.
    if (this.precalculatedHashCode != otherPath.precalculatedHashCode) {
      return false;
    }

    // We don't need to test equality of owners because owners are already included in description
    return this.type.equals(otherPath.type)
        && this.description.equals(otherPath.description);
  }

  private void precalculateHashCode() {
    this.precalculatedHashCode = this.description.hashCode();
  }

  public int hashCode() {
    return this.precalculatedHashCode;
  }

  public boolean equals(Object anotherObject) {
    if (anotherObject instanceof SnapshotItem) {
      return equals((SnapshotItem) anotherObject);
    } else {
      return false;
    }
  }

  /**
   * @return bin item
   * @throws RuntimeException when SnapshotItem was not constructed with a bin item
   */
  public Object getBinItem() {
    if (this.binItem == null) {
      throw new RuntimeException(
          "This item was not created with a BinItem (see SnapshotBuilder).");
    }

    return this.binItem;
  }

  public String toString() {
    return getSerializedForm();
  }

  String getSerializedForm() {
    return this.type + COLUMN_SEPARATOR + this.description + COLUMN_SEPARATOR +
        this.owner + COLUMN_SEPARATOR + this.ownerOfOwner +
        COLUMN_SEPARATOR + this.access;
  }

  static SnapshotItem[] createItems(String[] itemLines) {
    SnapshotItem[] result = new SnapshotItem[itemLines.length];

    for (int i = 0; i < itemLines.length; i++) {
      result[i] = new SnapshotItem(itemLines[i]);
    }

    return result;
  }

  public static String createTitle(String apiDiffLine) {
    return beautifyArrayNames(getHumanReadableDescription(apiDiffLine));
  }

  // FIXME move to BinArrayType
  /**
   *  HACK. "[Ljava.lang.String;" => "java.lang.String[]". Supports muilti-dim arrays
   * @param description array type name
   * @return array type name in human form
   */
  private static String beautifyArrayNames(String description) {
    description = StringUtil.replace(description, ";", "");
    description = StringUtil.replace(description, "[L", "[");

    while (description.indexOf("[") >= 0) {
      int p = description.indexOf("[");

      description = description.substring(0, p) + description.substring(p + 1);

      int firstFreePosition = description.indexOf(" ", p);

      // First insert "*" instead of "[]". This prevents forever-looping. Later "*" is replaced.
      description = description.substring(0,
          firstFreePosition) + "*" + description.substring(firstFreePosition);
    }

    return StringUtil.replace(description, "*", "[]");
  }

  private static String getHumanReadableDescription(String s) {
    if (s.indexOf(SnapshotItem.SEPARATOR_BEFORE_HUMAN_READABLE_PART) >= 0) {
      return s.substring(
          s.indexOf(SnapshotItem.SEPARATOR_BEFORE_HUMAN_READABLE_PART) +
          SnapshotItem.SEPARATOR_BEFORE_HUMAN_READABLE_PART.length());
    } else {
      return s;
    }
  }
}
