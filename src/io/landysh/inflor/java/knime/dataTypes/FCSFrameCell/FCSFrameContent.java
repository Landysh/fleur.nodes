package io.landysh.inflor.java.knime.dataTypes.FCSFrameCell;

import java.io.IOException;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.filestore.FileStore;

import com.google.protobuf.InvalidProtocolBufferException;

import io.landysh.inflor.java.core.dataStructures.FCSFrame;

public class FCSFrameContent {

  public static final class CellSerializer implements DataCellSerializer<FCSFrameCell> {

    @Override
    public FCSFrameCell deserialize(DataCellDataInput input) throws IOException {
      try {
        final byte[] bytes = new byte[input.readInt()];
        input.readFully(bytes);
        FCSFrame cStore;
        cStore = FCSFrame.load(bytes);
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

  public static final DataType TYPE = DataType.getType(FCSFrameCell.class);

  private FCSFrame m_data = null;

  public FCSFrameContent(byte[] buffer) throws InvalidProtocolBufferException {
    m_data = FCSFrame.load(buffer);
  }

  public FCSFrameContent(FCSFrame dataFrame) {
    m_data = dataFrame;
  }

  public FCSFrame getColumnStore() {
    return m_data;
  }

  public FCSFrameCell toColumnStoreCell(FileStore fs) {
    return new FCSFrameCell(fs, m_data);
  }
}
