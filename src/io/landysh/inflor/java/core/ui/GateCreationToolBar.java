package io.landysh.inflor.java.core.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JToolBar;

import io.landysh.inflor.java.core.plots.FCSChartPanel;
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
	
	public GateCreationToolBar(){
		super(TOOLBAR_TITLE);
		
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
				cursorButtons.forEach(button -> button.setEnabled(true));
				((JButton)arg0.getSource()).setEnabled(false);
				panel.removeMouseListener(activeListener);
				panel.removeMouseMotionListener((MouseMotionListener) activeListener);
			}
		});	
		return button;
	}

	private JButton createRectGateButton() {
	    JButton button = new JButton("RectGate");
	    button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//Set button states.
				cursorButtons.forEach(button -> button.setEnabled(true));
				((JButton)arg0.getSource()).setEnabled(false);
				
				//Remove current listener.
				panel.removeMouseListener(activeListener);
				panel.removeMouseMotionListener((MouseMotionListener) activeListener);
				
				//Create new rect gate listener.
				activeListener = new RectangleGateAdapter(panel);
				panel.addMouseListener(activeListener);
				panel.addMouseMotionListener((MouseMotionListener) activeListener);
			}
		});
		return button;
	}
}
