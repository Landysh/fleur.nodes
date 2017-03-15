package inflor.core.regressiontree;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class RegressionTreeNode extends DefaultMutableTreeNode {
  
  private static final String LEFT = "left";
  private static final String RIGHT = "right";
  BitSet parent;
  SplitStatistics stats;
  RegressionTreeNode left;
  RegressionTreeNode right;
  private MultiTargetRegressionTree tree;
  
  class SplitStatistics {
    final String dimensionName;
    final double dimensionValue;
    final BitSet mask;
    Map<String, double[]> results;

    public SplitStatistics(String name, double splitValue, BitSet rows,
        Map<String, double[]> resultMap) {
      dimensionName = name;
      dimensionValue = splitValue;
      mask = rows;
      results = resultMap; 
    }

    Double sumVariances(){
      double variance = 0;
      for (Entry<String, double[]> e:results.entrySet()){
        variance+=e.getValue()[1];
      }      
      return variance;
    }

    public BitSet getMask() {
      return mask;
    }

    public double getSplitValue() {
      return dimensionValue;
    }    
  }

  public RegressionTreeNode(MultiTargetRegressionTree tree, BitSet mask, SplitStatistics newStats){
    this.tree = tree;
    parent = mask;
    stats = newStats;
  }
  public boolean trySplit() {
    boolean isSplittable = false;
    
    Optional<Integer> size = tree.getX().entrySet().stream().map(e -> e.getValue().length).reduce((a,b) -> a>b?a:b);
    if (size.isPresent()&& size.get() > MultiTargetRegressionTree.DEFAULT_MINIMUM_SPLIT_SIZE){
      
      Optional<Map<String, SplitStatistics>> possibleResult = tree.getX().entrySet()
        .parallelStream()
        .map(e -> testDimension(e, tree.getY()))
        .reduce((a,b) -> lowerVariance(a,b));
 
      if (possibleResult.isPresent()){
        isSplittable = true;
        //create left and right;
      }
    }
    return isSplittable;
  }

  private Map<String, SplitStatistics> lowerVariance(Map<String, SplitStatistics> a, Map<String, SplitStatistics> b) {
    Double aVar = a.entrySet().stream().map(e-> e.getValue().sumVariances()).reduce((c,d) -> c+d).get();
    Double bVar = b.entrySet().stream().map(e-> e.getValue().sumVariances()).reduce((c,d) -> d+d).get();
    return aVar<bVar?a:b;
  }
  Map<String, SplitStatistics> testDimension(Entry<String, double[]> x, Map<String, double[]> y){
    Map<String, SplitStatistics> bestResults = null;
    Double bestCombinedVariance = Double.MAX_VALUE;
    double[] xSorted = x.getValue().clone();
    Arrays.sort(xSorted);
    for (int splitIndex =1;splitIndex<xSorted.length -1;splitIndex++){
      double splitValue = xSorted[splitIndex];
      Map<String, SplitStatistics> currentResults = score(splitValue, x, y);
      //Check the left and right variance
      Double localCombinedVariance = currentResults.entrySet().stream().map(e-> e.getValue().sumVariances()).reduce((a,b) -> a+b).get();
      if (bestResults==null|| localCombinedVariance < bestCombinedVariance){
        bestResults = currentResults;
        bestCombinedVariance = localCombinedVariance;
      }
    }
    return bestResults;
  }
  private Map<String, SplitStatistics> score(double splitValue, Entry<String, double[]> e, Map<String, double[]> y2) {
    /**
     * Based on welford's method. 
     */
    Map<String, SplitStatistics> results = new HashMap<>();
    double[] x = e.getValue();
    BitSet left = new BitSet(x.length);
    BitSet right = new BitSet(x.length);
    String[] keys = y2.keySet().toArray(new String[y2.size()]);
    double[][] leftParameters = new double[keys.length][2];
    double[][] rightParameters = new double[keys.length][2];

    double[][] yVectors = new double[keys.length][x.length];
    for (int i=0;i<keys.length;i++){
      yVectors[i] = y2.get(keys[i]);
      double m=0;
      double s=0;
      leftParameters[i] = new double[]{m, s};
      rightParameters[i] = new double[]{m, s};
    }
    
    for (int i=0;i<x.length;i++){
      if (x[i] < splitValue&&parent.get(i)){
        left.set(i);
        for (int j=0;j<keys.length;j++){
          double   m = leftParameters[j][0];
          double   s = leftParameters[j][1];
          double[] y = yVectors[j];
          leftParameters[j] = upateParameters(m,s,y, left.cardinality(), i);
        }
      } else if(parent.get(i)){
        right.set(i);
        for (int j=0;j<keys.length;j++){
          double   m = rightParameters[j][0];
          double   s = rightParameters[j][1];
          double[] y = yVectors[j];
          rightParameters[j] = upateParameters(m,s,y, right.cardinality(), i);
        }
      }
    }
    
    Map<String, double[]> leftResultMap = new HashMap<>();
    Map<String, double[]> rightResultMap = new HashMap<>();
    
    for (int i=0;i<keys.length;i++){
      
      double leftMean = leftParameters[i][0];
      double leftVariance = estimateVariance(leftParameters[i][1], left.cardinality());
      leftResultMap.put(keys[i], new double[]{leftMean, leftVariance});
      
      double rightMean = rightParameters[i][0];
      double rightVariance = estimateVariance(rightParameters[i][1], right.cardinality());
      rightResultMap.put(keys[i], new double[]{rightMean, rightVariance});
    }
    
    SplitStatistics leftResults = new SplitStatistics(e.getKey(), splitValue, left, leftResultMap);
    SplitStatistics rightResults = new SplitStatistics(e.getKey(), splitValue, right, rightResultMap);
    results.put(LEFT, leftResults);
    results.put(RIGHT, rightResults);
    
    return results;
  }
  private double estimateVariance(double s, int n) {
    return Math.sqrt(s/(n-1));
  }
  private double[] upateParameters(double m, double s, double[] y2, int n, int i) {
    double tempM = m;
    m = (y2[i] - tempM)/n;
    s = (y2[i] - tempM) * (y2[i]-m);
    return new double[]{m,s};
  }

  public SplitStatistics predict(double[] x) {
    if (this.isLeaf()){
      return stats;
    } else {
      String[] keys = tree.getX().keySet().toArray(new String[tree.getX().size()]);
      int targetIndex = -1;
      for (int i=0;i<keys.length;i++){
        if (keys[i].equals(stats.dimensionName)){
          targetIndex = i;
          break;
        }
      }
      double val = x[targetIndex];
      RegressionTreeNode node = val < stats.getSplitValue() ? left:right; ;
      return node.predict(x);
    }    
  }
  
  public RegressionTreeNode getLeftNode() {
    return left;
  }
  public RegressionTreeNode getRightNode() {
    return right;
  }  
}