package inflor.core.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import org.jfree.chart.JFreeChart;

import inflor.core.data.FCSDimension;
import inflor.core.data.FCSFrame;
import inflor.core.fcs.DimensionTypes;
import inflor.core.gates.AbstractGate;
import inflor.core.gates.ui.GateCreationToolBar;
import inflor.core.plots.AbstractFCChart;
import inflor.core.plots.ChartSpec;
import inflor.core.plots.ChartingDefaults;
import inflor.core.plots.FCSChartPanel;
import inflor.core.transforms.TransformSet;
import inflor.core.utils.FCSUtilities;
import inflor.core.utils.PlotUtils;

@SuppressWarnings("serial")
public class ChartEditorDialog extends JDialog {

  /**
   * A modal dialog from which new chart definitions will be created and existing charts may be
   * edited
   */

  private static final String HORIZONTAL_AXIS_GROUP_LABEL = "Horizontal Axis";
  private static final String RANGE_AXIS_GROUP_LABEL = "Vertical Axis";
  protected JPanel previewPanel;
  protected JPanel settingsPanel;
  protected JPanel contentPanel;

  private ChartSpec localSpec;
  private FCSFrame dataFrame;

  private boolean isOK = false;
  private FCSChartPanel chartPanel;
  private GateCreationToolBar gatingToolBar;
  private List<AbstractGate> gates;
  private String path;
  private TransformSet transforms;

  public ChartEditorDialog(Window topFrame, String path, FCSFrame dataFrame, List<AbstractGate> applicableGates, TransformSet transforms, ChartSpec spec) {
    /**
     * Use this constructor to edit an existing chart.
     * 
     * @param topFrame the frame in which this dialog resides. Required to make dialog modal
     * @param parent the parent dialog which stores the data model.
     * @param id The UUID of the domain object. typically found in the settingsModel.
     */
    super(topFrame);
    setModal(true);
    this.path = path;
    this.dataFrame = dataFrame;
    this.transforms = transforms;
    this.gates = applicableGates;
    // populate the dialog
    final JPanel content = createContentPanel(spec);
    getContentPane().add(content);
    pack();
    setLocationRelativeTo(getParent());
  }
  
  public ChartEditorDialog(Window topFrame,String path, FCSFrame selectedSample, List<AbstractGate> applicableGates, TransformSet transforms) {
    this(topFrame, path, selectedSample, applicableGates, transforms, null);
  }

  private FCSDimension guessDomainDimension(FCSFrame fcsFrame) {
    FCSDimension domainDimension = FCSUtilities.findPreferredDimensionType(fcsFrame, DimensionTypes.FORWARD_SCATTER);
    if (domainDimension!=null){
      return domainDimension;
    } else {
      return fcsFrame
          .getData()
          .first();
    }
  }
  
  private FCSDimension guessRangeDimension(FCSFrame fcsFrame) {
    FCSDimension domainDimension = FCSUtilities.findPreferredDimensionType(fcsFrame, DimensionTypes.SIDE_SCATTER);
    if (domainDimension!=null){
      return domainDimension;
    } else {
      return fcsFrame.getData().first();
    }
  }

  private JButton createCancelButton() {
    JButton button = new JButton();
    button.setText("Cancel");
    button.addActionListener(e -> setVisible(false));
    return button;
  }

  private JPanel createContentPanel(ChartSpec originalSpec) {
   // Create the panel
   contentPanel = new JPanel(new GridBagLayout());

   JButton okButton = createOkButton();
   JButton cancelButton = createCancelButton();
   final JPanel buttonPanel = new JPanel(new FlowLayout());
   buttonPanel.add(okButton);
   buttonPanel.add(cancelButton);
   JComboBox<FCSDimension> domainParameterSelector = new JComboBox<>();
   Component domainAxisGroup = createAxisGroup(HORIZONTAL_AXIS_GROUP_LABEL, domainParameterSelector);
   JComboBox<FCSDimension> rangeParameterSelector = new JComboBox<>();
   Component rangeAxisGroup = createAxisGroup(RANGE_AXIS_GROUP_LABEL, rangeParameterSelector);
   
   if (originalSpec!=null){
     localSpec = originalSpec.clone();
   } else {
     localSpec = new ChartSpec();
     localSpec.setDomainAxisName(guessDomainDimension(dataFrame).getShortName());
     localSpec.setRangeAxisName(guessRangeDimension(dataFrame).getShortName());
     localSpec.setParentID(dataFrame.getID());
     localSpec.setPlotType(ChartingDefaults.DEFAULT_PLOT_TYPE);
   }
   
   localSpec.setParentID(dataFrame.getID());
   
   setSelection(localSpec.getDomainAxisName(), domainParameterSelector);
   setSelection(localSpec.getRangeAxisName(), rangeParameterSelector);
   previewPanel = createPreviewPanel();
   domainParameterSelector.addActionListener(e ->{
     FCSDimension dimension = (FCSDimension) domainParameterSelector.getModel().getSelectedItem();
     localSpec.setDomainAxisName(dimension.getShortName());
     updatePreviewPlot();
     });

   rangeParameterSelector.addActionListener(e -> {
       FCSDimension dimension = (FCSDimension) rangeParameterSelector.getModel().getSelectedItem();
       localSpec.setRangeAxisName(dimension.getShortName());
      updatePreviewPlot();});
    // GridLayout
   
   GridBagConstraints gbc = new GridBagConstraints();
   gbc.anchor = GridBagConstraints.NORTHWEST;
   addGatingToolBar();
    // Preview Panel
    gbc.gridx = 0;
    gbc.gridy = 1;
    contentPanel.add(previewPanel, gbc);
    gbc.gridy = 3;
    contentPanel.add(domainAxisGroup, gbc);
    gbc.gridy = 4;
    contentPanel.add(rangeAxisGroup, gbc);
    // Button Panel
    gbc.anchor = GridBagConstraints.SOUTHEAST;
    gbc.gridy = 5;
    // ProgressBar
    gbc.gridy = 6;
    JProgressBar progressBar = new JProgressBar();
    progressBar.setVisible(false);
    contentPanel.add(progressBar, gbc);
    contentPanel.add(buttonPanel, gbc);
    return contentPanel;
  }

  private void addGatingToolBar() {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.gridx = 0;
    gbc.gridy = 0;
    contentPanel.add(gatingToolBar, gbc);
  }

  private void setSelection(String domainAxisName,
      JComboBox<FCSDimension> comboBox) {
    int itemCount = comboBox.getItemCount();
    for (int i=0;i<itemCount;i++){
      FCSDimension dim = comboBox.getItemAt(i);
      if (dim.getShortName().equals(domainAxisName)){
        comboBox.setSelectedIndex(i);
      }
    }
  }

  private FCSChartPanel createPreviewPanel() {
    AbstractFCChart previewPlot = PlotUtils.createPlot(localSpec);
    JFreeChart chart = previewPlot.createChart(dataFrame, transforms);
    chartPanel = new FCSChartPanel(chart, localSpec, dataFrame, transforms);
    gatingToolBar = new GateCreationToolBar(chartPanel);
    chartPanel.setSelectionListener(gatingToolBar.getSelectionListener());//TODO: bad design.
    chartPanel.createAnnotations(gates);
    return chartPanel;
  }

  private Component createAxisGroup(String label,JComboBox<FCSDimension> comboBox) {
    JPanel axisGroup = new JPanel();
    axisGroup.setLayout(new FlowLayout());
    TitledBorder groupBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), label);
    axisGroup.setBorder(groupBorder);
    
    dataFrame.getData().forEach(comboBox::addItem);
    
    axisGroup.add(comboBox);
    return axisGroup;
  }

  private JButton createOkButton() {
    JButton button  = new JButton();
    button.setText("Ok");
    button.addActionListener(e -> {
        isOK = true;
        setVisible(false);});
    return button;
  }

  protected void updatePreviewPlot() {
    if (chartPanel!=null){
      previewPanel.remove(chartPanel);
    }
    if (gatingToolBar!=null){
      contentPanel.remove(gatingToolBar);
    }
    AbstractFCChart abstractChart = PlotUtils.createPlot(localSpec);  
    JFreeChart chart = abstractChart.createChart(dataFrame, transforms);
    chartPanel = new FCSChartPanel(chart, localSpec, dataFrame, transforms);
    chartPanel.setChart(chart);
    gatingToolBar = new GateCreationToolBar(chartPanel);
    addGatingToolBar();
    chartPanel.setSelectionListener(gatingToolBar.getSelectionListener());
    chartPanel.createAnnotations(gates);
    chartPanel.revalidate();
    chartPanel.repaint();
    previewPanel.add(chartPanel);
    previewPanel.revalidate();
    previewPanel.repaint();    
  }

  public ChartSpec getChartSpec() {
    return localSpec;
  }

  public List<AbstractGate> getGates() {
    return this.chartPanel.createAbstractGates();
  }

  public String getReferenceID() {
    return this.dataFrame.getID();
  }

  public boolean isOK() {
    return isOK;
  }

  public String getParentID() {
    return dataFrame.getID();
  }

  public String getPath() {
    return this.path;
  }
}
