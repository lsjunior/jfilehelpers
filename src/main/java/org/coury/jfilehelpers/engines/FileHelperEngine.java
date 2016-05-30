/*
 * FileHelperEngine.java
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
package org.coury.jfilehelpers.engines;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.coury.jfilehelpers.core.ForwardReader;
import org.coury.jfilehelpers.events.AfterReadRecordEventArgs;
import org.coury.jfilehelpers.events.AfterReadRecordHandler;
import org.coury.jfilehelpers.events.AfterWriteRecordEventArgs;
import org.coury.jfilehelpers.events.AfterWriteRecordHandler;
import org.coury.jfilehelpers.events.BeforeReadRecordEventArgs;
import org.coury.jfilehelpers.events.BeforeReadRecordHandler;
import org.coury.jfilehelpers.events.BeforeWriteRecordEventArgs;
import org.coury.jfilehelpers.events.BeforeWriteRecordHandler;
import org.coury.jfilehelpers.helpers.ProgressHelper;
import org.coury.jfilehelpers.helpers.StringHelper;
import org.coury.jfilehelpers.interfaces.NotifyRead;
import org.coury.jfilehelpers.interfaces.NotifyWrite;

public class FileHelperEngine<T> extends EngineBase<T> implements Iterable<T> {

  private int maxRecords = 0;

  private int currentRecord = 0;

  private LineInfo line;

  private String currentLine;

  private String completeLine;

  private FileReader fr = null;

  private ForwardReader freader = null;

  private BeforeReadRecordHandler<T> beforeReadRecordHandler;

  private AfterReadRecordHandler<T> afterReadRecordHandler;

  private BeforeWriteRecordHandler<T> beforeWriteRecordHandler;

  private AfterWriteRecordHandler<T> afterWriteRecordHandler;

  public FileHelperEngine(final Class<T> recordClass) {
    super(recordClass);
  }

  public List<T> readFile(final String fileName) throws IOException {
    return this.readFile(fileName, Integer.MAX_VALUE);
  }

  public void writeFile(final String fileName, final List<T> records) throws IOException {
    this.writeFile(fileName, records, -1);
  }

  public void writeFile(final String fileName, final List<T> records, final int maxRecords)
      throws IOException {
    FileWriter fw = null;
    try {
      fw = new FileWriter(new File(fileName));
      // fw.write("ABCDEF\n");
      this.writeStream(fw, records, maxRecords);
    } finally {
      if (fw != null) {
        fw.flush();
        fw.close();
      }
    }
  }

  public void writeStream(final Writer writer, final List<T> records) throws IOException {
    this.writeStream(writer, (Iterable<T>) records, -1);
  }

  public void writeStream(final Writer writer, final List<T> records, final int maxRecords)
      throws IOException {
    this.writeStream(writer, (Iterable<T>) records, maxRecords);
  }

  public String getRecordsAsString(final List<T> records) throws IOException {
    StringWriter sw = new StringWriter();
    this.writeStream(sw, records, -1);
    return sw.getBuffer().toString();
  }

  private void writeStream(final Writer osr, final Iterable<T> records, final int maxRecords)
      throws IOException {
    BufferedWriter writer = new BufferedWriter(osr);

    try {
      this.resetFields();
      if ((this.getHeaderText() != null) && (this.getHeaderText().length() != 0)) {
        if (this.getHeaderText().endsWith(StringHelper.NEW_LINE)) {
          writer.write(this.getHeaderText());
        } else {
          writer.write(this.getHeaderText() + StringHelper.NEW_LINE);
        }
      }

      int max = maxRecords;
      if (records instanceof Collection) {
        max = Math.min(max < 0 ? Integer.MAX_VALUE : max, ((Collection<T>) records).size());
      }

      ProgressHelper.notify(this.notifyHandler, this.progressMode, 0, max);

      int recIndex = 0;
      boolean first = true;

      for (T rec : records) {
        if (recIndex == maxRecords) {
          break;
        }

        this.lineNumber++;

        try {
          if (rec == null) {
            throw new IllegalArgumentException("The record at index " + recIndex + " is null.");
          }

          if (first) {
            first = false;
          }

          boolean skip = false;
          ProgressHelper.notify(this.notifyHandler, this.progressMode, recIndex + 1, max);
          skip = this.onBeforeWriteRecord(rec);

          if (!skip) {
            this.currentLine = this.recordInfo.recordToStr(rec);
            this.currentLine = this.onAfterWriteRecord(this.currentLine, rec);
            writer.write(this.currentLine + StringHelper.NEW_LINE);
          }

        } catch (Exception ex) {
          ex.printStackTrace();
          // TODO error manager
          // switch (mErrorManager.ErrorMode)
          // {
          // case ErrorMode.ThrowException:
          // throw;
          // case ErrorMode.IgnoreAndContinue:
          // break;
          // case ErrorMode.SaveAndContinue:
          // ErrorInfo err = new ErrorInfo();
          // err.mLineNumber = mLineNumber;
          // err.mExceptionInfo = ex;
          //// err.mColumnNumber = mColumnNum;
          // err.mRecordString = currentLine;
          // mErrorManager.AddError(err);
          // break;
          // }
        }
        recIndex++;
      }
      this.currentLine = null;
      this.totalRecords = recIndex;

      // if (mFooterText != null && mFooterText != string.Empty)
      // if (mFooterText.EndsWith(StringHelper.NewLine))
      // writer.Write(mFooterText);
      // else
      // writer.WriteLine(mFooterText);
    } finally {
      writer.flush();
    }
  }

  public List<T> readFile(final String fileName, final int maxRecords) throws IOException {
    List<T> tempRes = null;
    Reader r = null;
    try {
      r = new FileReader(new File(fileName));
      tempRes = this.readStream(r, maxRecords);
    } finally {
      if (r != null) {
        r.close();
      }
    }
    return tempRes;
  }

  public List<T> readResource(final String resourceName) throws IOException {
    return this.readResource(resourceName, Integer.MAX_VALUE);
  }

  public List<T> readResource(final String fileName, final int maxRecords) throws IOException {
    List<T> tempRes = null;
    Reader r = null;
    try {
      r = new InputStreamReader(this.getClass().getResourceAsStream(fileName));
      tempRes = this.readStream(r, maxRecords);
    } finally {
      if (r != null) {
        r.close();
      }
    }

    return tempRes;
  }

  public List<T> readStream(final Reader fileReader) throws IOException {
    return this.readStream(fileReader, Integer.MAX_VALUE);
  }

  public List<T> readStream(final Reader fileReader, final int maxRecords) throws IOException {
    List<T> list = null;
    try {
      list = new ArrayList<T>();
      this.openStream(fileReader, maxRecords);
      for (T t : this) {
        if (t != null) {
          list.add(t);
        }
      }
    } catch (IOException e) {
      throw e;
    } finally {
      this.close();
    }
    return list;
  }

  public void openFile(final String fileName) throws IOException {
    this.openFile(fileName, Integer.MAX_VALUE);
  }

  public void openFile(final String fileName, final int maxRecords) throws IOException {
    this.fr = new FileReader(new File(fileName));
    this.openStream(this.fr, maxRecords);
  }

  public void openResource(final String resourceName) throws IOException {
    this.openResource(resourceName, Integer.MAX_VALUE);
  }

  public void openResource(final String fileName, final int maxRecords) throws IOException {
    Reader r = null;
    r = new InputStreamReader(this.getClass().getResourceAsStream(fileName));
    this.openStream(r, maxRecords);
  }

  public void openStream(final Reader fileReader, final int maxRecords) throws IOException {
    BufferedReader reader = new BufferedReader(fileReader);
    this.resetFields();
    this.setHeaderText("");
    this.setFooterText("");

    this.freader = new ForwardReader(reader, this.recordInfo.getIgnoreLast());
    this.freader.setDiscardForward(true);

    this.setLineNumber(1);
    this.completeLine = this.freader.readNextLine();
    this.currentLine = this.completeLine;

    ProgressHelper.notify(this.notifyHandler, this.progressMode, 0, -1);

    if (this.recordInfo.getIgnoreFirst() > 0) {
      for (int i = 0; (i < this.recordInfo.getIgnoreFirst()) && (this.currentLine != null); i++) {
        this.headerText += this.currentLine + StringHelper.NEW_LINE;
        this.currentLine = this.freader.readNextLine();
        this.lineNumber++;
      }
    }

    // TODO boolean byPass = false;

    if (maxRecords < 0) {
      this.maxRecords = Integer.MAX_VALUE;
    } else {
      this.maxRecords = maxRecords;
    }

    this.line = new LineInfo(this.currentLine);
    this.line.setReader(this.freader);
  }

  public void close() throws IOException {
    if (this.fr != null) {
      this.fr.close();
    }
  }

  public void setBeforeReadRecordHandler(final BeforeReadRecordHandler<T> beforeReadRecordHandler) {
    this.beforeReadRecordHandler = beforeReadRecordHandler;
  }

  public void setAfterReadRecordHandler(final AfterReadRecordHandler<T> afterReadRecordHandler) {
    this.afterReadRecordHandler = afterReadRecordHandler;
  }

  public void setBeforeWriteRecordHandler(
      final BeforeWriteRecordHandler<T> beforeWriteRecordHandler) {
    this.beforeWriteRecordHandler = beforeWriteRecordHandler;
  }

  public void setAfterWriteRecordHandler(final AfterWriteRecordHandler<T> afterWriteRecordHandler) {
    this.afterWriteRecordHandler = afterWriteRecordHandler;
  }

  private boolean onBeforeReadRecord(final BeforeReadRecordEventArgs<T> e) {
    if (this.beforeReadRecordHandler != null) {
      this.beforeReadRecordHandler.handleBeforeReadRecord(this, e);
      return e.getSkipThisRecord();
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private boolean onAfterReadRecord(final String line, final T record) {
    if (this.recordInfo.isNotifyRead()) {
      ((NotifyRead<T>) record).afterRead(this, line);
    }
    if (this.afterReadRecordHandler != null) {
      AfterReadRecordEventArgs<T> e =
          new AfterReadRecordEventArgs<T>(line, record, this.lineNumber);
      this.afterReadRecordHandler.handleAfterReadRecord(this, e);
      return e.getSkipThisRecord();
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private boolean onBeforeWriteRecord(final T record) {
    if (this.recordInfo.isNotifyWrite()) {
      ((NotifyWrite<T>) record).beforeWrite(this);
    }
    if (this.beforeWriteRecordHandler != null) {
      BeforeWriteRecordEventArgs<T> e = new BeforeWriteRecordEventArgs<T>(record, this.lineNumber);
      this.beforeWriteRecordHandler.handleBeforeWriteRecord(this, e);
      return e.getSkipThisRecord();
    }
    return false;
  }

  private String onAfterWriteRecord(final String line, final T record) {
    if (this.afterWriteRecordHandler != null) {
      AfterWriteRecordEventArgs<T> e =
          new AfterWriteRecordEventArgs<T>(record, this.lineNumber, line);
      this.afterWriteRecordHandler.handleAfterWriteRecord(this, e);
      return e.getRecordLine();
    }
    return line;
  }

  public boolean hasNext() {
    return (this.currentLine != null);
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {

      @Override
      public boolean hasNext() {
        return (FileHelperEngine.this.currentLine != null);
      }

      @Override
      public T next() {
        T record = null;
        if ((FileHelperEngine.this.currentLine != null)
            && (FileHelperEngine.this.currentRecord < FileHelperEngine.this.maxRecords)) {
          try {
            FileHelperEngine.this.totalRecords++;
            FileHelperEngine.this.currentRecord++;
            FileHelperEngine.this.line.reload(FileHelperEngine.this.currentLine);
            boolean skip = false;
            ProgressHelper.notify(FileHelperEngine.this.notifyHandler,
                FileHelperEngine.this.progressMode, FileHelperEngine.this.currentRecord, -1);
            BeforeReadRecordEventArgs<T> e = new BeforeReadRecordEventArgs<T>(
                FileHelperEngine.this.currentLine, FileHelperEngine.this.lineNumber);
            skip = FileHelperEngine.this.onBeforeReadRecord(e);
            if (e.getRecordLineChanged()) {
              FileHelperEngine.this.line.reload(e.getRecordLine());
            }
            if (!skip) {
              record = FileHelperEngine.this.recordInfo.strToRecord(FileHelperEngine.this.line);
              skip = FileHelperEngine.this.onAfterReadRecord(FileHelperEngine.this.currentLine,
                  record);
              if (skip) {
                record = null;
              }
            }
            FileHelperEngine.this.currentLine = FileHelperEngine.this.freader.readNextLine();
            FileHelperEngine.this.completeLine = FileHelperEngine.this.currentLine;
            FileHelperEngine.this.lineNumber++;
          } catch (IOException ex) {
            throw new Error(ex);
          }
        }
        return record;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    };
  }

}
