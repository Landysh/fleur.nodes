package main.java.inflor.knime.data.type.cell.fcs;

import java.io.IOException;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.filestore.FileStore;

public class FCSFrameContent {

  public static final class CellSerializer implements DataCellSerializer<FCSFrameFileStoreDataCell> {

    @Override
    public FCSFrameFileStoreDataCell deserialize(DataCellDataInput input) throws IOException {
      try {
        return (FCSFrameFileStoreDataCell) input.readDataCell();
      } catch (final Exception e) {
        e.printStackTrace();
        throw new IOException("Error during deserialization");
      }
    }

    @Override
    public void serialize(FCSFrameFileStoreDataCell cell, DataCellDataOutput output) throws IOException {
      output.writeDataCell(cell);
    }
  }

  public static final DataType TYPE = DataType.getType(FCSFrameFileStoreDataCell.class);

  private FCSFrameMetaData metaData;

  public FCSFrameContent(FileStore fileStore, FCSFrameMetaData metaData) {
    this.metaData = metaData;
  }

  public FCSFrameMetaData getColumnStore() {
    return metaData;
  }

  public FCSFrameFileStoreDataCell toColumnStoreCell(FileStore fs, int size) {
    return new FCSFrameFileStoreDataCell(fs, metaData);
  }
}
