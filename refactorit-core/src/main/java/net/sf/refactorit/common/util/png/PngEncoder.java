/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util.png;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;


/**
 * Png encoder utility class. This class need never be
 * instantiated, as all its methods are static.
 * <p>
 * Currently, only 8-bit palettes are supported. See the Palette class
 * for a further description of what is supported there, and limitations.
 */
public final class PngEncoder {

  private PngEncoder() {
  }

  /**
   * Encodes a buffered image to a given file.
   *
   * @param bi the buffered image to encode
   * @param file the file to save to
   */
  public static void encode(BufferedImage bi, File file) throws IOException {
    FileOutputStream fos = new FileOutputStream(file);
    try {
      encode(bi, fos);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Encodes a buffered image to a given filename.
   *
   * @param bi the buffered image to encode
   * @param filename the name of the file to save to
   */
  public static void encode(BufferedImage bi,
      String filename) throws IOException {
    encode(bi, new File(filename));
  }

  /**
   * Encodes a buffered image to a given file using a given palette.
   *
   * @param bi the buffered image to encode
   * @param file the file to save to
   * @param pal the palette to use
   */
  public static void encode(BufferedImage bi, File file,
      Palette pal) throws IOException {
    FileOutputStream fos = new FileOutputStream(file);
    try {
      encode(bi, fos, pal);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Encodes a buffered image to a given filename using a given palette.
   *
   * @param bi the buffered image to encode
   * @param filename the name of the file to save to
   * @param pal the palette to use
   */
  public static void encode(BufferedImage bi, String filename,
      Palette pal) throws IOException {
    encode(bi, new File(filename), pal);
  }

  private static final byte[] PNG_SIGNATURE = {(byte) 137, 80, 78, 71, 13, 10,
      26, 10};

  /**
   * Encodes a buffered image to a specified output stream
   *
   * @param bi the buffered image to encode
   * @param os the output stream to write the encoded data to
   */
  public static void encode(BufferedImage bi,
      OutputStream os) throws IOException {
    encode(bi, os, new Palette(bi, 256, true));
  }

  /**
   * Encodes a buffered image to an output stream, using the given 8-bit palette
   *
   * @param bi the buffered image to encode
   * @param os the output stream to write the encoded data to
   * @param pal the palette to use
   */
  public static void encode(BufferedImage bi, OutputStream os,
      Palette pal) throws IOException {
    Chunk header = new Chunk(Chunk.CHUNK_IHDR);
    Chunk palChunk = new Chunk(Chunk.CHUNK_PLTE);
    Chunk trans = new Chunk(Chunk.CHUNK_TRNS);
    Chunk data = new Chunk(Chunk.CHUNK_IDAT);
    Chunk end = new Chunk(Chunk.CHUNK_IEND);
    DeflaterOutputStream dos = new DeflaterOutputStream(data.getDataStream());
    try {
      // Write out the signature
      os.write(PNG_SIGNATURE);

      // Write out the header chunk
      int width = bi.getWidth();
      int height = bi.getHeight();

      header.writeInt(width);
      header.writeInt(height);
      header.writeByte(8); // 8-bit
      header.writeByte(3); // paletted
      header.writeByte(0); // deflate compression
      header.writeByte(0); // no filter
      header.writeByte(0); // no interlace
      header.output(os);

      // Construct the palette chunk
      for (int i = 0; i < pal.getSize(); i++) {
        palChunk.writeByte(pal.getRed(i));
        palChunk.writeByte(pal.getGreen(i));
        palChunk.writeByte(pal.getBlue(i));
      }
      palChunk.output(os);

      // Transparency
      for (int i = 0; i < pal.getTransparentIndex(); i++) {
        trans.writeByte(255);
      }
      trans.writeByte(0);
      trans.output(os);

      // Data chunk
      int[] rgb = new int[width];
      for (int y = 0; y < height; y++) {
        bi.getRGB(0, y, width, 1, rgb, 0, width);
        byte[] indices = pal.getIndices(rgb, 0, rgb.length);
        dos.write(0); // No filtering for this scanline
        dos.write(indices, 0, width);
      }
      dos.finish();
      data.output(os);

      // End chunk
      end.output(os);
    } finally {
      header.close();
      palChunk.close();
      trans.close();
      data.close();
      end.close();
      if (dos != null) {
        try {
          dos.close();
        } catch (IOException e) {
        }
      }
    }
  }
}
