/*
 * ------------------------------------------------------------------------
 *  Copyright by Aaron Hart
 *  Email: Aaron.Hart@gmail.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 *
 * Created on December 13, 2016 by Aaron Hart
 */
package io.landysh.inflor.main.knime.dataTypes.FCSFrameCell;

import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreCell;
import org.knime.core.node.NodeLogger;

import io.landysh.inflor.main.core.data.FCSFrame;

public class FCSFrameFileStoreDataCell extends FileStoreCell implements FCSFrameDataValue  {

  public static final class Serializer implements DataCellSerializer<FCSFrameFileStoreDataCell> {

    @Override
    public FCSFrameFileStoreDataCell deserialize(DataCellDataInput input) throws IOException {
      try {
        final byte[] bytes = new byte[input.readInt()];
        input.readFully(bytes);
        FCSFrame cStore = FCSFrame.load(bytes);
        return new FCSFrameFileStoreDataCell(cStore);
      } catch (final Exception e) {
        e.printStackTrace();
        throw new IOException("Error during deserialization");
      }
    }

    @Override
    public void serialize(FCSFrameFileStoreDataCell cell, DataCellDataOutput output) throws IOException {
      final byte[] bytes = cell.getFCSFrameValue().save();
      output.writeInt(bytes.length);
      output.write(bytes);
    }
  }

  private static final long serialVersionUID = 1L;
  public static final DataType TYPE = DataType.getType(FCSFrameFileStoreDataCell.class, FCSFrameFileStoreDataCell.TYPE);
  /**
   * A cell type matching the functionality of the ColumnStorePortObject.
   */
  private final FCSFrame m_data;

  FCSFrameFileStoreDataCell(FCSFrame dataFrame) {
    
    // Use with deserializer.
    super();
    m_data = dataFrame;
  }

  public FCSFrameFileStoreDataCell(FileStore fs, FCSFrame dataFrame) {
    super(fs);
    m_data = dataFrame;
  }

  @Override
  public String toString() {
    return m_data.toString();
  }

  @Override
  public FCSFrame getFCSFrameValue() {
    return m_data;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean equalsDataCell(final DataCell dataCell) {
    FCSFrameFileStoreDataCell fcsCell = (FCSFrameFileStoreDataCell)dataCell;
    return m_data.getID().equals(fcsCell.getFCSFrameValue().getID());
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean equalContent(final DataValue otherValue) {
      try {
          return FCSFrameDataValue.equalContent(this, (FCSFrameDataValue)otherValue);
      } catch (IOException ex) {
          NodeLogger.getLogger(getClass()).error("IO problem: " + ex.getMessage(), ex);
          return false;
      }
  }
}
