package io.landysh.inflor.main.knime.nodes.createGates.ui;

import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import io.landysh.inflor.main.core.dataStructures.FCSFrame;
import io.landysh.inflor.main.core.plots.AbstractFCChart;
import io.landysh.inflor.main.core.plots.ChartSpec;
import io.landysh.inflor.main.core.plots.PlotUtils;

public class UpdatePlotWorker extends SwingWorker<JFreeChart, String> {

  private JProgressBar progress;
  private ChartSpec plotSpec;
  private ChartPanel chatPanel;
  private JFreeChart newChart;
  private FCSFrame data;

  public UpdatePlotWorker(JProgressBar progressBar, ChartPanel chartPanel, ChartSpec spec,
      FCSFrame data) {
    // UI Stuff
    this.progress = progressBar;

    // backgroundData
    this.plotSpec = spec;
    this.data = data;
    this.chatPanel = chartPanel;
  }

  @Override
  protected JFreeChart doInBackground() throws Exception {
    // Start
    publish("Updating plot.");
    AbstractFCChart newPlot = PlotUtils.createPlot(plotSpec);
    publish("Loading data.");
    this.newChart = newPlot.createChart(data);
    publish("Finished update");
    setProgress(100);
    return newChart;
  }

  @Override
  protected void process(List<String> chunks) {
    progress.setString(chunks.get(chunks.size() - 1));
    progress.getModel().setValue(getProgress());
  }

  @Override
  protected void done() {
    chatPanel.setChart(newChart);
    progress.setVisible(false);
  }
}
