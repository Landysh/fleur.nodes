package inflor.core.regressiontree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import inflor.core.regressiontree.RegressionTreeNode.LeafStats;

public class MultiTargetRegressionTree {

  static final int DEFAULT_MINIMUM_SPLIT_SIZE = 100;
  static final int DEFAULT_MINIMUM_NODE_SIZE = 50;

  RegressionTreeNode root = null;
  final Map<String, double[]> X;
  String[] xNames;
  final Map<String, double[]> Y;
  String[] yNames;
  boolean finished = false;
  List<RegressionTreeNode> nodesToSplit = new ArrayList<>();

  public MultiTargetRegressionTree(Map<String, double[]> x, Map<String, double[]> y) {
    X = x;
    xNames = x.keySet().toArray(new String[x.size()]);
    Y = y;
    yNames = y.keySet().toArray(new String[y.size()]);
  }

  public boolean learn() {
    RegressionTreeNode currentNode = null;
    if (root == null) {
      Optional<Integer> size =
          X.entrySet().stream().map(e -> e.getValue().length).reduce((a, b) -> a > b ? a : b);
     if (size.isPresent()){
       root = new RegressionTreeNode(this, null, X,Y);
       currentNode = root;       
     } else {
       throw new RuntimeException("Unable to find data size");
     }

    } else {
      Optional<RegressionTreeNode> nextNode = nodesToSplit.stream().findAny();
      if (nextNode.isPresent()) {
        currentNode = nextNode.get();
        nodesToSplit.remove(currentNode);
      } else {
        finished = true;
      }
    }
    if (currentNode != null) {
      boolean splitable = currentNode.trySplit();
      if (splitable) {
        nodesToSplit.add(currentNode.left());
        nodesToSplit.add(currentNode.right());
      }
    } else {
      return true;//Is this hit? 
    }

    finished = nodesToSplit.size() == 0 ? true : false;
    return finished;
  }

  public LeafStats predict(double[] x) {
    return root.predict(x);
  }

  Map<String, double[]> getX() {
    return X;
  }

  Map<String, double[]> getY() {
    return Y;
  }
  
  public int findXIndex(String mSplitDimension) {
    int index = -1;
    for (int i = 0;i<xNames.length;i++){
      if (mSplitDimension.equals(xNames[i])){
        index = i;
      }
    }
    if (!(index==-1)){
      return index;
    } else {
      throw new RuntimeException("Index not found");
    }
  }

  public int minSplitSize() {
    return MultiTargetRegressionTree.DEFAULT_MINIMUM_SPLIT_SIZE;
  }

  public int minNodeSize() {
    return MultiTargetRegressionTree.DEFAULT_MINIMUM_NODE_SIZE;
  }
}
