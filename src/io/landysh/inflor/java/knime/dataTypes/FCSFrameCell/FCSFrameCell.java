package io.landysh.inflor.java.knime.dataTypes.FCSFrameCell;

import java.io.IOException;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreCell;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;

public class FCSFrameCell extends FileStoreCell implements Comparable<String>{

  public static final class ColumnStoreCellSerializer
      implements DataCellSerializer<FCSFrameCell> {

    @Override
    public FCSFrameCell deserialize(DataCellDataInput input) throws IOException {
      try {
        final byte[] bytes = new byte[input.readInt()];
        input.readFully(bytes);
        FCSFrame cStore = FCSFrame.load(bytes);
        final FCSFrameCell newCell = new FCSFrameCell(cStore);
        return newCell;
      } catch (final Exception e) {
        e.printStackTrace();
        throw new IOException("Error during deserialization");
      }
    }

    @Override
    public void serialize(FCSFrameCell cell, DataCellDataOutput output) throws IOException {
      final byte[] bytes = cell.getFCSFrame().save();
      output.writeInt(bytes.length);
      output.write(bytes);
    }
  }

  private static final long serialVersionUID = 1L;
  public static final DataType TYPE = DataType.getType(FCSFrameCell.class, FCSFrameCell.TYPE);
  /**
   * A cell type matching the functionality of the ColumnStorePortObject.
   */
  private final FCSFrame m_data;

  FCSFrameCell(FCSFrame cStore) {
    // Use with deserializer.
    m_data = cStore;
  }

  public FCSFrameCell(FileStore fs, FCSFrame cStore) {
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

  @Override
  public int compareTo(String o) {
    return m_data.toString().compareTo(o);
  }
}
