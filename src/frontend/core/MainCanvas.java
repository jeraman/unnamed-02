package frontend.core;
/************************************************
 ** My canvas class! ****************************
 ************************************************
 ** jeraman.info, Nov. 23 2016 ******************
 ************************************************
 ************************************************/

import controlP5.*;
import frontend.Main;
import frontend.ZenStates;
import netP5.NetAddress;
import oscP5.OscMessage;

import java.io.Serializable;
import java.util.Vector;

import javax.script.ScriptException;

public class MainCanvas implements Serializable { 

	public StateMachine root; // my basic state machine
	Vector<StateMachine> sm_stack; // a stack of sm used for allowing hierarchy
	public TempoControl timeCounter;
	public Blackboard board;

	transient private Main p;
	transient private ControlP5 cp5;
	transient private Button close_preview;

	private boolean is_running;
	private boolean isTryingToConnect;

	// contructor
	public MainCanvas(Main p, ControlP5 cp5) {
		this.p = p;
		this.cp5 = cp5;
		board = new Blackboard(p);
		is_running = false;
		init_buttons();
		setup();
	}

	public boolean is_running() {
		return is_running;
	}

	void build(Main p, ControlP5 cp5) {
		this.p = p;
		this.cp5 = cp5;
		root.build(p, cp5);
		this.init_buttons();
		// root.show();
	}

	void setup() {
		root = new StateMachine(this.p, cp5, "unsaved file");
		timeCounter = new TempoControl();
		setupVariables();
	}

	void setup(StateMachine newsm, TempoControl newTimer) {
		root = newsm;
		root.build(p, cp5);
		this.timeCounter = newTimer;
		setupVariables();
	}
	
	private void setupVariables() {
		sm_stack = new Vector<StateMachine>();
		sm_stack.add(root);
		root.show();
		close_preview.hide();
		this.timeCounter.createUi(board.getX(), board.getY()+board.getHeight()+20, board.getWidth(), board.getHeight());
		this.isTryingToConnect = false;
	}
	

	// draw method
	public void draw() {

		// updates global variables in the bb
		board.update_global_variables();
		// draws the blackboard
		board.draw();
		
		this.updateTime();
		// executes the hfsm
		root.tick();
		// sm_stack.lastElement().tick();

		// drawing the root
		// root.show();
		// root.draw();
		// sm_stack.lastElement().show();
		sm_stack.lastElement().draw();
		draw_names();
	}
	
	public void nextBegin() {
		sm_stack.lastElement().nextBegin();
	}
	
	public void oscEvent(OscMessage msg) {
		board.oscEvent(msg);
	}

	public void noteOn(int channel, int pitch, int velocity) {
		this.root.noteOn(channel, pitch, velocity);
	}

	public void noteOff(int channel, int pitch, int velocity) {
		this.root.noteOff(channel, pitch, velocity);
	}
	
	public String whatUserIsPlaying() {
		return this.root.whatUserIsPlaying();
	}
	
	public String getLastPlayedNote() {
		return this.root.getLastPlayedNote();
	}
	
	public String getLastVelocity() {
		return this.root.getLastVelocity();
	}
	
	public boolean thereIsKeyDown() {
		return this.root.thereIsKeyDown();
	}
	
	public boolean thereIsKeyReleased() {
		return this.root.thereIsKeyReleased();
	}
	
	public int numberOfKeyPressed() {
		return this.root.numberOfKeyPressed();
	}
	
	public int getBeat() {
		return this.timeCounter.getBeat();
	}
	
	public int getBar() {
		return this.timeCounter.getBar();
	}
	
	public int getNoteCount() {
		return this.timeCounter.getNoteCount();
	}
	
	public int getBPM() {
		return this.timeCounter.getBPM();
	}
	
	public float getTime() {
		return this.timeCounter.getTime();
	}
	
	public float getSeconds() {
		return this.timeCounter.getSeconds();
	}
	
	public int getMinutes() {
		return this.timeCounter.getMinutes();
	}

	void draw_names() {
		p.fill(255);
		p.textAlign(p.LEFT);
		String text = root.title.toUpperCase() + " (ROOT)";

		for (int i = 1; i < sm_stack.size(); i++)
			text += "   >   " + (sm_stack.get(i).title).toUpperCase();

		p.text(text, 20, 20);
	}

	// creates a new state and adds its to the root state machine
	void create_state() {
		if (debug())
			System.out.println("creates a state");
		State newState = new State(p, cp5, State.generateRandomName(), this.sm_stack.lastElement().eng,
				p.mouseX, p.mouseY);
		// root.add_state(newState);
		this.add_state(newState);
		
		Main.log.countCreatedState();
	}

	void add_state(State newState) {
		newState.show_gui();
		sm_stack.lastElement().add_state(newState);
		newState.connect_anything_else_to_self();
	}

	// gets the state where the mouse is hoving and removes it form the root
	// state machine
	void remove_state() {
		if (debug())
			System.out.println("remove a state");
		// root.remove_state(p.mouseX, p.mouseY);
		sm_stack.lastElement().remove_state(p.mouseX, p.mouseY);
		
		Main.log.countRemovedState();
	}

	StateMachine get_actual_statemachine() {
		return sm_stack.lastElement();
	}

	// clears the root (not the current exhibited sm)
	synchronized void clear() {
		sm_stack.lastElement().clear();
		timeCounter.removeUi();
	}

	// runs the root (not the current exhibited sm)
	void start() {
		is_running = true;
		root.start();
		root.run();
		timeCounter.start();
		Main.log.countPlayBtn();
	}

	// stops the root (not the current exhibited sm)
	void stop() {
		is_running = false;
		stop_server();
		root.stop();
		Blackboard board = ZenStates.board();
		board.reset();
		timeCounter.stop();
		Main.log.countStopBtn();
	}
	
	private void updateTime() {
		this.timeCounter.update();
	}

	// sends a osc message to stop all media in the server
	void stop_server() {
		OscMessage om = new OscMessage("/stop");
		NetAddress na = new NetAddress(ZenStates.SERVER_IP, ZenStates.SERVER_PORT);
		ZenStates.oscP5.send(om, na);
		if (debug())
			System.out.println("stopping every media in the server");
	}

	public void push_root(StateMachine new_sm) {
		// hiding the current state machine
		this.sm_stack.lastElement().hide();
		// pusing the sm to be exhibited as of now
		this.sm_stack.add(new_sm);
		// shows the new state machine
		this.sm_stack.lastElement().show();
		this.close_preview.show();
	}

	void show() {
		sm_stack.lastElement().show();
	}

	void hide() {
		sm_stack.lastElement().hide();
	}

	void pop_root() {
		// in case there's only the root, nevermind poping
		if (this.sm_stack.size() == 1)
			return;
		// plays the pop animation
		sm_stack.lastElement().close();
		// otherwise, hides the current state machine
		this.sm_stack.lastElement().hide();
		// pops the last element
		this.sm_stack.remove(this.sm_stack.lastElement());
		// shows the new state machine
		this.sm_stack.lastElement().show();
		// in case this is the root, hides the button again
		if (this.sm_stack.size() == 1)
			this.close_preview.hide();
	}

	// processes the multiple interpretations of the '+' key
	public void process_plus_key_pressed() {

		// reinit any name the user was trying to change it
		// root.reset_all_names_gui();
		sm_stack.lastElement().reset_all_names_gui();

		// verifies if the mouse intersects a state
		// State result = root.intersects_gui(p.mouseX, p.mouseY);
		State result = sm_stack.lastElement().intersects_gui(p.mouseX, p.mouseY);

		// if it does not, creates a new state
		if (result == null)
			create_state();
		// otherwise, opens the pie menu
		else
			// shows the pie
			result.show_pie();

	}

	// processes the multiple interpretations of the '-' key
	public void process_minus_key_pressed() {

		// reinit any name the user was trying to change it
		// root.reset_all_names_gui();
		sm_stack.lastElement().reset_all_names_gui();

		// verifies if the mouse intersects a state
		// State result = root.intersects_gui(p.mouseX, p.mouseY);
		State result = sm_stack.lastElement().intersects_gui(p.mouseX, p.mouseY);

		// if it intersects no one, return
		if (result == null)
			return;

		// first tries to close the pie menu
		if (result.is_pie_menu_open())
			result.hide_pie();
		// otherwise, removes the state
		else
			remove_state();
	}

	public void process_right_mouse_button() {
		// looks for someone to intersect
		State result = sm_stack.lastElement().intersects_gui(p.mouseX, p.mouseY);

		// if intersected someone
		if (result != null)
			// shows the pie
			result.show_or_hide_pie();
	}

	public void process_copy() {
		if (debug())
			System.out.println("copying!");

		// verifies if the mouse intersects a state
		State result = sm_stack.lastElement().intersects_gui(p.mouseX, p.mouseY);

		// if it intersects no one, return
		if (result == null)
			return;

		// clonning the intersected state
		State newState = result.clone_it();
		// adding the new tstae to the state machine
		add_state(newState);
		// sets the new state to drag
		newState.start_gui_dragging();

	}

	// processes ui in case the shfit key was pressed
	void process_shift_key() {
		start_dragging_connection();
	}
	
	private void startConnectionAttempt() {
		this.isTryingToConnect = true;
	}
	
	public void closeConnectionAttempt() {
		this.isTryingToConnect = false;
	}

	public void start_dragging_connection() {
		// verifies if the mouse intersects a state
		State result = sm_stack.lastElement().intersects_gui(p.mouseX, p.mouseY);

		// if it does not...
		if (result != null && !isTryingToConnect) {
			this.startConnectionAttempt();
			// close pie menu
			result.hide_pie();
			// starts dragging
			result.freeze_movement_and_trigger_connection();
		}
	}

	void init_buttons() {
		int w = 4 * ZenStates.FONT_SIZE;
		int h = w;
		int offset = 5;
		
		// int x = 20; //p.width/2;
		int x = (p.width / 2) - 3 * (w + offset);
		int y = p.height - (h) - (h / 4);

		int back = p.color(255, 255, 255, 50);
		int font = p.color(50);

		CallbackListener cb_click = generate_callback_click();

		cp5.addButton("button_play").setValue(128).setPosition(x, y).setColorBackground(back).setWidth(w).setHeight(h)
				.onPress(cb_click).setLabel("play");

		cp5.addButton("button_stop").setValue(128).setPosition(x + w + offset, y).setColorBackground(back).setWidth(w)
				.setHeight(h).onPress(cb_click).setLabel("stop");

		// don't know why, but using b_save, button_saving generate problems in
		// cp5
		cp5.addButton("button_save").setValue(128).setPosition(x + (2 * w) + (2 * offset), y).setColorBackground(back)
				.setWidth(w).setHeight(h).onPress(cb_click).setLabel("save");

		cp5.addButton("button_load").setValue(128).setPosition(x + (3 * w) + (3 * offset), y).setColorBackground(back)
				.setWidth(w).setHeight(h).onPress(cb_click).setLabel("load");

		cp5.addButton("button_help").setValue(128).setPosition(x + (4 * w) + (4 * offset), y).setColorBackground(back)
			.setWidth(w).setHeight(h).onPress(cb_click).setLabel("help");

		close_preview = new Button(cp5, "close_preview");
		close_preview.setValue(128);
		close_preview.setPosition(20, 40);
		close_preview.setLabel("close preview");
		close_preview.hide();
		close_preview.onPress(generate_callback_close());
	}

	// callback functions
	public void button_play() {
		if (ZenStates.is_loading)
			return;
		this.start();
		// p.canvas.run();
	}

	public void button_stop() {
		if (ZenStates.is_loading)
			return;
		stop();
		// p.canvas.stop();
	}

	public void button_save() {
		if (ZenStates.is_loading)
			return;
		stop();
		save();
		Main.log.countSaveBtn();
	}

	public void save() {
		Main.serializer.save();
		root.save();
	}

	public void button_load() {
		if (ZenStates.is_loading)
			return;
		load();
		Main.log.countLoadBtn();
	}

	void load() {
		stop();
		Main.serializer.load();
	}
	
	public void button_help() {
		if (ZenStates.is_loading)
			return;
		help();
	}
	

	void help() {
		p.link("file://" + p.sketchPath() + "/data/cheatsheet/index.html");
	}
	
	private boolean debug() {
		return ZenStates.debug;
	}

	CallbackListener generate_callback_close() {
		return new CallbackListener() {
			public void controlEvent(CallbackEvent theEvent) {
				// close the current open state machine
				pop_root();
				if (debug())
					System.out.println("should close it!!!!");
				Main.log.countSmZoomOut();
			}
		};
	}

	CallbackListener generate_callback_click() {
		return new CallbackListener() {
			public void controlEvent(CallbackEvent theEvent) {

				String s = theEvent.getController().getName();

				if (s.equals("button_play"))
					button_play();
				if (s.equals("button_stop"))
					button_stop();
				if (s.equals("button_save"))
					button_save();
				if (s.equals("button_load"))
					button_load();
				if (s.equals("button_help"))
					button_help();
			}
		};
	}

}
