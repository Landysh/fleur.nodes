package io.landysh.inflor.java.knime.dataTypes.columnStoreCell;

import java.io.IOException;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreCell;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;

public class ColumnStoreCell extends FileStoreCell {

  public static final class ColumnStoreCellSerializer
      implements DataCellSerializer<ColumnStoreCell> {

    @Override
    public ColumnStoreCell deserialize(DataCellDataInput input) throws IOException {
      try {
        final byte[] bytes = new byte[input.readInt()];
        input.readFully(bytes);
        FCSFrame cStore;
        cStore = FCSFrame.load(bytes);
        final ColumnStoreCell newCell = new ColumnStoreCell(cStore);
        return newCell;
      } catch (final Exception e) {
        e.printStackTrace();
        throw new IOException("Error during deserialization");
      }
    }

    @Override
    public void serialize(ColumnStoreCell cell, DataCellDataOutput output) throws IOException {
      final byte[] bytes = cell.getFCSFrame().save();
      output.writeInt(bytes.length);
      output.write(bytes);
    }
  }

  private static final long serialVersionUID = 1L;
  public static final DataType TYPE = DataType.getType(ColumnStoreCell.class, ColumnStoreCell.TYPE);
  /**
   * A cell type matching the functionality of the ColumnStorePortObject.
   */
  private final FCSFrame m_data;

  ColumnStoreCell(FCSFrame cStore) {
    // Use with deserializer.
    m_data = cStore;
  }

  public ColumnStoreCell(FileStore fs, FCSFrame cStore) {
    super(fs);
    m_data = cStore;
  }

  public FCSFrame getFCSFrame() {
    return m_data;
  }

  @Override
  public String toString() {
    return m_data.toString();
  }
}
