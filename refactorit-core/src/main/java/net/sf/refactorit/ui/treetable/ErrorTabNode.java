/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable;

import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.source.UserFriendlyError;



/**
 * Error Tab TreeTable node.
 *
 * @author Anton Safonov
 * @author Vladislav Vislogubov
 */
public final class ErrorTabNode extends BinTreeTableNode {
  public ErrorTabNode(final UserFriendlyError err) {
    super(err, true);
    int line = ((UserFriendlyError) getBin()).getLine();
    if (line < 0) {line = 0;
    }
    setLine(line);
    setSourceHolder(((UserFriendlyError) getBin()).getCompilationUnit());
  }

  public final String getDisplayName() {
    if (this.name == null) {
      if (((UserFriendlyError) getBin()).getLine() >= 0) {
        this.name = ((UserFriendlyError) getBin()).getLine() + ":"
            + ((UserFriendlyError) getBin()).getColumn() + " "
            + ((UserFriendlyError) getBin()).getDescription();
      } else { // let's try to extract line number at the end of description
        this.name = ((UserFriendlyError) getBin()).getDescription();
        try {
          final int space = this.name.lastIndexOf(' ');
          final int collon = this.name.lastIndexOf(':');
          final int len = collon - space - 1;
          if (len > 0 && len < 5) {
            final int line = new Integer(this.name.substring(space + 1, collon))
                .intValue();
            this.name = line + this.name.substring(collon) + " "
                + this.name.substring(0, space);
            setLine(line);
          }
        } catch (Exception e) {
          // forget about the try
        }
      }

      if (this.name == null) {
        this.name = ((UserFriendlyError) getBin()).getDescription();
      }
    }

    return this.name;
  }

  public final String toClipboardFormat(final boolean preserveHtml,
      final BinTreeTableModel model) {
    String source = getLineSource();

    if (source == null || source.length() == 0) {
      return getDisplayName();
    } else {
      source = source.trim();
      if (!preserveHtml) {
        source = StringUtil.removeHtml(source);
      }
      return getDisplayName() + " (" + source.trim() + ")";
    }
  }
}
