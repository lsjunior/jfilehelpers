/*
 * MasterDetailEngine.java
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
package org.coury.jfilehelpers.masterdetail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.coury.jfilehelpers.core.ForwardReader;
import org.coury.jfilehelpers.core.RecordInfo;
import org.coury.jfilehelpers.engines.EngineBase;
import org.coury.jfilehelpers.engines.LineInfo;
import org.coury.jfilehelpers.helpers.ProgressHelper;
import org.coury.jfilehelpers.helpers.StringHelper;

/**
 * Handles flat files with master-detail information
 *
 * @author Felipe G. Coury <felipe.coury@gmail.com>
 *
 * @param <MT> Master Type
 * @param <DT> Detail Type
 */
public class MasterDetailEngine<MT, DT> extends EngineBase<DT> {

  private final RecordInfo<MT> masterInfo;

  private final MasterDetailSelector recordSelector;

  public MasterDetailEngine(final Class<MT> masterRecordClass, final Class<DT> detailRecordClass,
      final MasterDetailSelector recordSelector) {
    super(detailRecordClass);
    this.masterInfo = new RecordInfo<MT>(masterRecordClass);
    this.recordSelector = recordSelector;
  }

  public MasterDetailEngine(final Class<MT> masterRecordClass, final Class<DT> detailRecordClass,
      final CommonSelector action, final String selector) {
    super(detailRecordClass);
    this.masterInfo = new RecordInfo<MT>(masterRecordClass);
    final CommonInternalSelector sel = new CommonInternalSelector(action, selector,
        this.masterInfo.isIgnoreEmptyLines() || this.recordInfo.isIgnoreEmptyLines());

    this.recordSelector = new MasterDetailSelector() {

      @Override
      public RecordAction getRecordAction(final String recordString) {
        return sel.getCommonSelectorMethod(recordString);
      }

    };
  }

  public List<MasterDetails<MT, DT>> readResource(final String fileName) throws IOException {
    List<MasterDetails<MT, DT>> tempRes = null;

    InputStreamReader fr = null;
    try {
      fr = new InputStreamReader(this.getClass().getResourceAsStream(fileName));
      tempRes = this.readStream(fr);
    } finally {
      if (fr != null) {
        fr.close();
      }
    }

    return tempRes;
  }

  public List<MasterDetails<MT, DT>> fromString(final String s) throws IOException {
    return this.readStream(new InputStreamReader(new ByteArrayInputStream(s.getBytes())));
  }

  public List<MasterDetails<MT, DT>> readFile(final String fileName) throws IOException {
    List<MasterDetails<MT, DT>> tempRes = null;

    FileReader fr = null;
    try {
      fr = new FileReader(new File(fileName));
      tempRes = this.readStream(fr);
    } finally {
      if (fr != null) {
        fr.close();
      }
    }

    return tempRes;
  }

  public void writeFile(final String fileName, final MasterDetails<MT, DT> record)
      throws IOException {
    List<MasterDetails<MT, DT>> list = new ArrayList<MasterDetails<MT, DT>>();
    list.add(record);

    this.writeFile(fileName, list);
  }

  public void writeFile(final String fileName, final List<MasterDetails<MT, DT>> records)
      throws IOException {
    this.writeFile(fileName, records, -1);
  }

  public void writeFile(final String fileName, final List<MasterDetails<MT, DT>> records,
      final int maxRecords) throws IOException {
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

  private void writeStream(final OutputStreamWriter osr, final List<MasterDetails<MT, DT>> records,
      final int maxRecords) throws IOException {
    BufferedWriter writer = new BufferedWriter(osr);

    this.resetFields();
    if ((this.getHeaderText() != null) && (this.getHeaderText().length() != 0)) {
      writer.write(this.getHeaderText());
      if (!this.getHeaderText().endsWith(StringHelper.NEW_LINE)) {
        writer.write(StringHelper.NEW_LINE);
      }
    }

    String currentLine = null;

    int max = records.size();

    if (maxRecords >= 0) {
      max = Math.min(max, maxRecords);
    }

    ProgressHelper.notify(this.notifyHandler, this.progressMode, 0, max);

    for (int i = 0; i < max; i++) {
      try {
        if (records.get(i) == null) {
          throw new IllegalArgumentException("The record at index " + i + " is null.");
        }

        ProgressHelper.notify(this.notifyHandler, this.progressMode, i + 1, max);

        currentLine = this.masterInfo.recordToStr(records.get(i).getMaster());
        writer.write(currentLine + StringHelper.NEW_LINE);

        if (records.get(i).getDetails() != null) {
          for (int d = 0; d < records.get(i).getDetails().size(); d++) {
            currentLine = this.recordInfo.recordToStr(records.get(i).getDetails().get(d));
            writer.write(currentLine + StringHelper.NEW_LINE);
          }
        }

        writer.flush();
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
    }

    this.totalRecords = records.size();

    if ((this.getFooterText() != null) && (this.getFooterText() != "")) {
      writer.write(this.getFooterText());
      if (!this.getFooterText().endsWith(StringHelper.NEW_LINE)) {
        writer.write(StringHelper.NEW_LINE);
      }
    }
  }

  private List<MasterDetails<MT, DT>> readStream(final InputStreamReader fileReader)
      throws IOException {
    BufferedReader reader = new BufferedReader(fileReader);

    this.resetFields();
    this.setHeaderText("");
    this.setFooterText("");

    List<MasterDetails<MT, DT>> resArray = new ArrayList<MasterDetails<MT, DT>>();

    ForwardReader freader = new ForwardReader(reader, this.masterInfo.getIgnoreLast());
    freader.setDiscardForward(true);

    String currentLine, completeLine;

    this.lineNumber = 1;

    completeLine = freader.readNextLine();
    currentLine = completeLine;

    ProgressHelper.notify(this.notifyHandler, this.progressMode, 0, -1);

    int currentRecord = 0;

    if (this.masterInfo.getIgnoreFirst() > 0) {
      for (int i = 0; (i < this.masterInfo.getIgnoreFirst()) && (currentLine != null); i++) {
        this.headerText += currentLine + StringHelper.NEW_LINE;
        currentLine = freader.readNextLine();
        this.lineNumber++;
      }
    }

    boolean byPass = false;
    MasterDetails<MT, DT> record = null;

    List<DT> tmpDetails = new ArrayList<DT>();

    LineInfo line = new LineInfo(currentLine);
    line.setReader(freader);

    while (currentLine != null) {
      try {
        currentRecord++;

        line.reload(currentLine);

        ProgressHelper.notify(this.notifyHandler, this.progressMode, currentRecord, -1);

        RecordAction action = this.recordSelector.getRecordAction(currentLine);
        switch (action) {
          case Master:
            if (record != null) {
              record.addDetails(tmpDetails);
              resArray.add(record);
            }

            this.totalRecords++;
            record = new MasterDetails<MT, DT>();
            tmpDetails.clear();

            MT lastMaster = this.masterInfo.strToRecord(line);

            if (lastMaster != null) {
              record.setMaster(lastMaster);
            }

            break;

          case Detail:
            DT lastChild = this.recordInfo.strToRecord(line);

            if (lastChild != null) {
              tmpDetails.add(lastChild);
            }
            break;

          default:
            break;
        }
      } catch (Exception ex) {
        // TODO error handling
        ex.printStackTrace();
        // switch (mErrorManager.ErrorMode)
        // {
        // case ErrorMode.ThrowException:
        // byPass = true;
        // throw;
        // case ErrorMode.IgnoreAndContinue:
        // break;
        // case ErrorMode.SaveAndContinue:
        // ErrorInfo err = new ErrorInfo();
        // err.mLineNumber = mLineNumber;
        // err.mExceptionInfo = ex;
        //// err.mColumnNumber = mColumnNum;
        // err.mRecordString = completeLine;
        //
        // mErrorManager.AddError(err);
        // break;
        // }
      } finally {
        if (byPass == false) {
          currentLine = freader.readNextLine();
          completeLine = currentLine;
          this.lineNumber = freader.getLineNumber();
        }
      }

    }

    if (record != null) {
      record.addDetails(tmpDetails);
      resArray.add(record);
    }

    if (this.masterInfo.getIgnoreLast() > 0) {
      this.footerText = freader.getRemainingText();
    }

    return resArray;
  }

  class CommonInternalSelector {

    private final String selector;

    private final boolean ignoreEmpty;

    private final CommonSelector action;

    public CommonInternalSelector(final CommonSelector action, final String selector,
        final boolean ignoreEmpty) {
      this.action = action;
      this.selector = selector;
      this.ignoreEmpty = ignoreEmpty;
    }

    protected RecordAction getCommonSelectorMethod(final String recordString) {
      if (this.ignoreEmpty && (recordString.length() < 1)) {
        return RecordAction.Skip;
      }

      switch (this.action) {
        case DetailIfContains:
          if (recordString.indexOf(this.selector) >= 0) {
            return RecordAction.Detail;
          } else {
            return RecordAction.Master;
          }

        case MasterIfContains:
          if (recordString.indexOf(this.selector) >= 0) {
            return RecordAction.Master;
          } else {
            return RecordAction.Detail;
          }

        case DetailIfBegins:
          if (recordString.startsWith(this.selector)) {
            return RecordAction.Detail;
          } else {
            return RecordAction.Master;
          }

        case MasterIfBegins:
          if (recordString.startsWith(this.selector)) {
            return RecordAction.Master;
          } else {
            return RecordAction.Detail;
          }

        case DetailIfEnds:
          if (recordString.endsWith(this.selector)) {
            return RecordAction.Detail;
          } else {
            return RecordAction.Master;
          }

        case MasterIfEnds:
          if (recordString.endsWith(this.selector)) {
            return RecordAction.Master;
          } else {
            return RecordAction.Detail;
          }

        case DetailIfEnclosed:
          if (recordString.startsWith(this.selector) && recordString.endsWith(this.selector)) {
            return RecordAction.Detail;
          } else {
            return RecordAction.Master;
          }

        case MasterIfEnclosed:
          if (recordString.startsWith(this.selector) && recordString.endsWith(this.selector)) {
            return RecordAction.Master;
          } else {
            return RecordAction.Detail;
          }
      }

      return RecordAction.Skip;
    }

  }
}
