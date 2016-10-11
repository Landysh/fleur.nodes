package io.landysh.inflor.java.core.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JToolBar;

import io.landysh.inflor.java.core.plots.FCSChartPanel;
import io.landysh.inflor.java.core.plots.gateui.GateSelectionAdapter;
import io.landysh.inflor.java.core.plots.gateui.RectangleGateAdapter;

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
	
	public GateCreationToolBar(FCSChartPanel panel){
		super(TOOLBAR_TITLE);
		this.panel = panel;
		
		cursorButtons = new ArrayList<JButton>();
		
		JButton zoomButton = createSelectButton();
	    JButton rectGateButton = createRectGateButton();
	    
	    cursorButtons = new ArrayList<JButton>();
	    cursorButtons.add(zoomButton);
	    cursorButtons.add(rectGateButton);    
	    cursorButtons.forEach(button-> this.add(button));
	}

	private JButton createSelectButton() {
		JButton button = new JButton("Select");
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (activeListener!=null){
					panel.removeMouseListener(activeListener);
					panel.removeMouseMotionListener((MouseMotionListener) activeListener);
				}
				cursorButtons.forEach(button -> button.setEnabled(true));
				((JButton)arg0.getSource()).setEnabled(false);
				panel.addMouseListener(new GateSelectionAdapter(panel));
			}
		});	
		return button;
	}

	private JButton createRectGateButton() {
	    JButton button = new JButton("RectGate");
	    button.addActionListener(new ActionListener(){
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
		return button;
	}
}
