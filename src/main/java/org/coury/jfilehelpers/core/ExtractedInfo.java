/*
 * ExtractedInfo.java
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

import java.util.Arrays;

import org.coury.jfilehelpers.engines.LineInfo;
import org.coury.jfilehelpers.helpers.StringHelper;

public final class ExtractedInfo {

  public static final ExtractedInfo Empty = new ExtractedInfo("");

  private LineInfo line;

  private int extractedFrom;

  private int extractedTo;

  private String customExtractedString = null;

  public ExtractedInfo(final LineInfo line) {
    this.line = line;
    this.extractedFrom = line.getCurrentPos();
    this.extractedTo = line.getLine().length - 1;
  }

  public ExtractedInfo(final LineInfo line, final int extractTo) {
    this.line = line;
    this.extractedFrom = line.getCurrentPos();
    this.extractedTo = extractTo - 1;
  }

  public ExtractedInfo(final String customExtract) {
    this.customExtractedString = customExtract;
  }

  public void trimStart(final char[] sortedToTrim) {
    if (this.customExtractedString != null) {
      // customExtractedString = customExtractedString.trimStart(sortedToTrim);
      this.customExtractedString = StringHelper.trimStart(this.customExtractedString, sortedToTrim);
    } else {
      while ((this.extractedFrom < this.extractedTo)
          && (Arrays.binarySearch(sortedToTrim, this.line.getLine()[this.extractedFrom]) >= 0)) {
        this.extractedFrom++;
      }
    }
  }

  public void TrimEnd(final char[] sortedToTrim) {
    if (this.customExtractedString != null) {
      // customExtractedString = customExtractedString.trimEnd(sortedToTrim);
      this.customExtractedString = StringHelper.trimEnd(this.customExtractedString, sortedToTrim);
    } else {
      while ((this.extractedTo > this.extractedFrom)
          && (Arrays.binarySearch(sortedToTrim, this.line.getLine()[this.extractedTo]) >= 0)) {
        this.extractedTo--;
      }
    }
  }

  public void trimBoth(final char[] sortedToTrim) {
    if (this.customExtractedString != null) {
      // customExtractedString = mCustomExtractedString.Trim(sortedToTrim);
      this.customExtractedString = StringHelper.trimBoth(this.customExtractedString, sortedToTrim);
    } else {
      while ((this.extractedFrom <= this.extractedTo)
          && (Arrays.binarySearch(sortedToTrim, this.line.getLine()[this.extractedFrom]) >= 0)) {
        this.extractedFrom++;
      }

      while ((this.extractedTo > this.extractedFrom)
          && (Arrays.binarySearch(sortedToTrim, this.line.getLine()[this.extractedTo]) >= 0)) {
        this.extractedTo--;
      }
    }
  }

  public boolean hasOnlyThisChars(final char[] sortedArray) {
    // Check if the chars at pos or right are empty ones
    if (this.customExtractedString != null) {
      int pos = 0;
      while ((pos < this.customExtractedString.length())
          && (Arrays.binarySearch(sortedArray, this.customExtractedString.charAt(pos)) >= 0)) {
        pos++;
      }

      return pos == this.customExtractedString.length();
    } else {
      int pos = this.extractedFrom;
      while ((pos <= this.extractedTo)
          && (Arrays.binarySearch(sortedArray, this.line.getLine()[pos]) >= 0)) {
        pos++;
      }

      return pos > this.extractedTo;
    }
  }

  public int length() {
    return (this.extractedTo - this.extractedFrom) + 1;
  }

  public String extractedString() {
    if (this.customExtractedString == null) {
      return new String(this.line.getLine(), this.extractedFrom,
          (this.extractedTo - this.extractedFrom) + 1);
    } else {
      return this.customExtractedString;
    }
  }

  public LineInfo getLine() {
    return this.line;
  }

  public void setLine(final LineInfo line) {
    this.line = line;
  }

  public int getExtractedFrom() {
    return this.extractedFrom;
  }

  public void setExtractedFrom(final int extractedFrom) {
    this.extractedFrom = extractedFrom;
  }

  public int getExtractedTo() {
    return this.extractedTo;
  }

  public void setExtractedTo(final int extractedTo) {
    this.extractedTo = extractedTo;
  }

  public String getCustomExtractedString() {
    return this.customExtractedString;
  }

  public void setCustomExtractedString(final String customExtractedString) {
    this.customExtractedString = customExtractedString;
  }

  @Override
  public String toString() {
    return StringHelper.toStringBuilder(this);
  }
}
