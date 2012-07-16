package calico.plugins.analysis.components.buttons;

import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import calico.plugins.analysis.controllers.ADAnalysisController;

public class JSliderOnJOptionPane {

	public static JSlider getSliderActivity(final JOptionPane optionPane, int initialValue) {
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 1000, initialValue);
		optionPane.setInputValue(initialValue);
		
		slider.setMajorTickSpacing(100);
		slider.setPaintTicks(true);
		//slider.setPaintLabels(true);
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JSlider theSlider = (JSlider) changeEvent.getSource();
				if (!theSlider.getValueIsAdjusting()) {
					optionPane.setInputValue(new Integer(theSlider.getValue()));
				}
			}
		};
		slider.addChangeListener(changeListener);
		return slider;
	}
	
	public static JSlider getSliderAnalysis(final JOptionPane optionPane) {
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 1000, (int) ADAnalysisController.CURRDISTANCE);
		
		slider.setMajorTickSpacing(100);
		slider.setPaintTicks(true);
		//slider.setPaintLabels(true);
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JSlider theSlider = (JSlider) changeEvent.getSource();
				if (!theSlider.getValueIsAdjusting()) {
					optionPane.setInputValue(new Integer(theSlider.getValue()));
				}
			}
		};
		slider.addChangeListener(changeListener);
		return slider;
	}
		
	
	public static JSlider getSliderDecision(final JOptionPane optionPane, int initialValue) {
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, initialValue);
		optionPane.setInputValue((double)initialValue/100);
		
		slider.setMajorTickSpacing(10);
		slider.setPaintTicks(true);
		//slider.setPaintLabels(true);
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JSlider theSlider = (JSlider) changeEvent.getSource();
				if (!theSlider.getValueIsAdjusting()) {
					optionPane.setInputValue(new Double((double)theSlider.getValue()/100));
				}
			}
		};
		slider.addChangeListener(changeListener);
		return slider;
	}

}
