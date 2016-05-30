/*
 * ConverterBase.java
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

package org.coury.jfilehelpers.converters;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.coury.jfilehelpers.enums.ConverterKind;

public class ConvertHelpers {

  public static ConverterBase getConverter(final ConverterKind converterKind, final String format) {
    switch (converterKind) {
      case Date:
        return new ConvertHelpers().new DateTimeConverter(format);

      case Boolean:
        return new ConvertHelpers().new BooleanConverter();

      // case Byte:
      // return new ConvertHelpers.ByteConverter();
      //
      case Int:
        return new ConvertHelpers().new IntConverter(format);

      // case Float:
      // return new ConvertHelpers().new FloatConverter();
      //
      // case Double:
      // return new ConvertHelpers().new DoubleConverter();

    }
    return null;
  }

  public static ConverterBase getDefaultConverter(final Field field) {
    if (field.getType() == String.class) {
      return null;
    } else if (field.getType() == Date.class) {
      return new ConvertHelpers().new DateTimeConverter();
    } else if (field.getType() == Boolean.class) {
      return new ConvertHelpers().new BooleanConverter();
    } else if (field.getType() == Integer.class) {
      return new ConvertHelpers().new IntConverter();
    }
    return null;
  }

  public class DateTimeConverter extends ConverterBase {

    String format;

    public DateTimeConverter() {
      this(ConverterBase.getDefaultDateTimeFormat());
    }

    public DateTimeConverter(final String format) {
      if ((format == null) || (format.length() < 1)) {
        throw new IllegalArgumentException(
            "The format of the DateTime Converter can be null or empty.");
      }

      try {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.format(new Date());
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "The format: '" + format + " is invalid for the DateTime Converter.");
      }

      this.format = format;
    }

    @Override
    public Object stringToField(String from) {
      if (from == null) {
        from = "";
      }

      Object val;

      try {
        val = new SimpleDateFormat(this.format).parse(from);
      } catch (ParseException e) {
        String extra = "";
        if (from.length() > this.format.length()) {
          extra = " There are more chars than in the format string: '" + this.format + "'";
        } else if (from.length() < this.format.length()) {
          extra = " There are less chars than in the format string: '" + this.format + "'";
        } else {
          extra = " Using the format: '" + this.format + "'";
        }

        // throw new ConvertException(from, typeof (DateTime), extra);
        throw new RuntimeException(extra);
      }

      return val;
    }

    @Override
    public String fieldToString(final Object from) {
      return new SimpleDateFormat(this.format).format(from);
    }
  }

  public class BooleanConverter extends ConverterBase {

    private String trueString = null;

    private String falseString = null;

    private String trueStringLower = null;

    private String falseStringLower = null;

    public BooleanConverter() {}

    public BooleanConverter(final String trueString, final String falseString) {
      this.trueString = trueString;
      this.falseString = falseString;
      this.trueStringLower = trueString.toLowerCase();
      this.falseStringLower = falseString.toLowerCase();
    }

    @Override
    public Object stringToField(final String from) {
      Object val;
      try {
        String testTo = from.toLowerCase();
        if (this.trueString == null) {
          testTo = testTo.trim();
          if (testTo.equals("true") || testTo.equals("1")) {
            val = true;
          } else if (testTo.equals("false") || testTo.equals("0") || testTo.equals("")) {
            val = false;
          } else {
            throw new Exception();
          }
        } else {
          if (testTo.equals(this.trueStringLower) || testTo.trim().equals(this.trueStringLower)) {
            val = true;
          } else if (testTo.equals(this.falseStringLower)
              || testTo.trim().equals(this.falseStringLower)) {
            val = false;
          } else {
            // throw new ConvertException(from, typeof(bool), "The string: " + from + " cant be
            // recognized as boolean using the true/false values: " + mTrueString + "/" +
            // mFalseString);
            throw new RuntimeException("The string: " + from + " cant be recognized as boolean "
                + "using the true/false values: " + this.trueString + "/" + this.falseString);
          }
        }
      } catch (Exception e) {
        // throw new ConvertException(from, typeof (Boolean));
        throw new RuntimeException("Error converting: " + from + " to boolean");
      }

      return val;
    }

    @Override
    public String fieldToString(final Object from) {
      boolean b = Boolean.parseBoolean(from.toString());
      if (b) {
        if (this.trueString == null) {
          return "True";
        } else {
          return this.trueString;
        }
      } else if (this.falseString == null) {
        return "False";
      } else {
        return this.falseString;
      }

    }
  }

  public class IntConverter extends ConverterBase {

    String format;

    public IntConverter() {
      //
    }

    public IntConverter(final String format) {
      this.format = format;
    }

    @Override
    public Object stringToField(String from) {
      if (from != null) {
        from = from.trim();
      }
      if (format != null) {
        try {
          return new DecimalFormat(this.format).parse(from);
        } catch (ParseException e) {
          throw new RuntimeException(e);
        }
      }
      return Integer.parseInt(from);
    }

    @Override
    public String fieldToString(final Object from) {
      if (format != null) {
        return new DecimalFormat(this.format).format(from);
      }
      return from.toString();
    }

  }

}
