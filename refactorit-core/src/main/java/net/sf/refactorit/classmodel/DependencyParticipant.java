/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import java.util.Collection;
import java.util.Set;


/**
 * @author Anton Safonov
 */
public interface DependencyParticipant extends BinTypeRef {

  void cleanForReuse();

  void addDependableWithoutCheck(BinTypeRef dependable);

  void addDependable(BinTypeRef dependable);

  void removeDependables(Collection dependables);

  Set getDependables();

  Set getAllBinaryDependables();
}
