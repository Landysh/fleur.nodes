package inflor.core.regressiontree;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

@SuppressWarnings("serial")
public class RegressionTreeNode extends DefaultMutableTreeNode {

  private static final String LEFT = "left";
  private static final String RIGHT = "right";
  private RegressionTreeNode parent;
  private RegressionTreeNode left;
  private RegressionTreeNode right;
  private BitSet parentMask;
  private SplitStatistics stats;
  private MultiTargetRegressionTree tree;
  int depth;

  class SplitStatistics {
    final String dimensionName;
    final double dimensionValue;
    final BitSet mask;
    Map<String, Double> results;

    public SplitStatistics(String name, double splitValue, BitSet rows,
        Map<String, Double> resultMap) {
      dimensionName = name;
      dimensionValue = splitValue;
      mask = rows;
      results = resultMap;
    }

    Optional<Double> sumVariances() {
      return results.entrySet().stream().map(Entry::getValue).reduce((a,b) -> a+b);
    }

    public BitSet getMask() {
      return mask;
    }

    public double getSplitValue() {
      return dimensionValue;
    }
  }

  public RegressionTreeNode(MultiTargetRegressionTree tree, BitSet mask, SplitStatistics newStats, RegressionTreeNode nodeParent) {
    this.tree = tree;
    parent = nodeParent;
    //Set all the bits if root.
    if (nodeParent==null);{
      mask.set(0,  mask.cardinality()-1);
    }
    parentMask = mask;
    stats = newStats;
    depth = findDepth(); 
  }

  public RegressionTreeNode(MultiTargetRegressionTree multiTargetRegressionTree, Integer size) {
    this(multiTargetRegressionTree, new BitSet(size), null, null);
  }

  protected int findDepth() {
    if (this.parent==null){//Root Case
      return 0;      
    } else {
      return parent.findDepth() + 1;
    }
  }

  public boolean trySplit() {
    boolean isSplittable = false;
    Optional<Integer> size = tree.getX().entrySet().stream().map(e -> e.getValue().length)
        .reduce((a, b) -> a > b ? a : b);
    if (size.isPresent() && size.get() > MultiTargetRegressionTree.DEFAULT_MINIMUM_SPLIT_SIZE) {

      Optional<Map<String, SplitStatistics>> possibleResult =
          tree.getX().entrySet().parallelStream().map(e -> testDimension(e, tree.getY()))
              .reduce((a, b) -> lowerVariance(a, b));

      if (possibleResult.isPresent()) {
        Map<String, SplitStatistics> result = possibleResult.get();
        // create left and right
        left = new RegressionTreeNode(tree, result.get(LEFT).getMask(), result.get(LEFT), this);
        right = new RegressionTreeNode(tree, result.get(RIGHT).getMask(), result.get(RIGHT), this);
        isSplittable = true;
      } else {
        calculateLeafStats();
      }
    }
    return isSplittable;
  }

  private void calculateLeafStats() {
    // TODO Auto-generated method stub
    
  }

  private Map<String, SplitStatistics> lowerVariance(Map<String, SplitStatistics> a,
      Map<String, SplitStatistics> b) {
    Optional<Double> aVar = 
        a.entrySet().stream().map(e -> e.getValue().sumVariances()).filter(Optional::isPresent).map(Optional::get).reduce((c, d) -> c + d);
    Optional<Double> bVar =
        b.entrySet().stream().map(e -> e.getValue().sumVariances()).filter(Optional::isPresent).map(Optional::get).reduce((c, d) -> c + d);
    
    if(bVar.isPresent()&&aVar.isPresent()){
      return aVar.get() < bVar.get() ? a : b;

    } else if(aVar.isPresent()){
      return a;
    } else if(bVar.isPresent()){
      return b;
    } else {
      throw new RuntimeException("failed to find a split");
    }
    
  }

  Map<String, SplitStatistics> testDimension(Entry<String, double[]> x, Map<String, double[]> y) {
    Map<String, SplitStatistics> bestResults = null;
    Double bestCombinedVariance = Double.MAX_VALUE;
    double[] xSorted = x.getValue().clone();
    Arrays.sort(xSorted);
    for (int splitIndex = 1; splitIndex < xSorted.length - 1; splitIndex++) {
      double splitValue = xSorted[splitIndex];
      Map<String, SplitStatistics> currentResults = score(splitValue, x, y);
      // Check the left and right variance
      Optional<Double> localCombinedVariance = currentResults.entrySet().stream()
          .map(e -> e.getValue().sumVariances()).filter(opt -> opt.isPresent()).map(Optional::get).reduce((a, b) -> a + b);
      if (localCombinedVariance.isPresent()&&bestResults == null || 
          (localCombinedVariance.isPresent()&&localCombinedVariance.get() < bestCombinedVariance)) {
        bestResults = currentResults;
        bestCombinedVariance = localCombinedVariance.get();
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
    String[] yKeys = y2.keySet().toArray(new String[y2.size()]);
    double[][] leftParameters = new double[yKeys.length][2];
    double[][] rightParameters = new double[yKeys.length][2];

    //Initialize results. 
    Map<String, Mean> lMeans = new HashMap<>();
    Map<String, Double> lScore = new HashMap<>();
    Map<String, Mean> rMeans = new HashMap<>();
    Map<String, Double> rScore = new HashMap<>();
    for (int i = 0; i < yKeys.length; i++) {
      lMeans.put(yKeys[i], new Mean());
      rMeans.put(yKeys[i], new Mean());
      lScore.put(yKeys[i], Double.MAX_VALUE);
      rScore.put(yKeys[i], Double.MAX_VALUE);
    }
    
    for (int i = 0; i < x.length; i++) {
      
      if (x[i] < splitValue && parentMask.get(i)) {
        left.set(i);
        for (int j = 0; j < yKeys.length; j++) {          
          double value = x[i];
          Mean mean = lMeans.get(yKeys[i]);
          double increment = calculateObjective(value, mean);
          lScore.put(yKeys[i], lScore.get(yKeys[i])+increment);
        }
      } else if (parentMask.get(i)) {
        right.set(i);
        for (int j = 0; j < yKeys.length; j++) {
          double value = x[i];
          Mean mean = rMeans.get(yKeys[i]);
          double increment = calculateObjective(value, mean);
          rScore.put(yKeys[i], rScore.get(yKeys[i])+increment);
        }
      } else {
        //NOOP
      }
    }

    Map<String, double[]> leftResultMap = new HashMap<>();
    Map<String, double[]> rightResultMap = new HashMap<>();

    for (int i = 0; i < yKeys.length; i++) {

      double leftMean = leftParameters[i][0];
      double leftVariance = estimateVariance(leftParameters[i][1], left.cardinality());
      leftResultMap.put(yKeys[i], new double[] {leftMean, leftVariance});

      double rightMean = rightParameters[i][0];
      double rightVariance = estimateVariance(rightParameters[i][1], right.cardinality());
      rightResultMap.put(yKeys[i], new double[] {rightMean, rightVariance});
    }

    SplitStatistics leftResults = new SplitStatistics(e.getKey(), splitValue, left, lScore);
    SplitStatistics rightResults = new SplitStatistics(e.getKey(), splitValue, right, rScore);
    results.put(LEFT, leftResults);
    results.put(RIGHT, rightResults);

    return results;
  }

  private double calculateObjective(double value, Mean mean) {
    mean.increment(value);
    double meanVal = mean.evaluate();
    double increment = Math.abs(meanVal-value);
    return increment;
  }

  private double estimateVariance(double s, int n) {
    return Math.sqrt(s / (n - 1));
  }

  public RegressionTreeNode getLeftNode() {
    return left;
  }

  public RegressionTreeNode getRightNode() {
    return right;
  }

  public SplitStatistics predict(double[] x) {
    if (isLeaf()){
      return stats;
    } else {
      RegressionTreeNode child = evaluate() ? left:right;
      return child.predict(x);
    }
  }

  private boolean evaluate() {
      return false;
  }
}
