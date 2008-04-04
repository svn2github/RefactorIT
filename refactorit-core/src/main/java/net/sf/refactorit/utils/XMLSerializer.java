/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 *
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

import net.sf.refactorit.common.util.AppRegistry;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Utility class to serialize XML Documents.
 *
 * This class is created only for NetBeans compatibility
 * since different NB versions has different flaws and
 * resolution of one flaw conflicts with others...
 *
 * The only working solution is to share one standard NB module
 * for Xerces library, which is done for NB >= 3.5, but since
 * we need to support 3.4 too this hack was introduced.
 *
 * @author Igor Malinin
 */
public abstract class XMLSerializer {
  private static final DefaultSerializer DEFAULT = new DefaultSerializer();

  public static XMLSerializer getDefaultSerializer() {
    return DEFAULT;
  }

  public void serialize(
      Document document, OutputStream out
  ) throws IOException {
    serialize(document, out, "UTF-8");
  }

  public abstract void serialize(
      Document document, OutputStream out, String enc
  ) throws IOException;

  static final class DefaultSerializer extends XMLSerializer {
    private static final Logger log =
        AppRegistry.getLogger(DefaultSerializer.class);

    public final void serialize(
        Document document, OutputStream out, String enc
    ) throws IOException {
      TransformerFactory factory = TransformerFactory.newInstance();
      try {
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, enc);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(document), new StreamResult(out));
      } catch (TransformerConfigurationException e) {
        log.error(e);
        throw new RuntimeException(e);
      } catch (IllegalArgumentException e) {
        log.error(e);
        throw new RuntimeException(e);
      } catch (TransformerException e) {
        log.warn(e);
        throw new IOException(e.getMessage());
      }
    }
  }
}
