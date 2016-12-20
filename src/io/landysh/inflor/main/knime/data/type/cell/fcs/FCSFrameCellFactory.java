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
package io.landysh.inflor.main.knime.data.type.cell.fcs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellFactory.FromInputStream;
import org.knime.core.data.DataType;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.ExecutionContext;
import org.knime.core.util.FileUtil;

public class FCSFrameCellFactory implements FromInputStream {

  private final FileStoreFactory m_fileStoreFactory;

  public FCSFrameCellFactory() {
    m_fileStoreFactory = FileStoreFactory.createNotInWorkflowFileStoreFactory();
  }

  public FCSFrameCellFactory(ExecutionContext exec) {
    m_fileStoreFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);
  }

  @Override
  public DataCell createCell(InputStream input) throws IOException {
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    FileUtil.copy(input, output);
    output.close();
    final byte[] buffer = output.toByteArray();
    // Create the file store.
    final FileStore fs = m_fileStoreFactory.createFileStore("column.store");
    final FCSFrameFileStoreDataCell cell = new FCSFrameContent(buffer).toColumnStoreCell(fs);
    return cell;
  }

  @Override
  public DataType getDataType() {
    return FCSFrameFileStoreDataCell.TYPE;
  }
}
