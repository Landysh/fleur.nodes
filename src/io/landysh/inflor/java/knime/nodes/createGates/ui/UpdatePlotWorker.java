package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import io.landysh.inflor.java.core.plots.AbstractFCSPlot;
import io.landysh.inflor.java.core.plots.ChartSpec;
import io.landysh.inflor.java.core.plots.PlotUtils;

public class UpdatePlotWorker extends SwingWorker<JFreeChart, String> {

	private JProgressBar progress;
	private ChartSpec plotSpec;
	double[] X;
	double[] Y;
	ChartPanel chatPanel;
	private JFreeChart newChart;

	public UpdatePlotWorker(JProgressBar progressBar, ChartPanel chartPanel, ChartSpec spec, double[] xData, double[] yData) {
		//UI Stuff
		this.progress = progressBar;
		
		//backgroundData
		this.plotSpec = spec;
		this.X = xData;
		this.Y = yData;
		this.chatPanel = chartPanel;
	}

	@Override
	protected JFreeChart doInBackground() throws Exception {
		// Start
		publish("Updating plot.");
		AbstractFCSPlot newPlot = PlotUtils.createPlot(plotSpec);
		publish("Loading data.");
		this.newChart = newPlot.createChart(X,Y);
		publish("Finished update");
		setProgress(100);
		return newChart;
	}

	@Override
	protected void process(List<String> chunks) {
		progress.setString(chunks.get(chunks.size()-1));
		progress.getModel().setValue(getProgress());
	}
	
	@Override
	protected void done(){
		chatPanel.setChart(newChart);
		progress.setVisible(false);
	}
	
}