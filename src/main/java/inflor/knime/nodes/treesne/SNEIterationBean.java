package inflor.knime.nodes.treesne;

public class SNEIterationBean {

  private final int iteration;
  private final double[][] data;

  public SNEIterationBean(int i, double[][] d) {
    iteration = i;
    data = d;
  }

  public int getIteration() {
    return iteration;
  }

  public double[][] getData() {
    return data;
  }
}
