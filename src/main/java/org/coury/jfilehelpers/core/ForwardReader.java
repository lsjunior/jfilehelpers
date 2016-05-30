/*
 * ForwardReader.java
 *
 * Copyright (C) 2007 Felipe Gonçalves Coury <felipe.coury@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package org.coury.jfilehelpers.core;

import java.io.BufferedReader;
import java.io.IOException;

import org.coury.jfilehelpers.helpers.StringHelper;

public class ForwardReader {

  private final BufferedReader reader;

  private int forwardIndex = 0;

  private int forwardLines = 0;

  // private char[] EOF = StringHelper.NEW_LINE.toCharArray();
  // private int capacityHint = 64;

  private int remainingLines = 0;

  private int lineNumber = 0;

  private final String[] forwardStrings;

  private boolean discardForward = false;

  public ForwardReader(final BufferedReader reader) throws IOException {
    this(reader, 0, 0);
  }

  public ForwardReader(final BufferedReader reader, final int forwardLines) throws IOException {
    this(reader, forwardLines, 0);
  }

  public ForwardReader(final BufferedReader reader, final int forwardLines, final int startLine)
      throws IOException {
    this.reader = reader;
    this.forwardLines = forwardLines;
    this.lineNumber = startLine;

    this.forwardStrings = new String[forwardLines + 1];
    this.remainingLines = forwardLines + 1;

    for (int i = 0; i < (forwardLines + 1); i++) {
      this.forwardStrings[i] = reader.readLine();
      this.lineNumber++;

      if (this.forwardStrings[i] == null) {
        this.remainingLines = i;
        break;
      }
    }
  }

  public String readNextLine() throws IOException {
    if (this.remainingLines <= 0) {
      return null;
    } else {
      String res = this.forwardStrings[this.forwardIndex];

      if (this.remainingLines == (this.forwardLines + 1)) {
        this.forwardStrings[this.forwardIndex] = this.reader.readLine();
        this.lineNumber++;

        if (this.forwardStrings[this.forwardIndex] == null) {
          this.remainingLines--;
        }
      } else {
        this.remainingLines--;
        if (this.discardForward) {
          return null;
        }
      }

      this.forwardIndex = (this.forwardIndex + 1) % (this.forwardLines + 1);

      return res;
    }
  }

  public String getRemainingText() {
    StringBuffer sb = new StringBuffer(100);

    for (int i = 0; i < (this.remainingLines + 1); i++) {
      sb.append(this.forwardStrings[(this.forwardIndex + i) % (this.forwardLines + 1)]
          + StringHelper.NEW_LINE);
    }

    return sb.toString();
  }

  public void close() {
    try {
      this.reader.close();
    } catch (IOException e) {
    }
  }

  /**
   * @return the lineNumber
   */
  public int getLineNumber() {
    return this.lineNumber - 1;
  }

  /**
   * @return the discardForward
   */
  public boolean isDiscardForward() {
    return this.discardForward;
  }

  /**
   * @param discardForward the discardForward to set
   */
  public void setDiscardForward(final boolean discardForward) {
    this.discardForward = discardForward;
  }

}
