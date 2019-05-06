package inflor.core.plots;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Optional;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import fleur.core.data.FCSDimension;
import fleur.core.data.FCSFrame;
import fleur.core.transforms.AbstractTransform;
import fleur.core.transforms.TransformSet;
import inflor.core.utils.FCSUtilities;
import inflor.core.utils.PlotUtils;

public class ScatterPlot extends AbstractFCChart {

  public ScatterPlot(String priorUUID, ChartSpec spec) {
    super(priorUUID, spec);
  }

  public ScatterPlot(ChartSpec plotSpec) {
    this(null, plotSpec);
  }

  @Override
  public JFreeChart createChart(FCSFrame data, TransformSet transforms) {
	//Have a look for spec dimensions in the data frame: 
	Optional<FCSDimension> opDomainDimension = FCSUtilities.findCompatibleDimension(data, spec.getDomainAxisName());
    Optional<FCSDimension> opRangeDimension = FCSUtilities.findCompatibleDimension(data, spec.getRangeAxisName());
    
    ///if they are found, do what you were meant to do. 
    if (opDomainDimension.isPresent()&&opRangeDimension.isPresent()){
    	//get the transformed data.
    	AbstractTransform domainTransform = transforms.get(opDomainDimension.get().getShortName());
        double[] domainData = domainTransform.transform(opDomainDimension.get().getData());
        AbstractTransform rangeTransform = transforms.get(opDomainDimension.get().getShortName());
        double[] rangeData = rangeTransform.transform(opRangeDimension.get().getData());
        
        //create the data series.
        DefaultXYDataset plotData = new DefaultXYDataset();
        double[][] seriesArray = new double[2][domainData.length];
        for (int i=0;i<domainData.length;i++){
          seriesArray[0][i] = domainData[i];
          seriesArray[1][i] = rangeData[i];
        }
        plotData.addSeries("l1", seriesArray);
        XYPlot plot = new XYPlot();
        // Create the plot
        plot.setDataset(plotData);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.GRAY);
        Rectangle rect = new Rectangle(1, 1);
        renderer.setSeriesShape(0, rect);
        renderer.setSeriesLinesVisible(0, false);
        plot.setRenderer(renderer);
        plot.setDomainAxis(PlotUtils.createAxis(opDomainDimension.get().getDisplayName(), domainTransform));
        plot.setRangeAxis(PlotUtils.createAxis(opRangeDimension.get().getDisplayName(), rangeTransform));
        // Add to the panel
        JFreeChart chart = new JFreeChart(plot);
        chart.removeLegend();
        return chart;
    //Otherwise return null :(
    } else {
    	return null;
    }
  }
}