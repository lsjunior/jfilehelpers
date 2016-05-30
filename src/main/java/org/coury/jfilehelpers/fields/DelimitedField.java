/*
 * DelimitedField.java
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

package org.coury.jfilehelpers.fields;

import java.io.IOException;
import java.lang.reflect.Field;

import org.coury.jfilehelpers.core.ExtractedInfo;
import org.coury.jfilehelpers.engines.LineInfo;
import org.coury.jfilehelpers.enums.MultilineMode;
import org.coury.jfilehelpers.enums.QuoteMode;
import org.coury.jfilehelpers.enums.TrimMode;
import org.coury.jfilehelpers.helpers.StringHelper;

public class DelimitedField extends FieldBase {

  private String separator;

  private final char quoteChar = '\0';

  private QuoteMode quoteMode;

  private final MultilineMode quoteMultiline = MultilineMode.AllowForBoth;

  public DelimitedField(final Field fi, final String sep) {
    super(fi);
    this.separator = sep;
  }

  @Override
  protected ExtractedInfo extractFieldString(final LineInfo line) {
    if (this.isOptional() && line.isEol()) {
      return ExtractedInfo.Empty;
    }

    if (this.isLast()) {
      this.charsToDiscard = 0;
    } else {
      this.charsToDiscard = this.separator.length();
    }

    if (this.quoteChar == '\0') {
      return this.basicExtractString(line);
    } else {
      // TODO: UnComment and Fix

      if ((this.getTrimMode() == TrimMode.Both) || (this.getTrimMode() == TrimMode.Left)) {
        line.trimStart(this.getTrimChars());
      }

      String quotedStr = Character.toString(this.quoteChar);
      if (line.startsWith(quotedStr)) {
        try {
          return StringHelper.extractQuotedString(line, this.quoteChar,
              (this.quoteMultiline == MultilineMode.AllowForBoth)
                  || (this.quoteMultiline == MultilineMode.AllowForRead));
        } catch (IOException e) {
          throw new RuntimeException(
              "IOException extracting information from field '" + this.getFieldInfo().getName());
        }
      } else {
        if ((this.quoteMode == QuoteMode.OptionalForBoth)
            || (this.quoteMode == QuoteMode.OptionalForRead)) {
          return this.basicExtractString(line);
        } else if (line.startsWithTrim(quotedStr)) {
          throw new IllegalArgumentException("The field '" + this.getFieldInfo().getName()
              + "' has spaces before the QuotedChar at line " + line.getLineNumber()
              + ". Use the TrimAttribute to by pass this error. Field String: "
              + line.getCurrentString());
        } else {
          throw new IllegalArgumentException("The field '" + this.getFieldInfo().getName()
              + "' not begin with the QuotedChar at line " + line.getLineNumber() + ". "
              + "You can use @FieldQuoted(quoteMode=QuoteMode.OptionalForRead) "
              + "to allow optional quoted field.. " + "Field String: " + line.getCurrentString());
        }
      }

    }
  }

  private ExtractedInfo basicExtractString(final LineInfo line) {
    ExtractedInfo res;

    if (this.isLast()) {
      res = new ExtractedInfo(line);
    } else {
      int sepPos;

      sepPos = line.indexOf(this.separator);

      if (sepPos == -1) {
        if (this.isNextOptional() == false) {
          String msg = null;

          if (this.isFirst() && line.isEmptyFromPos()) {
            msg = "The line " + line.getLineNumber()
                + " is empty. Maybe you need to use the annotation "
                + "[@IgnoreEmptyLines] in your record class.";
          } else {
            msg = "The delimiter '" + this.separator + "' " + "can't be found after the field '"
                + this.getFieldInfo().getName() + "' at line " + line.getLineNumber()
                + " (the record has less fields, the delimiter "
                + "is wrong or the next field must be marked " + "as optional).";
          }

          // throw new FileHelpersException(msg);
          throw new IllegalArgumentException(msg);
        } else {
          sepPos = line.getLine().length - 1;
        }
      }

      res = new ExtractedInfo(line, sepPos);
    }
    return res;
  }

  @Override
  protected void createFieldString(final StringBuffer sb, final Object fieldValue) {
    String field = super.baseFieldString(fieldValue);

    boolean hasNewLine = field.indexOf(StringHelper.NEW_LINE) >= 0;

    // If have a new line and this is not allowed throw an exception
    if (hasNewLine && ((this.quoteMultiline == MultilineMode.AllowForRead)
        || (this.quoteMultiline == MultilineMode.NotAllow))) {

      throw new IllegalArgumentException("One value for the field " + this.getFieldInfo().getName()
          + " has a new line inside. To allow write this value you must "
          + "add a FieldQuoted attribute with the multiline option in true.");

    }

    // Add Quotes If:
    // - optional == false
    // - is optional and contains the separator
    // - is optional and contains a new line

    if ((this.quoteChar != '\0') && ((this.quoteMode == QuoteMode.AlwaysQuoted)
        || (this.quoteMode == QuoteMode.OptionalForRead)
        || (((this.quoteMode == QuoteMode.OptionalForWrite)
            || (this.quoteMode == QuoteMode.OptionalForBoth))
            && (field.indexOf(this.separator) >= 0))
        || hasNewLine)) {
      StringHelper.createQuotedString(sb, field, this.quoteChar);
    } else {
      sb.append(field);
    }

    if (!this.isLast()) {
      sb.append(this.separator);
    }
  }

  public String getSeparator() {
    return this.separator;
  }

  public void setSeparator(final String separator) {
    this.separator = separator;
    if (this.isLast()) {
      this.charsToDiscard = 0;
    } else {
      this.charsToDiscard = separator.length();
    }
  }

}
