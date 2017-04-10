package inflor.core.regressiontree;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import inflor.core.utils.BitSetUtils;

@SuppressWarnings("serial")
public class RegressionTreeNode extends DefaultMutableTreeNode {
  // Tree references
  private MultiTargetRegressionTree mTree;
  private RegressionTreeNode mLeft;
  private RegressionTreeNode mRight;

  // Node Summary
  private Map<String, double[]> X;
  private Map<String, double[]> Y;
  
  // Best split
  private String mSplitDimension;
  private double mSplitValue;
  private double mSplitScore;
  private double mVariance;
  // Leaf stats
  private LeafStats leafStats;


  public class SplitResults {
    final String mDimensionName;
    final double mDimensionValue;
    final double mSplitScore;
    private Map<String, double[]> mLeftX;
    private Map<String, double[]> mLeftY;
    private Map<String, double[]> mRightX;
    private Map<String, double[]> mRightY;
    public double mLeftVariance;
    public double mRighVariance;
    
    public SplitResults(String name, double splitValue, double splitScore, double leftVariance, double rightVariance, Map<String, double[]>lx, Map<String, double[]>ly, Map<String, double[]>rx, Map<String, double[]>ry) {
      mDimensionName = name;
      mDimensionValue = splitValue;
      mSplitScore = splitScore;
      mLeftVariance = leftVariance;
      mRighVariance = rightVariance;
      mLeftX = lx;
      mLeftY = ly;
      mRightX = rx;
      mRightY = ry;
    }

  }
  public class LeafStats {

    public static final int INDEX_MEAN = 0;
    public static final int INDEX_STDEV = 1;

    public final Map<String, double[]> xStats;
    public final Map<String, double[]> yStats;

    public LeafStats(Map<String, double[]> xStats, Map<String, double[]> yStats) {
      this.yStats = yStats;
      this.xStats = xStats;
    }
  }



  public RegressionTreeNode(MultiTargetRegressionTree tree, Double variance, Map<String, double[]>x, Map<String, double[]> y) {
    mTree = tree;
    X = x;
    Y = y;
    if (variance==null){
      mVariance = calculateVariance(Y);
    } else {
      mVariance = variance.doubleValue();
    }
  }

  public boolean trySplit() {
    boolean isSplittable = false;
    int size = X.entrySet().stream().map(Entry::getValue).map(x -> x.length).findAny().get();
    if (size > mTree.minSplitSize()) {

      Optional<SplitResults> oResult = X.entrySet().parallelStream()
          .map(e -> testDimension(e, Y)).filter(o -> o.isPresent()).map(Optional::get)
          .reduce((a, b) -> a.mSplitScore > b.mSplitScore ? a : b);

      if (oResult.isPresent()){
        SplitResults result = oResult.get();    
        int  leftSize = result.mLeftY.entrySet().stream().map(e -> e.getValue().length).findAny().get();
        int  rightSize = result.mRightY.entrySet().stream().map(e -> e.getValue().length).findAny().get();

        if (leftSize>mTree.minSplitSize()&&rightSize> mTree.minSplitSize()&&result.mSplitScore > 0){
          mSplitDimension = result.mDimensionName;
          mSplitValue = result.mDimensionValue;
          mSplitScore = result.mSplitScore;
          System.out.println(mSplitDimension + ": " + mSplitValue + "  " + mSplitScore);
          isSplittable = true;
          RegressionTreeNode left = new RegressionTreeNode(mTree, result.mLeftVariance, result.mLeftX, result.mLeftY);
          RegressionTreeNode right = new RegressionTreeNode(mTree, result.mRighVariance, result.mRightX, result.mRightY);
          this.add(right);
          this.add(left);
          mLeft = left;
          mRight = right;
          if (mRight==null){
            System.out.println("not splittable");
          }
        } else {
          leafStats = calculateLeafStats();
        }
      } else {
        leafStats = calculateLeafStats();
      }
      } else {
        leafStats = calculateLeafStats();
      }

    if (!isSplittable){
      System.out.println("not splittable");
    }
    return isSplittable; 
  }

  private LeafStats calculateLeafStats() {

    ConcurrentMap<String, double[]> xStats =
        mTree.getX().entrySet().parallelStream().map(this::calculateStats)
            .collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue));
    ConcurrentMap<String, double[]> yStats = mTree.getY().entrySet().parallelStream().map(this::calculateStats)
        .collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue));

    return new LeafStats(xStats, yStats);
  }

  private Entry<String, double[]> calculateStats(Entry<String, double[]> e) {
    Mean m = new Mean();
    StandardDeviation sd = new StandardDeviation();
    for (double d : e.getValue()) {
      m.increment(d);
      sd.increment(d);
    }
    HashMap<String, double[]> h = new HashMap<>();
    double mean = m.getResult();
    double d = sd.getResult();
    String k = e.getKey();
    h.put(k, new double[] {mean, d});
    return h.entrySet().stream().findAny().get();
  }

  Optional<SplitResults> testDimension(Entry<String, double[]> x, Map<String, double[]> y) {
    SplitResults bestResults = null;
    Double bestScore = Double.MAX_VALUE;
    double[] xSorted = x.getValue().clone();
    Arrays.sort(xSorted);
    int minSize = mTree.minNodeSize();
    double priorSplitValue = Double.MAX_VALUE;
    for (int splitIndex = minSize; splitIndex < xSorted.length - minSize; splitIndex++) {
      double splitValue = xSorted[splitIndex];
      if (priorSplitValue != splitValue) {
        Optional<SplitResults> currentResults = score(splitValue, x.getKey(), x.getValue(), y);
        if (currentResults.isPresent()) {
          double currentScore = currentResults.get().mSplitScore;
          if (bestResults == null || currentScore > bestScore) {
            bestResults = currentResults.get();
            bestScore = currentScore;
          }
        }

      }
    }
    return Optional.ofNullable(bestResults);
  }

  /**
   * Calculate the score for a given split value.
   * 
   * @param splitValue
   * @param e
   * @param y2
   * @return a SplitStatistics instance describing the results of the split.
   */
  private Optional<SplitResults> score(double splitValue, String key, double[] x,
      Map<String, double[]> y2) {

    BitSet mask = new BitSet(x.length);
    for (int i = 0; i < x.length; i++) {
      if (x[i] <= splitValue) {
        mask.set(i);
      } 
    }
    if (mask.cardinality() > mTree.minNodeSize()&& mask.size()-mask.cardinality() > mTree.minNodeSize()) {
      
      Map<String, Map<String, double[]>> xPart = BitSetUtils.partition(X, mask);
      Map<String, Map<String, double[]>> yPart = BitSetUtils.partition(Y, mask);
      
      double leftVariance = calculateVariance(yPart.get(BitSetUtils.IN));
      double rightVariance = calculateVariance(yPart.get(BitSetUtils.OUT));
      double splitScore = mVariance - (leftVariance + rightVariance);
      return Optional.of(new SplitResults(key, splitValue, splitScore, leftVariance, rightVariance, xPart.get(BitSetUtils.IN), yPart.get(BitSetUtils.IN), xPart.get(BitSetUtils.OUT), yPart.get(BitSetUtils.OUT)));
    } else {
      return Optional.ofNullable(null);
    }
  }

  private double calculateVariance(Map<String, double[]> map) {
    double result = 0;
    for (Entry<String, double[]> e : map.entrySet()) {
      double[] s = e.getValue();
      StandardDeviation sd = new StandardDeviation();
      double stdev = sd.evaluate(s);
      result+=stdev*s.length;
    }
    return result;
  }

  public RegressionTreeNode left() {
    return mLeft;
  }

  public RegressionTreeNode right() {
    return mRight;
  }

  public LeafStats predict(double[] x) {
    if (leafStats != null) {
      return leafStats;
    } else if (mLeft != null && mRight != null) {
      RegressionTreeNode child = evaluate(x) ? mLeft : mRight;
      return child.predict(x);
    } else {
      throw new RuntimeException("Invalid Tree");
    }
  }

  private boolean evaluate(double[] x) {
    int index = mTree.findXIndex(mSplitDimension);
    return x[index] < mSplitValue ? true : false;
  }
}
