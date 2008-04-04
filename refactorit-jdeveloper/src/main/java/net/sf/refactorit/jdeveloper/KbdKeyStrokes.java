/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;


import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.ui.module.BackAction;
import oracle.ide.keyboard.KeyStrokesConstraint;
import oracle.ide.keyboard.KeyStrokesConstraintFactory;
import oracle.ide.keyboard.XMLKeyStrokeContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * This class defines the default global shortcuts for the <code>Set</code>
 * of Command IDs defined by this Addin. This is a utility class used by
 * the Tools | Preferences ... / Accelerators panel.<br>
 *
 * @author Vladislav Vislogubov
 */
public final class KbdKeyStrokes extends XMLKeyStrokeContext {
  public static final String KEY = KbdKeyStrokes.class.getName();

  public KbdKeyStrokes() {
    super(KEY);
  }

  public Set getAllActions(boolean bGlobal) {
    if (bGlobal) {
      Set set = new HashSet();

      ActionRepository rep = ActionRepository.getInstance();
      set.add(rep.getAction(BackAction.KEY));

      Iterator it = rep.getShortcutActions().iterator();

      while (it.hasNext()) {
        set.add(it.next());
      }
//      set.add(IdeAction.find(RefactorItController.REFACTORIT_WHERECAUGHT_CMD_ID));

      return set;
    }

    return Collections.EMPTY_SET;
  }

  public KeyStrokesConstraint getKeyStrokeConstraint() {
    return KeyStrokesConstraintFactory.getConstraint(
        KeyStrokesConstraintFactory.TYPE_ALL
        );
  }
}
