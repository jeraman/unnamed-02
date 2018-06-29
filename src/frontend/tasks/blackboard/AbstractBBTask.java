package frontend.tasks.blackboard;

import controlP5.ControlP5;
import frontend.Blackboard;
import frontend.Main;
import frontend.tasks.Task;
import frontend.ui.TextfieldUi;
import frontend.ui.ToggleUi;
import processing.core.PApplet;
import soundengine.util.Util;



abstract class AbstractBBTask extends Task {
	
	protected TextfieldUi variableName;
	protected TextfieldUi value;
	protected ToggleUi shouldRepeat;
	float timer;
	float timerMilestone;
	
	public AbstractBBTask(PApplet p, ControlP5 cp5, String taskname) {
		super(p, cp5, taskname);
		
		this.variableName = new TextfieldUi(taskname);
		this.shouldRepeat = new ToggleUi();
		
		this.timerMilestone = 0;
		this.timer = 0;
	}
	
	private void processNameChange() {
		variableName.update();
	}
	
	private void processValueChange() {
		value.update();
	}

	protected void processAllParameters() {
		processNameChange();
		processValueChange();
	}
	
	public void run() {
		boolean wasFirstTime = first_time;
		super.run();	
		if (shouldRepeat.getValue() || wasFirstTime)
			updateVariable();
	}
	
	public boolean should_run() {
		if (first_time)
			reset_timer();
		boolean should_run = super.should_run();
		update_timer();
		return should_run;
	}
	
	void update_timer() {
		this.timer = ((float) Util.millis() / 1000f) - timerMilestone;
	}

	void reset_timer() {
		this.timerMilestone = (float) Util.millis() / 1000f;
		this.timer = 0;
	}

	public void updateVariable() {
		System.out.println("updating variable: " + value.evaluate());
		Blackboard board = Main.instance().board();
		board.put(variableName.getValue(), value.evaluate());
	}
	
	@Override
	protected String[] getDefaultParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset_gui_fields() {
		// TODO Auto-generated method stub
		
	}

}