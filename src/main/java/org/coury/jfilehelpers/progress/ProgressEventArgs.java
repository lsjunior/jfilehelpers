/*
 * ProgressEventArgs.java
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

package org.coury.jfilehelpers.progress;

import org.coury.jfilehelpers.enums.ProgressMode;

/**
 * @author Robert Eccardt
 *
 */
public class ProgressEventArgs {

  private int progressCurrent;

  private int progressTotal;

  private ProgressMode progressMode = ProgressMode.DontNotify;

  public ProgressEventArgs(final ProgressMode mode, final int current, final int total) {
    this.progressMode = mode;
    this.progressCurrent = current;
    this.progressTotal = total;
  }

  public ProgressEventArgs() {
    this.progressMode = ProgressMode.DontNotify;
  }

  public int getProgressCurrent() {
    return this.progressCurrent;
  }

  public int getProgressTotal() {
    return this.progressTotal;
  }

  public ProgressMode getProgressMode() {
    return this.progressMode;
  }

}
