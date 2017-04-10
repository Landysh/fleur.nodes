package inflor.knime.nodes.treesne;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class TreeSNENodeSettings {
  
  final String TSNE_1    = "TSNE1";
  final String TSNE_2   = "TSNE2";
  final String TREESNE_1 = "TreeSNE1";
  final String TREESNE_2 = "TreeSNE2";

  
////Data Settings
  //FCS Column
  final String KEY_SELECTED_COLUMN = "FCS Column";
  final String DEFAULT_COLUMN_SELECTION = "Not Selected";
  private String mSelectedColumn = DEFAULT_COLUMN_SELECTION;
  //FCS Dimensions
  final String KEY_SELECTED_DIMENSIONS = "SelectedDimensions";
  final String[] DEFAULT_SELECTED_DIMENSIONS = new String[]{"Default"};
  private String[] mSelectedDimension = DEFAULT_SELECTED_DIMENSIONS;
  
////TSNE Settings
  //Iterations
  final String KEY_ITERATIONS = "Iterations";
  final Integer MIN_ITERATIONS = 10;
  final Integer MAX_ITERATIONS = 5000;
  final Integer DEFAULT_ITERATIONS = 500;
  private Integer mMaxIterations = DEFAULT_ITERATIONS;
  // PCA Dims
  final String KEY_PCA_DIMS = "PCA Dimensions";
  final Integer MIN_PCA_DIMS = 0;
  final Integer MAX_PCA_DIMS = 100;
  final Integer DEFAULT_PCA_DIMS = 10;
  private Integer mPCADims = DEFAULT_PCA_DIMS;
  // Perplexity
  final String KEY_PERPLEXITY = "Maximum iterations";
  final Integer MIN_PERPLEXITY = 1;
  final Integer MAX_PERPLEXITY = 100;
  final Integer DEFAULT_PERPLEXITY = 40;
  private Integer mPerplexity = DEFAULT_PERPLEXITY;

  //Max observations (max rowcount for tsne training).
  final String KEY_MAX_OBSERVATIONS= "Max Observations";
  final Integer MIN_OBSERVATIONS = 10;
  final Integer MAX_OBSERVATIONS = Integer.MAX_VALUE;
  final Integer DEFAULT_OBSERVATIONS = 10000;
  private Integer mObservations = DEFAULT_OBSERVATIONS;

  
////TreeSNE Settings

  public void save(NodeSettingsWO settings) {
    //Data Settings
    settings.addString(KEY_SELECTED_COLUMN, mSelectedColumn);
    settings.addStringArray(KEY_SELECTED_DIMENSIONS, mSelectedDimension);
    //TSNE Settings
    settings.addInt(KEY_ITERATIONS, mMaxIterations);
    settings.addInt(KEY_PCA_DIMS, mPCADims);
    settings.addInt(KEY_PERPLEXITY, mPerplexity);
    settings.addInt(KEY_MAX_OBSERVATIONS, mObservations);
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
    //Data Settings
    mSelectedColumn = settings.getString(KEY_SELECTED_COLUMN);
    mSelectedDimension = settings.getStringArray(KEY_SELECTED_DIMENSIONS);
    //TSNE Settings
    mMaxIterations = settings.getInt(KEY_ITERATIONS);
    mPCADims = settings.getInt(KEY_PCA_DIMS);
    mPerplexity = settings.getInt(KEY_PERPLEXITY);
    mObservations = settings.getInt(KEY_MAX_OBSERVATIONS);
    
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

  public Integer getPerplexity() {
    return mPerplexity;
  }

  public void setPerplexity(Integer mPerplexity) {
    this.mPerplexity = mPerplexity;
  }

  public Integer getMaxObservations() {
    return mObservations;
  }
}
