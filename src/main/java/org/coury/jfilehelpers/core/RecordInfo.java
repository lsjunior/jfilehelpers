/*
 * RecordInfo.java
 *
 * Copyright (C) 2007 Felipe Gon�alves Coury <felipe.coury@gmail.com>
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.coury.jfilehelpers.annotations.FixedLengthRecord;
import org.coury.jfilehelpers.annotations.IgnoreCommentedLines;
import org.coury.jfilehelpers.annotations.IgnoreEmptyLines;
import org.coury.jfilehelpers.annotations.IgnoreFirst;
import org.coury.jfilehelpers.annotations.IgnoreLast;
import org.coury.jfilehelpers.engines.LineInfo;
import org.coury.jfilehelpers.enums.RecordCondition;
import org.coury.jfilehelpers.fields.FieldBase;
import org.coury.jfilehelpers.fields.FieldFactory;
import org.coury.jfilehelpers.fields.FixedLengthField;
import org.coury.jfilehelpers.helpers.ConditionHelper;
import org.coury.jfilehelpers.helpers.StringHelper;
import org.coury.jfilehelpers.interfaces.NotifyRead;
import org.coury.jfilehelpers.interfaces.NotifyWrite;

/**
 * Information about one record of information. Keep field types and its values and settings for
 * importing/exporting from this records.
 * 
 * @author Felipe Gon�alves Coury <felipe.coury@gmail.com>
 * @param <T> Type of the record
 */
public final class RecordInfo<T> {

  private FieldBase[] fields;

  private final Class<T> recordClass;

  private Constructor<T> recordConstructor;

  private int ignoreFirst = 0;

  private int ignoreLast = 0;

  private boolean ignoreEmptyLines = false;

  private boolean ignoreEmptySpaces = false;

  private String commentMarker = null;

  private boolean commentAnyPlace = true;

  private RecordCondition recordCondition = RecordCondition.None;

  private String recordConditionSelector = "";

  private boolean notifyRead = false;

  private boolean notifyWrite = false;

  private String conditionRegEx = null;

  private int sizeHint = 32;

  // private ConverterBase converterProvider = null;

  private int fieldCount;

  public RecordInfo(final Class<T> recordClass) {
    // this.recordObject = recordObject;
    this.recordClass = recordClass;
    this.initFields();
  }

  /**
   * Parses a text line into a record object
   * 
   * @param line current text line extracted from file
   * @return parsed object
   */
  public T strToRecord(final LineInfo line) {
    if (this.mustIgnoreLine(line.getLineStr())) {
      return null;
    }

    Object[] values = new Object[this.fieldCount];

    // array that holds the fields values
    T record = null;
    try {
      for (int i = 0; i < this.fieldCount; i++) {
        values[i] = this.fields[i].extractValue(line);
      }

      record = this.createRecordObject();
      for (int i = 0; i < this.fieldCount; i++) {
        // sets the field on the object
        RecordInfo.setInternalField(this.fields[i].getFieldInfo().getName(), record, values[i]);
        // Field f = record.getClass().getDeclaredField(fields[i].getFieldInfo().getName());
        // f.set(record, values[i]);
        // fields[i].getFieldInfo().set(record, values[i]);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Problems while reading field values from " + record + " object",
          e);
    }

    return record;

    // TODO Improve
    // CreateAssingMethods();
    //
    // try
    // {
    // // Asign all values via dinamic method that creates an object and assign values
    // return mCreateHandler(mValues);
    // }
    // catch (InvalidCastException)
    // {
    // // Occurrs when the a custom converter returns an invalid value for the field.
    // for (int i = 0; i < mFieldCount; i++)
    // {
    // if (mValues[i] != null && ! mFields[i].mFieldType.IsInstanceOfType(mValues[i]))
    // throw new ConvertException(null, mFields[i].mFieldType, mFields[i].mFieldInfo.Name,
    // line.mReader.LineNumber, -1, "The converter for the field: " + mFields[i].mFieldInfo.Name + "
    // returns an object of Type: " + mValues[i].GetType().Name + " and the field is of type: " +
    // mFields[i].mFieldType.Name);
    // }
    // return null;
    // }
  }

  static Object getInternalField(final String fieldName, final Object target) {
    Object value = AccessController.doPrivileged(new PrivilegedAction<Object>() {

      @Override
      public Object run() {
        Object result = null;
        java.lang.reflect.Field field = null;
        Class<?> clazz = target.getClass();
        while ((field == null) && (clazz != null)) {
          try {
            field = clazz.getDeclaredField(fieldName);
          } catch (Exception e) {
            //
          } finally {
            clazz = clazz.getSuperclass();
          }
        }
        if (field != null) {
          try {
            field.setAccessible(true);
            result = field.get(target);
          } catch (Exception e) {
            //
          }
        }
        return result;
      }
    });
    return value;
  }

  static void setInternalField(final String fieldName, final Object target, final Object value) {
    AccessController.doPrivileged(new PrivilegedAction<Object>() {

      @Override
      public Object run() {
        java.lang.reflect.Field field = null;
        Class<?> clazz = target.getClass();
        while ((field == null) && (clazz != null)) {
          try {
            field = clazz.getDeclaredField(fieldName);
          } catch (Exception e) {
            //
          } finally {
            clazz = clazz.getSuperclass();
          }
        }
        if (field != null) {
          try {
            field.setAccessible(true);
            field.set(target, value);
          } catch (Exception e) {
            //
          }
        }
        return null;
      }
    });
  }

  /**
   * Creates a string representation of the record object
   * 
   * @param record the record object
   * @return string representation of the record object, respecting rules defined
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  public String recordToStr(final T record)
      throws IllegalArgumentException, IllegalAccessException {
    StringBuffer sb = new StringBuffer(this.sizeHint);

    Object[] values = new Object[this.fieldCount];
    for (int i = 0; i < this.fieldCount; i++) {
      // values[i] = fields[i].getFieldInfo().get(record);
      values[i] = RecordInfo.getInternalField(this.fields[i].getFieldInfo().getName(), record);

    }

    for (int i = 0; i < this.fieldCount; i++) {
      this.fields[i].assignToString(sb, values[i]);
    }

    return sb.toString();
  }

  /**
   * Instantiates a new object of the record class type
   * 
   * @return the newly instatiated object
   * @throws IllegalArgumentException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private T createRecordObject() throws IllegalArgumentException, InstantiationException,
      IllegalAccessException, InvocationTargetException {
    try {
      return this.recordConstructor.newInstance();
    } catch (IllegalArgumentException e) {
      Object parameter = this.recordClass.getEnclosingClass().newInstance();
      return this.recordConstructor.newInstance(parameter);
    }
  }

  /**
   * Verifies if current line should be ignored
   * 
   * @param line line to be examined
   * @return true or false indicating if passed line should be ignored
   */
  private boolean mustIgnoreLine(final String line) {
    if (this.ignoreEmptyLines) {
      if ((this.ignoreEmptySpaces && (line.trim().length() == 0)) || (line.length() == 0)) {
        return true;
      }
    }

    if ((this.commentMarker != null) && (this.commentMarker.length() > 0)) {
      if ((this.commentAnyPlace && line.trim().startsWith(this.commentMarker))
          || line.startsWith(this.commentMarker)) {
        return true;
      }
    }

    switch (this.recordCondition) {
      case ExcludeIfBegins:
        return ConditionHelper.beginsWith(line, this.recordConditionSelector);

      case IncludeIfBegins:
        return !ConditionHelper.beginsWith(line, this.recordConditionSelector);

      case ExcludeIfContains:
        return ConditionHelper.contains(line, this.recordConditionSelector);

      case IncludeIfContains:
        return !ConditionHelper.contains(line, this.recordConditionSelector);

      case ExcludeIfEnclosed:
        return ConditionHelper.enclosed(line, this.recordConditionSelector);

      case IncludeIfEnclosed:
        return !ConditionHelper.enclosed(line, this.recordConditionSelector);

      case ExcludeIfEnds:
        return ConditionHelper.endsWith(line, this.recordConditionSelector);

      case IncludeIfEnds:
        return !ConditionHelper.endsWith(line, this.recordConditionSelector);

      case ExcludeIfMatchRegex:
        return Pattern.matches(this.conditionRegEx, line);

      case IncludeIfMatchRegex:
        return !Pattern.matches(this.conditionRegEx, line);

    }

    return false;
  }

  /**
   * Initiate the values of member fields by looking for annotations on the record object that
   * changes behavior
   */
  private void initFields() {
    IgnoreFirst igf = this.recordClass.getAnnotation(IgnoreFirst.class);
    if (igf != null) {
      this.ignoreFirst = igf.lines();
    }

    IgnoreLast igl = this.recordClass.getAnnotation(IgnoreLast.class);
    if (igl != null) {
      this.ignoreLast = igl.lines();
    }

    this.ignoreEmptyLines = this.recordClass.isAnnotationPresent(IgnoreEmptyLines.class);

    IgnoreCommentedLines igc = this.recordClass.getAnnotation(IgnoreCommentedLines.class);
    if (igc != null) {
      this.commentMarker = igc.commentMarker();
      this.commentAnyPlace = igc.anyPlace();
    }

    // TODO ConditionalRecord

    if (NotifyRead.class.isAssignableFrom(this.recordClass)) {
      this.notifyRead = true;
    }

    if (NotifyWrite.class.isAssignableFrom(this.recordClass)) {
      this.notifyWrite = true;
    }

    try {
      this.recordConstructor = this.recordClass.getConstructor();
    } catch (SecurityException e) {
      throw new RuntimeException(
          "The class " + this.recordClass.getName() + " needs to be accessible to be used");
    } catch (NoSuchMethodException e) {
      boolean throwIt = true;

      try {
        if (this.recordClass.getEnclosingClass() != null) {
          this.recordConstructor =
              this.recordClass.getConstructor(this.recordClass.getEnclosingClass());
        }
        throwIt = false;
      } catch (NoSuchMethodException e1) {
      }

      if (throwIt) {
        throw new RuntimeException("The class " + this.recordClass.getName()
            + " needs to have an empty constructor to be used");
      }
    }

    Class<?> clazz = this.recordClass;
    List<FieldBase> list = new ArrayList<>();
    while (clazz != null) {
      List<FieldBase> tmp = RecordInfo.createCoreFields(clazz.getDeclaredFields(), clazz);
      if ((!list.isEmpty()) && (!tmp.isEmpty())) {
        tmp.get(tmp.size() - 1).setLast(false);
      }
      list.addAll(0, tmp);
      clazz = clazz.getSuperclass();
    }

    this.fields = list.toArray(new FieldBase[list.size()]);
    this.fieldCount = this.fields.length;

    if (this.isFixedLength()) {
      this.sizeHint = 0;
      for (int i = 0; i < this.fieldCount; i++) {
        this.sizeHint += ((FixedLengthField) this.fields[i]).getFieldLength();
      }
    }

    if (this.fieldCount == 0) {
      throw new IllegalArgumentException(
          "The record class " + this.recordClass.getName() + " don't contains any field.");
    }
  }

  /**
   * Indicates if this record is of fixed length
   * 
   * @return true if record is fixed length, false otherwise
   */
  private boolean isFixedLength() {
    return this.recordClass.isAnnotationPresent(FixedLengthRecord.class);
  }

  /**
   * Creates field descriptors for each field of the record class
   * 
   * @param fields current fields of the record class, acquired via reflection
   * @param recordClass the record class
   * @return an array of FieldBase, field descriptor objects
   */
  @SuppressWarnings("unchecked")
  private static List<FieldBase> createCoreFields(final Field[] fields, final Class recordClass) {
    FieldBase field;
    List<FieldBase> fieldArr = new ArrayList<FieldBase>();

    boolean someOptional = false;
    if (fields != null) {
      for (Field f : fields) {
        field = FieldFactory.createField(f, recordClass, someOptional);
        if (field != null) {
          someOptional = field.isOptional();
          fieldArr.add(field);
          if (fieldArr.size() > 1) {
            fieldArr.get(fieldArr.size() - 2)
                .setNextOptional(fieldArr.get(fieldArr.size() - 1).isOptional());
          }
        }
      }
    }

    if (fieldArr.size() > 0) {
      fieldArr.get(0).setFirst(true);
      fieldArr.get(fieldArr.size() - 1).setLast(true);
    }

    return fieldArr;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return StringHelper.toStringBuilder(this);
  }

  public FieldBase[] getFields() {
    return this.fields;
  }

  public void setFields(final FieldBase[] fields) {
    this.fields = fields;
  }

  public int getIgnoreFirst() {
    return this.ignoreFirst;
  }

  public void setIgnoreFirst(final int ignoreFirst) {
    this.ignoreFirst = ignoreFirst;
  }

  public int getIgnoreLast() {
    return this.ignoreLast;
  }

  public void setIgnoreLast(final int ignoreLast) {
    this.ignoreLast = ignoreLast;
  }

  public boolean isIgnoreEmptyLines() {
    return this.ignoreEmptyLines;
  }

  public void setIgnoreEmptyLines(final boolean ignoreEmptyLines) {
    this.ignoreEmptyLines = ignoreEmptyLines;
  }

  public boolean isIgnoreEmptySpaces() {
    return this.ignoreEmptySpaces;
  }

  public void setIgnoreEmptySpaces(final boolean ignoreEmptySpaces) {
    this.ignoreEmptySpaces = ignoreEmptySpaces;
  }

  public String getCommentMarker() {
    return this.commentMarker;
  }

  public void setCommentMarker(final String commentMaker) {
    this.commentMarker = commentMaker;
  }

  public boolean isCommentAnyPlace() {
    return this.commentAnyPlace;
  }

  public void setCommentAnyPlace(final boolean commentAnyPlace) {
    this.commentAnyPlace = commentAnyPlace;
  }

  public RecordCondition getRecordCondition() {
    return this.recordCondition;
  }

  public void setRecordCondition(final RecordCondition recordCondition) {
    this.recordCondition = recordCondition;
  }

  public String getRecordConditionSelector() {
    return this.recordConditionSelector;
  }

  public void setRecordConditionSelector(final String recordConditionSelector) {
    this.recordConditionSelector = recordConditionSelector;
  }

  public boolean isNotifyRead() {
    return this.notifyRead;
  }

  public void setNotifyRead(final boolean notifyRead) {
    this.notifyRead = notifyRead;
  }

  public boolean isNotifyWrite() {
    return this.notifyWrite;
  }

  public void setNotifyWrite(final boolean notifyWrite) {
    this.notifyWrite = notifyWrite;
  }

  public String getConditionRegEx() {
    return this.conditionRegEx;
  }

  public void setConditionRegEx(final String conditionRegEx) {
    this.conditionRegEx = conditionRegEx;
  }

  public int getFieldCount() {
    return this.fieldCount;
  }

  public void setFieldCount(final int fieldCount) {
    this.fieldCount = fieldCount;
  }

  public Constructor<T> getRecordConstructor() {
    return this.recordConstructor;
  }

  public void setRecordConstructor(final Constructor<T> recordConstructor) {
    this.recordConstructor = recordConstructor;
  }
}
