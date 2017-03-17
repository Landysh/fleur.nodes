/*
 * ------------------------------------------------------------------------ Copyright 2016 by Aaron
 * Hart Email: Aaron.Hart@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License, Version 3, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses>.
 * ---------------------------------------------------------------------
 */
package inflor.knime.nodes.compensation.extract.fjmtx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import inflor.core.compensation.SpilloverCompensator;
import inflor.knime.ports.compensation.CompMatrixPortObject;
import inflor.knime.ports.compensation.CompMatrixPortSpec;

/**
 * This is the model implementation of ExtractCompJO. Extract a compenation matrix from a text file
 * generated with FlowJo for Mac. This has only been tested with exports from version 9 of FlowJo.
 *
 * @author Aaron Hart
 */
public class ExtractCompJONodeModel extends NodeModel {

  // the logger instance
  private static final NodeLogger logger = NodeLogger.getLogger(ExtractCompJONodeModel.class);

  private static final String EMPTY_MATRIX_WARNING =
      "This compensation matrix has only spillover values of zero. Compensation will not modify the input data.";

  private ExtractCompJoSettings modelSettings = new ExtractCompJoSettings();
  private SpilloverCompensator viewCompensator;

  private boolean isExecuted = false;


  /**
   * Constructor for the node model.
   */
  protected ExtractCompJONodeModel() {
    super(new PortType[0],
        new PortType[] {PortTypeRegistry.getInstance().getPortType(CompMatrixPortObject.class)});
  }

  /**
   * {@inheritDoc}
   * 
   * @throws CanceledExecutionException
   */
  @Override
  protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec)
      throws CanceledExecutionException {
    logger.info("Starting Execution");
    try {

      SpilloverCompensator compr = readCompensationFromMTXFile(modelSettings.getFilePath());
      viewCompensator = compr;
      CompMatrixPortSpec spec = createPortSpec(compr);
      if (compr.isEmpty()) {
        logger.warn(EMPTY_MATRIX_WARNING);
      }
      CompMatrixPortObject portObject = new CompMatrixPortObject(spec, compr.getSpilloverValues());
      isExecuted = true;
      return new PortObject[] {portObject};
    } catch (final Exception e) {
      logger.error("Execution Failed. See debug console for details.", e);
      throw new CanceledExecutionException("Execution Failed. See debug console for details.");
    }
  }

  /**
   * @param filePath - Path to an mtx file as generated from V9.x of FlowJo.
   * @return a new SpilloverCompensator generated from the specified file or null if something goes
   *         wrong.
   * @throws Exception
   */
  private SpilloverCompensator readCompensationFromMTXFile(String filePath)
      throws CanceledExecutionException {

    try (FileReader freader = new FileReader(filePath);
        BufferedReader reader = new BufferedReader(freader)) {

      ArrayList<String> lines = new ArrayList<>();
      String line = reader.readLine();
      while (line != null) {
        lines.add(line);
        line = reader.readLine();
      }
      String headerLine = lines.get(2);
      String[] dimensionList = headerLine.split("\t");
      ArrayList<Double> spilloverList = new ArrayList<>();
      Double[] spilloverValues = new Double[dimensionList.length * dimensionList.length];
      for (int i = 3; i < lines.size(); i++) {
        String[] valueLine = lines.get(i).split("\t");
        for (String s : valueLine) {
          Double dValue = Double.parseDouble(s);
          spilloverList.add(dValue);
        }
      }
      if (spilloverList.size() == spilloverValues.length) {
        spilloverValues = spilloverList.toArray(spilloverValues);
      } else {
        CanceledExecutionException re = new CanceledExecutionException(
            "failed to parse compensation matrix.  Input file invalid or in unexpected format.");
        logger.error(
            "failed to parse compensation matrix.  Input file invalid or in unexpected format.",
            re);
        throw re;
      }
      String[] outDimensionList = new String[dimensionList.length];

      for (int i = 0; i < outDimensionList.length; i++) {
        outDimensionList[i] = "[" + dimensionList[i] + "]";
      }

      return new SpilloverCompensator(dimensionList, outDimensionList, spilloverValues);

    } catch (Exception e) {
      logger.error("failed to parse compensation matrix.  File may be missing, or corrupted", e);
      throw new CanceledExecutionException("File not read.");
    }
  }

  private CompMatrixPortSpec createPortSpec(SpilloverCompensator compr) {
    return new CompMatrixPortSpec(compr.getInputDimensions(), compr.getOutputDimensions());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void reset() {
    viewCompensator = null;
    isExecuted = false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected CompMatrixPortSpec[] configure(final PortObjectSpec[] inSpecs)
      throws InvalidSettingsException {

    CompMatrixPortSpec spec = null;
    try {
      SpilloverCompensator compr = readCompensationFromMTXFile(modelSettings.getFilePath());
      spec = createPortSpec(compr);
    } catch (final Exception e) {
      logger.error("Error while checking file. Check that it exists and is valid.", e);
      throw new InvalidSettingsException(
          "Error while checking file. Check that it exists and is valid.");
    }
    return new CompMatrixPortSpec[] {spec};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveSettingsTo(final NodeSettingsWO settings) {
    modelSettings.save(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
      throws InvalidSettingsException {

    modelSettings.load(settings);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

    modelSettings.validate();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {

    // TODO load internal data.
    // Everything handed to output ports is loaded automatically (data
    // returned by the execute method, models loaded in loadModelContent,
    // and user settings set through loadSettingsFrom - is all taken care
    // of). Load here only the other internals that need to be restored
    // (e.g. data used by the views).

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void saveInternals(final File internDir, final ExecutionMonitor exec)
      throws IOException, CanceledExecutionException {

    // TODO save internal models.
    // Everything written to output ports is saved automatically (data
    // returned by the execute method, models saved in the saveModelContent,
    // and user settings saved through saveSettingsTo - is all taken care
    // of). Save here only the other internals that need to be preserved
    // (e.g. data used by the views).

  }

  public SpilloverCompensator getCompensator() {
    return viewCompensator;
  }

  public boolean isExecuted() {
    return isExecuted;
  }

}

