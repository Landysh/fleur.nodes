package fleur.knime.ports.compensation;

import java.awt.BorderLayout;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;


public class CompMatrixPortSpec extends AbstractSimplePortObjectSpec {

  public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<CompMatrixPortSpec> {}

  private static final String INPUT_DIMENSIONS_KEY = "Input Dimensions";
  private static final String OUTPUT_DIMENSIONS_KEY = "Output Dimensions";

  private String[] mInputDimensions;
  private String[] mOutputDimensions;

  
  public CompMatrixPortSpec() {
    // no op, use with .load
  }

  public CompMatrixPortSpec(String[] inputDimensionNames, String[] outputDimensionNames) {
    mInputDimensions = inputDimensionNames;
    mOutputDimensions = outputDimensionNames;
  }



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
  
  public String[] getInputDimensions() {
    return mInputDimensions;
  }

  public String[] getOutputDimensions() {
    return mOutputDimensions;
  }

  @Override
  protected void save(ModelContentWO model) {
    model.addStringArray(INPUT_DIMENSIONS_KEY, mInputDimensions);
    model.addStringArray(OUTPUT_DIMENSIONS_KEY, mOutputDimensions);
  }

  @Override
  protected void load(ModelContentRO model) throws InvalidSettingsException {
    mInputDimensions = model.getStringArray(INPUT_DIMENSIONS_KEY);
    mOutputDimensions = model.getStringArray(OUTPUT_DIMENSIONS_KEY);
  }
  
  @Override
  public boolean equals(Object o){
    if (o instanceof CompMatrixPortSpec){
      CompMatrixPortSpec pos = (CompMatrixPortSpec) o;
      if (!Arrays.equals(pos.getInputDimensions(), getInputDimensions())
          ||!Arrays.equals(pos.getOutputDimensions(), getOutputDimensions())){
        return false;
      }
    } else {
      return false;
    }
    return true;    
  }
  
  @Override
  public int hashCode(){
    int code = 1;
    code = code * Arrays.hashCode(mInputDimensions);
    code = code * Arrays.hashCode(mOutputDimensions);
    return code;
  }
}
