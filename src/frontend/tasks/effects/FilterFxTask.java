package frontend.tasks.effects;

import java.util.Arrays;
import java.util.List;

import controlP5.ControlP5;
import controlP5.Group;
import frontend.Main;
import frontend.core.State;
import frontend.tasks.Task;
import frontend.tasks.generators.OscillatorGenTask;
import frontend.ui.ComputableFloatTextfieldUI;
import frontend.ui.ComputableFloatTextfieldUIWithUserInput;
import frontend.ui.ScrollableListUI;
import processing.core.PApplet;
import soundengine.SoundEngine;

public class FilterFxTask  extends AbstractFxTask {

	protected static final List<String> list = Arrays.asList("Low Pass", "High Pass", "Band Pass");
	
	private ComputableFloatTextfieldUI centerFreq;
	private ComputableFloatTextfieldUI resonance;
	private ScrollableListUI type;
	
	public FilterFxTask(PApplet p, ControlP5 cp5, String taskname, SoundEngine eng) {
		super(p, cp5, taskname, eng);
		this.centerFreq = new ComputableFloatTextfieldUI(800f, 0, 20000);
		this.resonance = new ComputableFloatTextfieldUI(0f, 0, 20000);
		this.type = new ScrollableListUI(list, 0);
		
		Main.log.countFilterFxTask();
	}
	
	public void addToEngine() {
		this.eng.addEffect(this.get_gui_id(), "MOOGFILTER", getDefaultParameters());
	}

	@Override
	protected String[] getDefaultParameters() {
		//return new String[]{ "300", "150", "BP"};
		return new String[] { 
				 this.centerFreq.getValue()+"", 
				 this.resonance.getValue()+"", 
				 convertTypeValue()+"" 
				 };
	}
	
	private void processCenterFrequencyChange() {
		if (centerFreq.update())
			this.eng.updateEffect(this.get_gui_id(), "centerFreq : " + centerFreq.getValue());
	}
	
	private void processResonanceChange() {
		if (resonance.update())
			this.eng.updateEffect(this.get_gui_id(), "resonance : " + resonance.getValue());
	}
	
	private void processTypeChange() {
		if (type.update())
			this.eng.updateEffect(this.get_gui_id(), "filterType : " + convertTypeValue());
	}
	
	private String convertTypeValue() {
		String result = "";
		
		if (type.getValue().trim().equalsIgnoreCase("Band Pass"))
			result = "BP";
		if (type.getValue().trim().equalsIgnoreCase("Low Pass"))
			result = "LP";
		if (type.getValue().trim().equalsIgnoreCase("High Pass"))
			result = "HP";
		
		return result;
	}

	@Override
	protected void processAllParameters() {
		processCenterFrequencyChange();
		processResonanceChange();
		processTypeChange();
	}

	@Override
	public Task clone_it() {
		FilterFxTask clone = new FilterFxTask(this.p, this.cp5, this.name, this.eng);
		clone.centerFreq = this.centerFreq;
		clone.resonance = this.resonance;
		clone.type = this.type;
		return clone;
	}
	
	/////////////////////////////////
	// UI config
	public Group load_gui_elements(State s) {
		this.textlabel = "Filter Effect";

		String id = get_gui_id();
		Group g = super.load_gui_elements(s);
		int width = g.getWidth() - (localx * 2);

		this.backgroundheight = (int) (localoffset * 3.5);
		g.setBackgroundHeight(backgroundheight);
		int miniOffsetDueToScrollList = 5;
		centerFreq.createUI(id, "cutoff", localx, localy + miniOffsetDueToScrollList + (1 * localoffset), width, g);
		resonance.createUI(id, "resonance freq.", localx, localy + miniOffsetDueToScrollList + (2 * localoffset), width, g);
		type.createUI(id, "type", localx, localy + (0 * localoffset), width, g);

		return g;
	}

}
