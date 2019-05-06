package fleur.knime.nodes.compensation.convert;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import fleur.knime.ports.compensation.CompMatrixPortObject;
import fleur.knime.ports.compensation.CompMatrixPortSpec;

/**
 * This is the model implementation of ConvertMatrix.
 * Converts a compensation matrix to a standard KNIME Table.  
 *
 * @author Aaron Hart
 */
public class ConvertMatrixNodeModel extends NodeModel {
    
    /**
     * Constructor for the node model.
     */
    protected ConvertMatrixNodeModel() {
    
        super(
            /*in*/  new PortType[] {PortTypeRegistry.getInstance().getPortType(CompMatrixPortObject.class)}, 
            /*out*/ new PortType[] {PortTypeRegistry.getInstance().getPortType(BufferedDataTable.class)}
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[]  execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        BufferedDataContainer container = exec.createDataContainer(createSpec());
        
        CompMatrixPortObject cmpo = (CompMatrixPortObject) inObjects[0];
        
        CompMatrixPortSpec inSpec = (CompMatrixPortSpec)cmpo.getSpec();
        String[] inDimNames = inSpec.getInputDimensions();
        String[] outDimNames = inSpec.getOutputDimensions();
        Double[] spills = cmpo.getSpilloverValues();
        if (inDimNames.length*outDimNames.length == spills.length){
          for (int i=0;i<inDimNames.length;i++){
            for (int j=0;j<outDimNames.length;j++){
              DataCell[] row = new DataCell[] {new StringCell(outDimNames[j]), 
                                               new StringCell(inDimNames[i]),
                                               new DoubleCell(spills[i*inDimNames.length + j])};
              DataRow  dataRow = new DefaultRow("Row " + i*inDimNames.length + j, row);
              container.addRowToTable(dataRow);
            }
            
          }
          container.close();
          return new PortObject[] {container.getTable()};
        } else {
          throw new CanceledExecutionException("Matrix appears to be of an illogical shape.");
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

        DataTableSpec tableSpec = createSpec();
        return new PortObjectSpec[]{tableSpec};
    }

    private DataTableSpec createSpec() {
      DataTableSpecCreator creator = new DataTableSpecCreator();
      
      DataColumnSpec outDimCol = new DataColumnSpecCreator("Output Dimension", StringCell.TYPE).createSpec();
      DataColumnSpec inDimCol = new DataColumnSpecCreator("Input Dimension", StringCell.TYPE).createSpec();
      DataColumnSpec spillCol = new DataColumnSpecCreator("Spillover Value", DoubleCell.TYPE).createSpec();
      DataColumnSpec[] columns = new DataColumnSpec[]{outDimCol, inDimCol, spillCol};
      creator.addColumns(columns);
      return creator.createSpec();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

}

