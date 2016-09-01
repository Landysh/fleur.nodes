package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import io.landysh.inflor.java.core.plots.InflorVisDataSet;
import io.landysh.inflor.java.core.plots.PlotSpec;

public class UpdatePlotWorker extends SwingWorker<Integer, String> {

	private JProgressBar progress;
	private JPanel plot;
	private PlotSpec plotSpec;
	private InflorVisDataSet data;

	public UpdatePlotWorker(JPanel previewPanel, JProgressBar progressBar, PlotSpec spec, InflorVisDataSet currentDataSet) {
		//UI Stuff
		this.plot = previewPanel;
		this.progress = progressBar;
		
		//backgroundData
		this.plotSpec = spec;
		this.data = currentDataSet;
		
	}

	@Override
	protected Integer doInBackground() throws Exception {
		// Start
		publish("Updating plot.");

		//TODO
		
		publish("Finished update");
		setProgress(100);
		return 1;
	}

	@Override
	protected void process(List<String> chunks) {
		progress.setString(chunks.get(chunks.size()-1));
		progress.getModel().setValue(getProgress());
	}
}