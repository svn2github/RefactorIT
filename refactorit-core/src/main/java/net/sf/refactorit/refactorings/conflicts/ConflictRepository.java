/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.conflicts;


import net.sf.refactorit.classmodel.BinMember;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *
 *
 * @author Tonis Vaga
 */
public class ConflictRepository {
  private HashMap singleConflicts = new HashMap();

  private HashMap oneToManyConflicts = new HashMap();
  private HashMap importConflicts = new HashMap();

  private class ConflictKey {
    List with;
    BinMember target;
    ConflictType type;
    // NB! all elements can be null!

    /**
     * @param type type
     */
    public ConflictKey(ConflictType type) {
      this.type = type;
    }

    public ConflictKey(ConflictType type, BinMember conflictTarget) {
      this.target = conflictTarget;
      this.type = type;
    }

    public ConflictKey(ConflictType type, BinMember conflictTarget,
        List conflictWith) {
      this.target = conflictTarget;
      this.with = conflictWith;
      this.type = type;
    }

    public boolean equals(Object object) {
      if (!(object instanceof ConflictKey)) {
        return false;
      }

      ConflictKey dest = (ConflictKey) object;
      return target == dest.target &&
          (with != null ? with.equals(dest.with) : with == dest.with)
          &&
          (type != null ? type.equals(dest.type) : type == dest.type);
    }

    public int hashCode() {
      int result = 0;
      if (type != null) {
        result += type.hashCode();

      }
      if (target != null) {
        result += (target.hashCode() << 1);
      }
      if (with != null) {
        result += with.hashCode() << 2;
      }
      return result;
    }

  }


  public ConflictRepository() {
  }

  public Conflict getConflict(BinMember hasConflict, ConflictType type) {
    return getFromKey(singleConflicts, hasConflict, new ConflictKey(type));
//    Map map = (Map) singleConflicts.get(hasConflict);
//    if (map == null) {
//      return null;
//    }
//    return (Conflict) map.get(new ConflictKey(type));
  }

  public Conflict getConflict(BinMember hasConflict,
      ConflictType type,
      BinMember target) {
    ConflictKey key = new ConflictKey(type, target);

    return getFromKey(singleConflicts, hasConflict, key);
  }

  public Conflict getConflict2(BinMember hasConflict,
      BinMember downMember,
      BinMember target) {
    ConflictKey key = new ConflictKey(null, target,
        Collections.singletonList(downMember));

    return getFromKey(singleConflicts, hasConflict, key);
  }

  private static Conflict getFromKey(final HashMap hashMap,
      final BinMember member, final ConflictKey key) {
    Map map = (Map) hashMap.get(member);
    if (map == null) {
      return null;
    }
    return (Conflict) map.get(key);
  }

  public Conflict getConflict(BinMember hasConflict, List conflictWith,
      BinMember target) {
    ConflictKey key = new ConflictKey(null, target, conflictWith);

    return getFromKey(oneToManyConflicts, hasConflict, key);
  }

  public void addConflict(Conflict conflict, BinMember hasConflict) {
    ConflictKey key = new ConflictKey(conflict.getType());

    putUnderKey(singleConflicts, hasConflict, conflict, key);
//
//    Map memberConflicts=(Map)singleConflicts.get(hasConflict);
//
//    if ( memberConflicts == null ) {
//      memberConflicts=new HashMap(2);
//      singleConflicts.put(hasConflict,memberConflicts);
//    }
//    memberConflicts.put(key,conflict);
  }

  public void addConflict(Conflict conflict, BinMember hasConflict,
      List conflictWith, BinMember target) {
    ConflictKey key = new ConflictKey(null, target, conflictWith);

    putUnderKey(oneToManyConflicts, hasConflict, conflict, key);

//    Map memberConflicts = (Map) oneToManyConflicts.get(hasConflict);
//    if (memberConflicts == null) {
//      memberConflicts = new HashMap(1);
//      oneToManyConflicts.put(hasConflict,memberConflicts);
//    }
//    memberConflicts.put( key,conflict);
  }

  public void addConflict(Conflict conflict, BinMember hasConflict,
      BinMember target) {
    Map memberConflicts = (Map) singleConflicts.get(hasConflict);
    if (memberConflicts == null) {
      memberConflicts = new HashMap(2);
      singleConflicts.put(hasConflict, memberConflicts);
    }
    memberConflicts.put(new ConflictKey(conflict.getType(), target), conflict);
  }

  /**
   * Don't use conflictType for key
   * @param conflict conflict
   * @param hasConflict has conflict
   * @param downMember down member
   * @param target target member
   */
  public void addConflict2(Conflict conflict,
      BinMember hasConflict, BinMember downMember, BinMember target) {

    // hack, use list for key

    ConflictKey key = new ConflictKey(null, target,
        Collections.singletonList(downMember));

    putUnderKey(singleConflicts, hasConflict, conflict, key);
  }

  private static void putUnderKey(final HashMap map, final BinMember member,
      final Conflict conflict, final ConflictKey key) {
    Map memberConflicts = (Map) map.get(member);

    if (memberConflicts == null) {
      memberConflicts = new HashMap(2);
      map.put(member, memberConflicts);
    }

    memberConflicts.put(key, conflict);
  }

  public void clear() {
    oneToManyConflicts.clear();
    singleConflicts.clear();
    importConflicts.clear();
  }

  public void removeConflict(BinMember member, ConflictType type) {
    Map map = (Map) singleConflicts.get(member);
    if (map != null) {
      map.remove(new ConflictKey(type));
    }
  }

  public void removeConflict(BinMember hasConflict, List conflictWith,
      BinMember target) {
    Map memberConflicts = (Map) oneToManyConflicts.get(hasConflict);
    if (memberConflicts != null) {
      memberConflicts.remove(new ConflictKey(null, target, conflictWith));
    }
  }

  /**
   * Shows all import and single conflicts for the member
   * @param member member
   * @return conflicts
   */
  public Collection getConflicts(BinMember member) {
    List result = new ArrayList();
    Map map = (Map) singleConflicts.get(member);
    if (map != null) {
      result.addAll(map.values());
    }

    Set set = (Set) importConflicts.get(member);
    if (set != null) {
      result.addAll(set);
    }

    return result;
  }

  public void addImportConflicts(BinMember member, List conflicts) {
    Set set = (Set) importConflicts.get(member);
    if (set == null) {
      set = new HashSet(conflicts.size());
      importConflicts.put(member, set);
    }

    for (Iterator i = conflicts.iterator(); i.hasNext(); ) {
      Conflict conflict = (Conflict) i.next();
      set.add(conflict);
    }
  }
}
