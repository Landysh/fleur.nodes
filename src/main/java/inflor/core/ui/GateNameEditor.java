package inflor.core.ui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

@SuppressWarnings("serial")
public class GateNameEditor extends JDialog {

  private static final boolean NAME_EDITABLE = true;
  private static final List<String> DEFAULT_GATE_NAMES =
      Arrays.asList(new String[] {"Scatter", "Live", "Singlets", "Lymphs", "T Cells", "B Cells"});

  protected ChartPanel panel;

  private JButton mOkButton = null;
  private JButton mCancelButton = null;
  private boolean isOK = false;
  private String gateName = "";


  public GateNameEditor() {

    /**
     * @param topFrame the frame in which this dialog resides. Required to make dialog modal
     */
    super();
    setModal(true);
    setTitle("Create gate");
    final JPanel content = createContentPanel();
    getContentPane().add(content);
    pack();
    setLocationRelativeTo(getParent());
  }

  private JButton createCancelButton() {
    mCancelButton = new JButton();
    mCancelButton.setText("Cancel");
    mCancelButton.addActionListener(e-> setVisible(false));
    return mCancelButton;
  }

  private JPanel createContentPanel() {
    // Create the panel
    JPanel contentPanel = new JPanel(new GridBagLayout());
    final GridBagConstraints gbc = new GridBagConstraints();

    // Gate name selector
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.gridx = 1;
    gbc.gridy = 1;
    JComboBox<String> gateNameBox = createGateNameBox();
    contentPanel.add(gateNameBox, gbc);

    // Button Panel
    gbc.anchor = GridBagConstraints.SOUTHEAST;
    gbc.gridy = 2;
    final JPanel buttonPanel = new JPanel(new FlowLayout());
    mOkButton = createOkButton();
    mCancelButton = createCancelButton();
    buttonPanel.add(mOkButton);
    buttonPanel.add(mCancelButton);
    contentPanel.add(buttonPanel, gbc);

    return contentPanel;
  }

  private JComboBox<String> createGateNameBox() {
    JComboBox<String> comboBox = new JComboBox<>();
    DEFAULT_GATE_NAMES.stream().sequential().forEachOrdered(comboBox::addItem);

    comboBox.setEditable(NAME_EDITABLE);

    comboBox.addActionListener(e -> gateName = (String) comboBox.getSelectedItem());
    comboBox.setSelectedIndex(0);
    return comboBox;
  }

  private JButton createOkButton() {
    mOkButton = new JButton();
    mOkButton.setText("Ok");
    mOkButton.addActionListener(e -> {
        setOK(true);
        setVisible(false);
    });
    return mOkButton;
  }

  public String getGateName() {
    return gateName;
  }

  public boolean isOK() {
    return isOK;
  }

  public void setOK(boolean isOK) {
    this.isOK = isOK;
  }
}
