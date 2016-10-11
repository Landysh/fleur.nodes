package io.landysh.inflor.java.core.plots.gateui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JToolBar;

import io.landysh.inflor.java.core.plots.FCSChartPanel;

@SuppressWarnings("serial")
public class GateCreationToolBar extends JToolBar {
    private static final String TOOLBAR_TITLE = "Mouse Mode";
	/**
     * This toolbar is responsible for managing the active listener (gate drawing/zooming mode)
     * on a JFreeChart ChartPanel. 
     */
	
	private ArrayList<JButton> cursorButtons;
	MouseListener activeListener;
	private FCSChartPanel panel;
	private JButton selectButton;
	private JButton rectGateButton;
	private JButton polyGateButton;
	
	public GateCreationToolBar(FCSChartPanel panel){
		super(TOOLBAR_TITLE);
		this.panel = panel;
		
		cursorButtons = new ArrayList<JButton>();
		
		selectButton   = createSelectButton();
	    rectGateButton = createRectGateButton();
	    polyGateButton = createPolyGateButton();
	    
	    cursorButtons = new ArrayList<JButton>();
	    cursorButtons.add(selectButton);
	    cursorButtons.add(rectGateButton);
	    cursorButtons.add(polyGateButton);    
	    cursorButtons.forEach(button-> this.add(button));
	}

	private JButton createSelectButton() {
		selectButton = new JButton("Select");
		selectButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (activeListener!=null){
					panel.removeMouseListener(activeListener);
					panel.removeMouseMotionListener((MouseMotionListener) activeListener);
				}
				cursorButtons.forEach(button -> button.setEnabled(true));
				((JButton)arg0.getSource()).setEnabled(false);
				GateSelectionAdapter gsa = new GateSelectionAdapter(panel);
				panel.addMouseListener(gsa);
				panel.addMouseMotionListener(gsa);
			}
		});	
		return selectButton;
	}

	private JButton createRectGateButton() {
	    rectGateButton = new JButton("RectGate");
	    rectGateButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//Remove current listener.
				if (activeListener!=null){
					panel.removeMouseListener(activeListener);
					panel.removeMouseMotionListener((MouseMotionListener) activeListener);
				}
				//Set button states.
				cursorButtons.forEach(button -> button.setEnabled(true));
				((JButton)arg0.getSource()).setEnabled(false);
				//Create new rect gate listener.
				activeListener = new RectangleGateAdapter(panel);
				panel.addMouseListener(activeListener);
				panel.addMouseMotionListener((MouseMotionListener) activeListener);
			}
		});
		return rectGateButton;
	}
	
	private JButton createPolyGateButton() {
	    polyGateButton = new JButton("Polygon");
	    polyGateButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//Remove current listener.
				if (activeListener!=null){
					panel.removeMouseListener(activeListener);
					panel.removeMouseMotionListener((MouseMotionListener) activeListener);
				}
				//Set button states.
				cursorButtons.forEach(button -> button.setEnabled(true));
				((JButton)arg0.getSource()).setEnabled(false);
				//Create new rect gate listener.
				activeListener = new PolygonGateAdapter(panel);
				panel.addMouseListener(activeListener);
				panel.addMouseMotionListener((MouseMotionListener) activeListener);
			}
		});
		return polyGateButton;
	}
}
