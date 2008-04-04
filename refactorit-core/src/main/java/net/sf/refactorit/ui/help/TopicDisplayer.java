/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.help;

import net.sf.refactorit.ui.module.IdeWindowContext;


public interface TopicDisplayer {
  void displayTopic(IdeWindowContext context, String topicId);
}
