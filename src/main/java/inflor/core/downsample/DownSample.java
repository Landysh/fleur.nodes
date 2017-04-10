package inflor.core.downsample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
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
    BitSet shuffleMask = BitSetUtils.getShuffledMask(inFrame.getRowCount(), targetSize);
    FCSFrame dsFrame = FCSUtilities.filterFrame(shuffleMask, inFrame);
    double[][] mtx = dsFrame.getMatrix(dimensionNames.toArray(new String[dimensionNames.size()]));
    MatrixUtilities.transformMatrix(dimensionNames.toArray(new String[dimensionNames.size()]), transforms, mtx);
    mtx = MatrixUtilities.transpose(mtx);
    double minimumMedianDistance = calculateMinMedDistance(mtx);
    //Calculate the number of local neighbors for each cell.
    double[] localDensity = calculateLocalDensity(inFrame, mtx, minimumMedianDistance, dimensionNames, transforms);
    //Finally calculate od, and td, and fill the mask.
    return generateBitSet(inFrame, localDensity, targetSize);
  }

  private static double calculateMinMedDistance(double[][] mtx) {
    //Calculate median minimum distance (aka nearest neighbor distance) for all cells.
    double[] minDistances = new double[mtx.length];
    for (int i=0;i<mtx.length;i++){
      double minDist = Double.MAX_VALUE;
      for (int j=0;j<mtx.length;j++){
        if (i!=j){
          double dist = manhattan(mtx[i], mtx[j]);
          minDistances[i] = dist<minDist ? dist:minDist;
        } 
      }
    }
    return (new Median()).evaluate(minDistances);
  }

  private static double[] calculateLocalDensity(FCSFrame inFrame, double[][] mtx,
      double minimumMedianDistance, List<String> dimensionNames, TransformSet transforms) {
    //Calculate the number of local neighbors for each cell.
    double dThreshs = minimumMedianDistance; //alpha
    double[] localDensity = new double[inFrame.getRowCount()];
    double[][] allData = inFrame.getMatrix(dimensionNames.toArray(new String[dimensionNames.size()]));
    MatrixUtilities.transformMatrix(dimensionNames.toArray(new String[dimensionNames.size()]), transforms, allData);
    allData = MatrixUtilities.transpose(allData);
    for (int k=0;k<localDensity.length;k++){
      for (int l=0;l<mtx.length;l++){
        double[] row1 = allData[k];
        double[] row2 = mtx[l];
        double cellDistance = manhattan(row1, row2);
        if (cellDistance < dThreshs){
          localDensity[k]+= 1;
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
    double td = calculateThreshold(od, targetSize, localDensity);
    Random rnJesus = new Random();
    BitSet outMask = new BitSet(inFrame.getRowCount());
    for (int index=0;index<localDensity.length;index++){
      if (localDensity[index] <= od){
        //Don't set the bit. 
      } else if (od < localDensity[index] && localDensity[index] <= td){
        outMask.set(index);
      } else {
		double probablity = td/localDensity[index];
        if (probablity > rnJesus.nextDouble()){
          outMask.set(index);
        }
      }
    }
    return outMask;
  }

  private static double calculateThreshold(double od, int targetSize, double[] localDensity) {
    double[] sortedLD = localDensity.clone();
    Arrays.sort(sortedLD);
    double td;
    int optIndex = sortedLD.length-1;
    double priorLD = Double.MIN_VALUE;  
    ArrayList<Integer> tds = new ArrayList<>();
    for (int i=0;i<sortedLD.length;i++){
      td = sortedLD[i];
      if (td!=priorLD){
        int tdSize = densitySplit(sortedLD, od, td);
        if (tdSize>targetSize){
          optIndex = i;
          break;
        }
        priorLD = td;
        tds.add(tdSize);
      }
    }
    return sortedLD[optIndex];
  }

//  private static double[] updateWindow(double[] window, int tdSize) {
//    double nextValue = tdSize;
//    double lag = Double.MAX_VALUE;
//    for (int i=0;i<window.length;i++){
//      lag = window[window.length-1-i];
//      window[window.length-1-i]=nextValue;
//      nextValue = lag;
//    }
//    return window;
//  }

  private static int densitySplit(double[] localDensity, double od, double td) {
    int size =0;
    Random rnJesus = new Random();
    for (int index=0;index<localDensity.length;index++){
      if (localDensity[index] <= od){
        //Don't set the bit. 
      } else if (od < localDensity[index] && localDensity[index] <= td){
        size++;
      } else {
        double probablity = td/localDensity[index];
        if (probablity > rnJesus.nextDouble()){
          size++;
        }
      }
    }
    return size;
  }
}