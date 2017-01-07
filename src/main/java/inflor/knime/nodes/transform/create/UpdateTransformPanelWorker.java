package main.java.inflor.knime.nodes.transform.create;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import main.java.inflor.core.data.FCSDimension;
import main.java.inflor.core.data.FCSFrame;
import main.java.inflor.core.plots.SubsetResponseChart;
import main.java.inflor.core.transforms.AbstractTransform;
import main.java.inflor.core.utils.FCSUtilities;

public class UpdateTransformPanelWorker extends SwingWorker<ArrayList<ChartPanel>, String> {


  private TransformNodeSettings m_Settings;
  private JPanel transformPanel;
  private JProgressBar progressBar;
  private ArrayList<FCSFrame> dataSet;
  private ArrayList<ChartPanel> newCharts;

  public UpdateTransformPanelWorker(JProgressBar progressBar, JPanel transformPanel,
      TransformNodeSettings m_Settings, ArrayList<FCSFrame> dataSet2) {
    this.progressBar = progressBar;
    this.transformPanel = transformPanel;
    this.m_Settings = m_Settings;
    this.dataSet = dataSet2;
    progressBar.setStringPainted(true);
  }

  @Override
  protected ArrayList<ChartPanel> doInBackground() throws Exception {
    // Start
    newCharts = new ArrayList<ChartPanel>();
    int progress = 2;
    setProgress(progress);
    SubsetResponseChart newChart;
    TreeMap<String, AbstractTransform> transformSet = m_Settings.getAllTransorms();
    int parameterCount = transformSet.size();
    int delta = parameterCount / 98;
    for (Entry<String, AbstractTransform> e : m_Settings.getAllTransorms().entrySet()) {
      String parametername = e.getKey();
      AbstractTransform currentTransform = e.getValue();
      newChart = new SubsetResponseChart(parametername, currentTransform);
      Map<String, FCSDimension> dataModel = createDataModel(parametername, dataSet);
      JFreeChart chart = newChart.createChart(dataModel);
      ChartPanel panel = new ChartPanel(chart);
      newCharts.add(panel);
      progress += delta;
      setProgress(progress);
      publish(e.getKey());
    }
    return newCharts;
  }

  private TreeMap<String, FCSDimension> createDataModel(String parametername,
      ArrayList<FCSFrame> dataSet2) {
    TreeMap<String, FCSDimension> dataModel = new TreeMap<>();
    for (FCSFrame dataFrame : dataSet2) {
      dataModel.put(dataFrame.getDisplayName(),
          FCSUtilities.findCompatibleDimension(dataFrame, parametername));
    }
    return dataModel;
  }

  @Override
  protected void process(List<String> charts) {
    progressBar.getModel().setValue(getProgress());
  }

  @Override
  protected void done() {
    setProgress(0);
    progressBar.setStringPainted(false);
    transformPanel.removeAll();
    transformPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    for (ChartPanel panel : newCharts) {
      panel.setPreferredSize(new Dimension(400, 200));
      transformPanel.add(panel, gbc);
      gbc.gridy++;
    }
  }
}
