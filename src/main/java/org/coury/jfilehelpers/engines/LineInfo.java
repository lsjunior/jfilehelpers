/*
 * LineInfo.java
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

package org.coury.jfilehelpers.engines;

import java.io.IOException;
import java.util.Arrays;

import org.coury.jfilehelpers.core.ForwardReader;
import org.coury.jfilehelpers.helpers.StringHelper;

public final class LineInfo {

  private static char[] emptyChars = new char[] {};

  private String lineStr;

  private char[] line;

  private int currentPos = 0;

  private int lineNumber;

  private ForwardReader reader;

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("currentLine = [").append(this.getLineStr()).append("]")
        .append(StringHelper.NEW_LINE);
    sb.append("   -> currentString = [").append(this.getCurrentString()).append("]")
        .append(StringHelper.NEW_LINE);
    sb.append("   -> currentLength = ").append(this.getCurrentLength())
        .append(StringHelper.NEW_LINE);
    sb.append("   -> isEol = ").append(this.isEol()).append(StringHelper.NEW_LINE);
    sb.append("   -> isEmptyFromPos = ").append(this.isEmptyFromPos())
        .append(StringHelper.NEW_LINE);

    return sb.toString();
    // + StringHelper.toStringBuilder(this);
  }

  public LineInfo(final String line) {
    this.lineStr = line;
    this.line = line == null ? LineInfo.emptyChars : line.toCharArray();
    this.currentPos = 0;
  }

  public String currentString() {
    return new String(this.line, this.currentPos, this.line.length - this.currentPos);
  }

  public int getCurrentLength() {
    return this.line.length - this.currentPos;
  }

  public boolean isEol() {
    return this.currentPos >= this.line.length;
  }

  public boolean isEmptyFromPos() {
    int length = this.line.length;
    int pos = this.currentPos;

    while ((pos < length)
        && (Arrays.binarySearch(StringHelper.WHITESPACE_CHARS, this.line[pos]) > 0)) {
      pos++;
    }

    return pos > length;
  }

  public void trimStart() {
    this.trimStartSorted(StringHelper.WHITESPACE_CHARS);
  }

  public void trimStart(final char[] toTrim) {
    Arrays.sort(toTrim);
    this.trimStartSorted(toTrim);
  }

  private void trimStartSorted(final char[] toTrim) {
    // Move the pointer to the first non to Trim char
    int length = this.line.length;

    while ((this.currentPos < length)
        && (Arrays.binarySearch(toTrim, this.line[this.currentPos]) >= 0)) {
      this.currentPos++;
    }
  }

  public boolean startsWith(final String str) {
    // Returns true if the string begin with str
    if (this.currentPos >= this.lineStr.length()) {
      return false;
    } else {
      return this.lineStr.substring(this.currentPos, str.length()).equalsIgnoreCase(str);
      // return Arrays.equals(mLineStr, mCurrentPos, str.Length, str, 0, str.Length,
      // CompareOptions.IgnoreCase) == 0;
    }
  }

  public boolean startsWithTrim(final String str) {
    int length = this.line.length;
    int pos = this.currentPos;

    while ((pos < length)
        && (Arrays.binarySearch(StringHelper.WHITESPACE_CHARS, this.line[pos]) > 0)) {
      pos++;
    }

    return this.lineStr.substring(pos, str.length()).equalsIgnoreCase(str);
  }

  // public String readLine() throws IOException {
  // String str = reader.readNextLine();
  // lineNumber++;
  // return str;
  // }

  public void readNextLine() throws IOException {
    this.lineStr = this.reader.readNextLine();
    this.line = this.lineStr.toCharArray();

    this.currentPos = 0;
  }

  public String getCurrentString() {
    return new String(this.line, this.currentPos, this.line.length - this.currentPos);
  }

  public void setReader(final ForwardReader reader) {
    this.reader = reader;
  }

  public int indexOf(final String toFind) {
    return this.lineStr.substring(this.currentPos).toUpperCase().indexOf(toFind.toUpperCase())
        + this.currentPos;
  }

  public void reload(final String line) {
    this.line = line == null ? LineInfo.emptyChars : line.toCharArray();
    this.lineStr = line;
    this.currentPos = 0;
  }

  public String getLineStr() {
    return this.lineStr;
  }

  public void setLineStr(final String lineStr) {
    this.lineStr = lineStr;
  }

  public char[] getLine() {
    return this.line;
  }

  public void setLine(final char[] line) {
    this.line = line;
  }

  public int getCurrentPos() {
    return this.currentPos;
  }

  public void setCurrentPos(final int currentPos) {
    this.currentPos = currentPos;
  }

  public int getLineNumber() {
    return this.lineNumber;
  }

  /**
   * @return the reader
   */
  public ForwardReader getReader() {
    return this.reader;
  }
}
