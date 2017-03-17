package inflor.knime.nodes.treesne;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class TreeSNENodeSettings {
  
  static final String TSNE_1    = "TSNE1";
  static final String TSNE_2   = "TSNE2";
  static final String TREESNE_1 = "TreeSNE1";
  static final String TREESNE_2 = "TreeSNE2";

  
////Data Settings
  //FCS Column
  static final String KEY_SELECTED_COLUMN = "FCS Column";
  static final String DEFAULT_COLUMN_SELECTION = "Not Selected";
  private String mSelectedColumn = DEFAULT_COLUMN_SELECTION;
  //FCS Dimensions
  static final String KEY_SELECTED_DIMENSIONS = "SelectedDimensions";
  static final String[] DEFAULT_SELECTED_DIMENSIONS = new String[]{"Default"};
  private String[] mSelectedDimension = DEFAULT_SELECTED_DIMENSIONS;
  
////TSNE Settings
  //Iterations
  static final String KEY_ITERATIONS = "Iterations";
  static final Integer MIN_ITERATIONS = 10;
  static final Integer MAX_ITERATIONS = 5000;
  static final Integer DEFAULT_ITERATIONS = 250;
  private Integer mMaxIterations = DEFAULT_ITERATIONS;
  // PCA Dims
  static final String KEY_PCA_DIMS = "PCA Dimensions";
  static final Integer MIN_PCA_DIMS = 0;
  static final Integer MAX_PCA_DIMS = 100;
  static final Integer DEFAULT_PCA_DIMS = 10;
  private Integer mPCADims = DEFAULT_PCA_DIMS;
  // Perplexity
  static final String KEY_PERPLEXITY = "Maximum iterations";
  static final Double MIN_PERPLEXITY = 1.;
  static final Double MAX_PERPLEXITY = 100.;
  static final Double DEFAULT_PERPLEXITY = 20.;
  private Double mPerplexity = DEFAULT_PERPLEXITY;
  
////TreeSNE Settings

  public void save(NodeSettingsWO settings) {
    //Data Settings
    settings.addString(KEY_SELECTED_COLUMN, mSelectedColumn);
    settings.addStringArray(KEY_SELECTED_DIMENSIONS, mSelectedDimension);
    //TSNE Settings
    settings.addInt(KEY_ITERATIONS, mMaxIterations);
    settings.addInt(KEY_PCA_DIMS, mPCADims);
    settings.addDouble(KEY_PERPLEXITY, mPerplexity);
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
    //Data Settings
    mSelectedColumn = settings.getString(KEY_SELECTED_COLUMN);
    mSelectedDimension = settings.getStringArray(KEY_SELECTED_DIMENSIONS);
    //TSNE Settings
    mMaxIterations = settings.getInt(KEY_ITERATIONS);
    mPCADims = settings.getInt(KEY_PCA_DIMS);
    mPerplexity = settings.getDouble(KEY_PERPLEXITY);
  }

  public String getSelectedColumn() {
    return mSelectedColumn;
  }

  public void setSelectedColumn(String mSelectedColumn) {
    this.mSelectedColumn = mSelectedColumn;
  }

  public String[] getSelectedDimension() {
    return mSelectedDimension;
  }

  public void setSelectedDimension(String[] mSelectedDimension) {
    this.mSelectedDimension = mSelectedDimension;
  }

  public Integer getMaxIterations() {
    return mMaxIterations;
  }

  public void setMaxIterations(Integer mMaxIterations) {
    this.mMaxIterations = mMaxIterations;
  }

  public Integer getPCADims() {
    return mPCADims;
  }

  public void setPCADims(Integer mPCADims) {
    this.mPCADims = mPCADims;
  }

  public Double getPerplexity() {
    return mPerplexity;
  }

  public void setPerplexity(Double mPerplexity) {
    this.mPerplexity = mPerplexity;
  }
}
