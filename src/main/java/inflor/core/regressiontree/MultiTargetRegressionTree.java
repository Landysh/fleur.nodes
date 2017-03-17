package inflor.core.regressiontree;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import inflor.core.regressiontree.RegressionTreeNode.SplitStatistics;

public class MultiTargetRegressionTree {

  static final int DEFAULT_MINIMUM_SPLIT_SIZE = 20;
  static final int DEFAULT_MINIMUM_NODE_SIZE = 10;

  RegressionTreeNode root = null;
  final Map<String, double[]> X;
  final Map<String, double[]> Y;
  boolean finished = false;
  List<RegressionTreeNode> nodesToSplit = new ArrayList<>();

  public MultiTargetRegressionTree(Map<String, double[]> x, Map<String, double[]> y) {
    X = x;
    Y = y;
  }

  public boolean learn() {
    RegressionTreeNode currentNode = null;
    if (root == null) {
      Optional<Integer> size =
          X.entrySet().stream().map(e -> e.getValue().length).reduce((a, b) -> a > b ? a : b);
      BitSet mask = new BitSet(size.get());
      mask.set(0, mask.size() - 1);
      root = new RegressionTreeNode(this, mask, null);
      currentNode = root;
    } else {
      Optional<RegressionTreeNode> nextNode = nodesToSplit.stream().findAny();
      if (nextNode.isPresent()) {
        nodesToSplit.remove(nextNode.get());
        currentNode = nextNode.get();
      } else {
        finished = true;
      }
    }
    if (currentNode != null) {
      boolean splitable = currentNode.trySplit();
      if (splitable) {
        nodesToSplit.add(currentNode.getLeftNode());
        nodesToSplit.add(currentNode.getRightNode());
      }
    } else {
      return true;// TODO?
    }

    finished = nodesToSplit.size() == 0 ? true : false;
    return finished;
  }

  SplitStatistics predict(double[] x) {
    return root.predict(x);
  }

  Map<String, double[]> getX() {
    return X;
  }

  Map<String, double[]> getY() {
    return Y;
  }
}
