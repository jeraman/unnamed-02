package frontend;

/************************************************
 ** My main!
 ************************************************
 ** jeraman.info, Sep. 30 2016 ******************
 ************************************************
 ************************************************/

import processing.core.PApplet;
import processing.core.PFont;
import soundengine.SoundEngine;
import soundengine.util.MidiIO;
import oscP5.*;
import java.math.BigDecimal;
import java.util.Vector;

import javax.script.*;

import controlP5.*;
import ddf.minim.*;
import frontend.core.Blackboard;
import frontend.core.Expression;
import frontend.core.MainCanvas;
import frontend.core.Serializer;
import frontend.ui.*;

public class Main extends PApplet {

	public MainCanvas canvas;
	public Blackboard board;
	public Serializer serializer;

	private static OscP5 oscP5; // my osc variables
	private static ControlP5 cp5; // my controlP5 variable for gui
	private static Minim minim;

	// system's default port for receiveing osc messages
	public String SERVER_IP;
	public int SERVER_PORT;
	public int OSC_RECV_PORT;
	public int STATE_CIRCLE_SIZE;
	public int FONT_SIZE;

	public boolean debug = false;
	public boolean keyReleased = false;
	public boolean mouseRightButtonReleased = false;
	public boolean is_loading = false;

	public static void main(String[] args) {
		PApplet.main("frontend.Main");
	}

	public void settings() {
		// fullScreen();
		size(800, 600);
	}

	public void setup() {
		inst = this;
		setupUtil();
		setupAudio();
		AbstractElementUi.setup(cp5, this);
		this.serializer = new Serializer(this);
		is_loading = true;
		background(0);
		smooth();
		board = new Blackboard(this);
		canvas = new MainCanvas(this, cp5);

		setup_expression_loading_bug();

		// this link covers changing font size:
		// https://www.kasperkamperman.com/blog/processing-code/controlp5-library-example2/
		// also here for the origins of the bug
		// https://forum.processing.org/one/topic/controlp5-blurry-text.html

		// PFont pfont = createFont("Arial",15,true); // use true/false for
		// smooth/no-smooth
		// ControlFont font = new ControlFont(pfont,12);
		// cp5.setFont(font);
		// textFont(pfont);

		textFont(cp5.getFont().getFont());
		textSize(FONT_SIZE);

		// testing autodraw
		// cp5.setAutoDraw(false);

		is_loading = false;
	}

	void setupAudio() {
		this.minim = new Minim(this);
		MidiIO.setup(this);
	}

	// solves the freezing problem when loading the first expression
	void setup_expression_loading_bug() {
		Expression test = new Expression("0");
		try {
			((Expression) test).eval(board);
		} catch (ScriptException e) {
			System.out.println("ScriptExpression thrown, unhandled update.");
		}
	}

	public void draw() {
		background(0);

		// if is loading an open patch, do not draw anything
		if (is_loading) {
			fill(255);
			textAlign(CENTER);
			text("loading... please, wait.", width / 2, height / 2);
			return;
		}

		// updates global variables in the bb
		board.update_global_variables();
		// draws the scenario
		canvas.draw();
		// draws the blackboard
		board.draw();

		if (keyReleased)
			keyReleased = false;
		if (mouseRightButtonReleased)
			mouseRightButtonReleased = false;

		serializer.autosave();
	}

	public void noteOn(int channel, int pitch, int velocity) {
		// eng.noteOn(channel, pitch, velocity);
		canvas.noteOn(channel, pitch, velocity);

	}

	public void noteOff(int channel, int pitch, int velocity) {
		// eng.noteOff(channel, pitch, velocity);
		canvas.noteOff(channel, pitch, velocity);
	}

	//////////////////////////////////////
	// UTIL FUNCTIONS

	// calls all other utils
	void setupUtil() {
		load_config();
		setup_osc();
		setup_control_p5();
	}

	// function for setting up osc
	void setup_osc() {
		// start oscP5, listening for incoming messages.
		oscP5 = new OscP5(this, OSC_RECV_PORT);
	}

	// function for setting up controlp5
	void setup_control_p5() {
		cp5 = new ControlP5(this);
		cp5.setFont(cp5.getFont().getFont(), FONT_SIZE);
	}

	void oscEvent(OscMessage msg) {
		if (debug) {
			System.out.println("### received an osc message.");
			System.out.println(" addrpattern: " + msg.addrPattern());
			System.out.println(" typetag: " + msg.typetag());
		}

		board.oscEvent(msg);
	}

	void load_config() {

		String lines[] = loadStrings(sketchPath() + "/data/config.txt");
		Vector<String> params = new Vector<String>();

		for (int i = 0; i < lines.length; i++)
			if (!lines[i].trim().startsWith("#"))
				params.add((String) lines[i]);

		if (debug) {
			println("SERVER IP: " + params.get(0));
			println("SERVER PORT: " + params.get(1));
			println("INCOMING OSC PORT: " + params.get(2));
			// println("FULLSCREEN: " + params.get(3));
			println("STATE CIRCLE SIZE: " + params.get(3));
			println("FONT SIZE: " + params.get(4));
		}

		SERVER_IP = params.get(0);
		SERVER_PORT = Integer.parseInt(params.get(1));
		OSC_RECV_PORT = Integer.parseInt(params.get(2));
		STATE_CIRCLE_SIZE = Integer.parseInt(params.get(3));
		FONT_SIZE = Integer.parseInt(params.get(4));

	}

	///////////////////////////////////////
	// ui related methods

	public boolean cmg_or_ctrl_pressed = false;
	public boolean should_copy = false;

	public void keyPressed() {
		if (debug)
			println("keyCode: " + keyCode + " key: " + key);

		// if is loading an open patch, do not draw anything
		if (is_loading)
			return;

		// updating cmg_or_ctrl_pressed if contrll or command keys were pressed
		if (keyCode == 17 || keyCode == 27 || keyCode == 157)
			cmg_or_ctrl_pressed = true;

		// if control or command key are not pressed, ignore
		if (!cmg_or_ctrl_pressed)
			return;

		switch (keyCode) {
		case 61: // +
			// create_state();
			canvas.process_plus_key_pressed();
			break;
		case 45: // -
			// remove_state();
			canvas.process_minus_key_pressed();
			break;
		case 32: // spacebar
			if (canvas.is_running())
				canvas.button_stop();
			else
				canvas.button_play();
			break;
		case 83: // s
			canvas.button_save();
			break;
		case 76: // l
			canvas.button_load();
			break;
		}
		// println(keyCode);
	}

	public void mousePressed() {
		// if is loading an open patch, do not draw anything
		if (is_loading)
			return;

		if (mouseButton == RIGHT)
			canvas.process_right_mouse_button();
		// canvas.process_shift_key();

		if (mouseButton == LEFT) {
			// if the key is not pressed, i'm not interested
			if (!keyPressed)
				return;

			switch (keyCode) {
			// in case the key it's shift
			// case 16:
			// canvas.process_shift_key();
			// break;
			// if alt key was pressed
			case 18:
				canvas.process_copy();
				break;
			}

		}
	}

	public void mouseDragged() {
		if (mouseButton == RIGHT)
			canvas.start_dragging_connection();
	}

	public void keyReleased() {
		keyReleased = true;

		// updating cmg_or_ctrl_pressed if contrll or command keys were pressed
		if (keyCode == 17 || keyCode == 27 || keyCode == 157)
			cmg_or_ctrl_pressed = false;
	}

	public void mouseReleased() {
		if (mouseButton == RIGHT)
			mouseRightButtonReleased = true;
	}

	// checks if the user released the key minus
	public boolean user_pressed_minus() {
		boolean result = keyReleased && key == '-';
		// returns the result
		return result;
	}

	///////////////////////////////////////////////////
	// the following code was taken from Sofians' prototype
	// the goal is to allow serialization

	public static OscP5 oscP5() {
		return oscP5;
	}

	public static ControlP5 cp5() {
		return cp5;
	}

	public static Minim minim() {
		return minim;
	}

	public Blackboard board() {
		return board;
	}

	public MainCanvas canvas() {
		return canvas;
	}

	public boolean debug() {
		return debug;
	}

	public String get_remote_ip() {
		return SERVER_IP;
	}

	public int get_remote_port() {
		return SERVER_PORT;
	}

	public int get_state_circle_size() {
		return STATE_CIRCLE_SIZE;
	}

	public int get_font_size() {
		return FONT_SIZE;
	}

	private static Main inst;

	public static Main instance() {
		return inst;
	}
}
