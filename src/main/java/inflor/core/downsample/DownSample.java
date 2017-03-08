package inflor.core.downsample;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import inflor.core.data.FCSFrame;
import inflor.core.transforms.TransformSet;
import inflor.core.utils.BitSetUtils;
import inflor.core.utils.FCSUtilities;
import inflor.core.utils.MatrixUtilities;

public class DownSample {

  private DownSample(){}
  
  public static BitSet densityDependent(FCSFrame inFrame, List<String> dimensionNames, int targetSize, TransformSet transforms){
    	  
    //Calculate median minimum distance.
    BitSet shuffleMask = BitSetUtils.getShuffledMask(inFrame.getRowCount(), 2000);
    FCSFrame dsFrame = FCSUtilities.filterFrame(shuffleMask, inFrame);
    double[][] mtx = dsFrame.getMatrix(dimensionNames);
    FCSUtilities.transformMatrix(dimensionNames, transforms, mtx);
    mtx = MatrixUtilities.transpose(mtx);
    double minimumMedianDistance = calculateMinMedDistance(mtx);
    //Calculate the number of local neighbors for each cell.
    double[] localDensity = calculateLocalDensity(inFrame, mtx, minimumMedianDistance, dimensionNames, transforms);
    //Finally calculate od, and td, and fill the mask.
    return generateBitSet(inFrame, localDensity, targetSize);
  }

  private static double calculateMinMedDistance(double[][] mtx) {
    //Calculate median minimum distance (aka nearest neighbor distance) for all cells.
    EuclideanDistance d = new EuclideanDistance();
    double[] minDistances = new double[mtx.length];
    for (int i=0;i<mtx.length;i++){
      double minDist = Double.MAX_VALUE;
      for (int j=0;j<mtx.length;j++){
        if (i!=j){
          double dist = d.compute(mtx[i], mtx[j]);
          minDistances[i] = dist<minDist ? dist:minDist;
        } 
      }
    }
    return (new Median()).evaluate(minDistances);
  }

  private static double[] calculateLocalDensity(FCSFrame inFrame, double[][] mtx,
      double minimumMedianDistance, List<String> dimensionNames, TransformSet transforms) {
    //Calculate the number of local neighbors for each cell.
    double dThresh = minimumMedianDistance * 5; //alpha
    double[] localDensity = new double[inFrame.getRowCount()];
    double[][] allData = inFrame.getMatrix(dimensionNames);//TODO: transform.
    FCSUtilities.transformMatrix(dimensionNames, transforms, allData);
    allData = MatrixUtilities.transpose(allData);
    for (int k=0;k<localDensity.length;k++){
      for (int l=0;l<mtx.length;l++){
        double[] row1 = allData[k];
        double[] row2 = mtx[l];
        double cellDistance = manhattan(row1, row2);
        if (cellDistance < dThresh){
          double nDensity = localDensity[k]+1;
          localDensity[k] = nDensity;
        }
        
      }
    }
    return localDensity;
  }

  private static double manhattan(double[] row1, double[] row2) {
    double distance = 0;
    for (int i=0;i<row1.length;i++){
      double delta = Math.abs(row1[i] - row2[i]);
      distance+=delta;
    }
    return distance;
  }

  private static BitSet generateBitSet(FCSFrame inFrame, double[] localDensity, int targetSize) {
    Percentile p = new Percentile();
    p.setData(localDensity);
    double od = p.evaluate(1);
    double td = p.evaluate(3);
    double defaultProbablity;
    //TODO: Bullshit I made up to limit rows in common subset, I can't algebra :(
    if (targetSize < inFrame.getRowCount()*0.98){
        defaultProbablity = targetSize/inFrame.getRowCount()*0.98;
    } else {
    	defaultProbablity = 1;
    }

    Random rnJesus = new Random();
    BitSet outMask = new BitSet(inFrame.getRowCount());
    for (int index=0;index<localDensity.length;index++){
      if (localDensity[index] <= od){
        //Don't set the bit. 
      } else if (od < localDensity[index] && localDensity[index] <= td){
        outMask.set(index);
      } else {
		double probablity = td/localDensity[index]*defaultProbablity;
        if (probablity > rnJesus.nextDouble()){
          outMask.set(index);
        }
      }
    }
    return outMask;
  }
}
