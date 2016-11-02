package io.landysh.inflor.java.knime.nodes.createGates.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import io.landysh.inflor.java.knime.nodes.createGates.CreateGatesNodeDialog;

public class SampleBoxListener implements ActionListener {

  private final CreateGatesNodeDialog dialog;

  public SampleBoxListener(CreateGatesNodeDialog dialog) {
    this.dialog = dialog;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final String newValue = (String) dialog.selectSampleBox.getModel().getSelectedItem();
    dialog.m_settings.setSelectedSample(newValue);
  }

}
