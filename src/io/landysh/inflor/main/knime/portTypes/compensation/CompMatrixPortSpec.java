package io.landysh.inflor.main.knime.portTypes.compensation;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

public class CompMatrixPortSpec implements PortObjectSpec {

  public static final class Serializer extends PortObjectSpecSerializer<CompMatrixPortSpec> {
    @Override
    public CompMatrixPortSpec loadPortObjectSpec(PortObjectSpecZipInputStream in)
        throws IOException {
      return CompMatrixPortSpec.load(in);
    }

    @Override
    public void savePortObjectSpec(CompMatrixPortSpec spec, PortObjectSpecZipOutputStream out)
        throws IOException {
      spec.save(out);
    }
  }

  private static final NodeLogger LOGGER = NodeLogger.getLogger(CompMatrixPortSpec.class);

  private static final String SPEC_KEY = "spec";
  private static final String INPUT_DIMENSIONS_KEY = "Input Dimensions";
  private static final String OUTPUT_DIMENSIONS_KEY = "Output Dimensions";

  public CompMatrixPortSpec() {
    // no op, use with .load
  }

  public CompMatrixPortSpec(String[] inputDimensionNames, String[] outputDimensionNames) {
    mInputDimensions = inputDimensionNames;
    mOutputDimensions = outputDimensionNames;
  }

  
  public static CompMatrixPortSpec load(PortObjectSpecZipInputStream in) {
    ModelContentRO model = null;
    try {
      final ZipEntry zentry = in.getNextEntry();
      assert zentry.getName().equals(SPEC_KEY);
      model = ModelContent.loadFromXML(in);
    } catch (final IOException ioe) {
      LOGGER.error("Internal error: Could not load settings", ioe);
    }
    String[] inputDimensionNames = null;
    String[] outputDimensionNames = null;
    try {
      inputDimensionNames = model.getStringArray(INPUT_DIMENSIONS_KEY);
      outputDimensionNames = model.getStringArray(OUTPUT_DIMENSIONS_KEY);
    } catch (final InvalidSettingsException ise) {
      LOGGER.error("Internal error: Could not load settings", ise);
    }

    return new CompMatrixPortSpec(inputDimensionNames, outputDimensionNames);

  }

  private String[] mInputDimensions;

  private String[] mOutputDimensions;


  @Override
  public JComponent[] getViews() {
    
    JTable t1 = new JTable(new String[][] {mInputDimensions}, new String[]{"Input Dimensions"});
    JTable t2 = new JTable(new String[][] {mOutputDimensions}, new String[]{"Output Dimensions"});
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(t1, BorderLayout.NORTH);
    panel.add(t2, BorderLayout.SOUTH);
    Border border = new EtchedBorder();
    panel.setBorder(border);
    return new JComponent[] {panel};
  }

  public void save(PortObjectSpecZipOutputStream out) {

    // Create model and add values.
    final ModelContent modelOut = new ModelContent(SPEC_KEY);
    modelOut.addStringArray(INPUT_DIMENSIONS_KEY, mInputDimensions);
    modelOut.addStringArray(OUTPUT_DIMENSIONS_KEY, mOutputDimensions);
    try {
      out.putNextEntry(new ZipEntry(SPEC_KEY));
      modelOut.saveToXML(out);
    } catch (final IOException ioe) {
      LOGGER.error("Internal error: Could not save settings", ioe);
    }
  }
  
  public String[] getInputDimensions() {
    return mInputDimensions;
  }

  public String[] getoutputDimensions() {
    return mOutputDimensions;
  }

}
