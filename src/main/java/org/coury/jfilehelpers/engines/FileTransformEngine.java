/*
 * FileTransformEngine.java
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

public final class FileTransformEngine<Source, Destination> {

  private Encoding destinationEncoding;

  private Destination destination;

  private Encoding sourceEncoding;

  private Source source;

  public FileTransformEngine(final Source source, final Destination destination) {
    this.source = source;
    this.destination = destination;
  }

  public Destination[] transformFile(final String sourceFile, final String destinationFile) {
    // TODO empty method stub
    return null;
  }

  public Encoding getDestinationEncoding() {
    return this.destinationEncoding;
  }

  public void setDestinationEncoding(final Encoding destinationEncoding) {
    this.destinationEncoding = destinationEncoding;
  }

  public Destination getDestination() {
    return this.destination;
  }

  public void setDestination(final Destination destination) {
    this.destination = destination;
  }

  public Encoding getSourceEncoding() {
    return this.sourceEncoding;
  }

  public void setSourceEncoding(final Encoding sourceEncoding) {
    this.sourceEncoding = sourceEncoding;
  }

  public Source getSource() {
    return this.source;
  }

  public void setSource(final Source source) {
    this.source = source;
  }
}
