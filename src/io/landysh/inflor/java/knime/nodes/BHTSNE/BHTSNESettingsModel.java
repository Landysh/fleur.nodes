package io.landysh.inflor.java.knime.nodes.BHTSNE;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

public class BHTSNESettingsModel {

  // Max Iterations
  static final String CFGKEY_Iterations = "Iterations";

  static final Integer DEFAULT_Iterations = 250;
  static final Integer Iterations_MIN = 1;
  static final Integer Iterations_MAX = 10000;

  // Perplexity
  static final String CFGKEY_Perplexity = "Perplexity";

  static final Integer DEFAULT_Perplexity = 40;

  static final Integer Perplexity_MIN = 1;
  static final Integer Perplexity_MAX = 1000;
  // Input Features
  static final String CFGKEY_Features = "Input Features";

  static final String[] DEFAULT_Features = {"No Features!"};

  private final SettingsModelIntegerBounded m_Iterations = new SettingsModelIntegerBounded(
      CFGKEY_Iterations, DEFAULT_Iterations, Iterations_MIN, Iterations_MAX);

  private final SettingsModelIntegerBounded m_Perplexity = new SettingsModelIntegerBounded(
      CFGKEY_Perplexity, DEFAULT_Perplexity, Perplexity_MIN, Perplexity_MAX);

  private final SettingsModelStringArray m_Features =
      new SettingsModelStringArray(CFGKEY_Features, DEFAULT_Features);

  public String[] getFeatures() {
    return m_Features.getStringArrayValue();
  }

  public SettingsModelStringArray getFeaturesModel() {
    return m_Features;
  }

  public int getFinalDimensions() {
    return 2;
  }

  public Integer getIterations() {
    return m_Iterations.getIntValue();
  }

  public SettingsModelIntegerBounded getIterationsModel() {
    return m_Iterations;
  }

  public int getPCADims() {
    return 5;
  }

  public Integer getPerplexity() {
    return m_Perplexity.getIntValue();
  }

  public SettingsModelIntegerBounded getPerplexityModel() {
    return m_Perplexity;
  }

  public void load(NodeSettingsRO settings) throws InvalidSettingsException {
    m_Iterations.loadSettingsFrom(settings);
    m_Perplexity.loadSettingsFrom(settings);
    m_Features.loadSettingsFrom(settings);
  }

  public void save(NodeSettingsWO settings) {
    m_Iterations.saveSettingsTo(settings);
    m_Perplexity.saveSettingsTo(settings);
    m_Features.saveSettingsTo(settings);
  }

  public void validate(NodeSettingsRO settings) throws InvalidSettingsException {
    m_Iterations.validateSettings(settings);
    m_Perplexity.validateSettings(settings);
    m_Features.validateSettings(settings);
  }
};
