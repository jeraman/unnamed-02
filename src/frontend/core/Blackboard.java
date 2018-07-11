package frontend.core;

import java.util.concurrent.ConcurrentHashMap;
import java.io.Serializable;
import processing.core.PApplet;
import java.util.regex.*;
import javax.script.ScriptException;
import frontend.Main;
import frontend.ui.AbstractElementUi;

import java.util.*;
import java.math.BigDecimal;
import oscP5.*;

/**
 * Stores global variables to be used anywhere in the system.
 * 
 * @author jeraman.info
 * @date Oct. 11 2016
 *
 * @update part of this code (support to expressions, and the ConcurrentHashMap)
 *         was written by Sofian and incorporated by him into the original code.
 */
public class Blackboard extends ConcurrentHashMap<String, Object> implements Serializable {
	private int mywidth;
	private int myheight;
	private int x;
	private int y;
	private boolean debug = false;

	transient private PApplet p;

	public Blackboard(PApplet p) {
		this.mywidth = 6 * ((Main) p).get_font_size();
		this.myheight = 2 * ((Main) p).get_font_size();

		this.build(p);
	}

	public int getWidth() {
		return mywidth * 3;
	}

	public int getHeight() {
		return myheight * (this.size() + 3);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	void set_debug(boolean b) {
		this.debug = b;
	}

	void build(PApplet p) {
		System.out
				.println("@TODO [BLACKBOARD] verify what sorts of things needs to be initialize when loaded from file");
		this.p = p;
		init_global_variables();
		this.x = ((Main) p).width - (int) (mywidth * 2.8);// -myheight;
		this.y = myheight;
	}

	void init_global_variables() {
		put("mouseX", (float) p.mouseX / p.width);
		put("mouseY", (float) p.mouseY / p.height);
		put("mousePressed", p.mousePressed);
		put("key", p.key);
		put("keyPressed", p.keyPressed);
		
		initKeyboardVariables();
	}

	public void update_global_variables() {
		// if the blackboard wasn't loaded yet
		if (p == null)
			return;

		replace("mouseX", (float) p.mouseX / p.width);
		replace("mouseY", (float) p.mouseY / p.height);
		replace("mousePressed", p.mousePressed);
		replace("key", p.key);
		replace("keyCode", p.keyCode);
		replace("keyPressed", p.keyPressed);
		
		updateKeyboardVariables();
	}
	
	private void initKeyboardVariables() {
		String playing = Main.instance().whatUserIsPlaying();
		put("playing", playing);
		String[] details = processPlaying(playing);
		put("note", details[0]);
//		put(details[0], details[0]);
		put("interval", details[1]);
//		put(details[1], details[1]);
		put("chord", details[2]);
//		put(details[2], details[2]);
	}
	
	private void updateKeyboardVariables() {
		String playing = Main.instance().whatUserIsPlaying();
		replace("playing", playing);
		String[] details = processPlaying(playing);
		replace("note", details[0]);
//		replace(details[0], details[0]);
		replace("interval", details[1]);
//		replace(details[1], details[1]);
		replace("chord", details[2]);
//		replace(details[2], details[2]);
	}
	
	private String[] processPlaying(String playing) {
		if (playing.contains("note"))
			return processNote(playing.substring("note:".length()));
		else if (playing.contains("interval"))
			return processInterval(playing.substring("interval:".length()));
		else if (playing.contains("chord"))
			return processChord(playing.substring("chord:".length()));
		else
			return processNothing();
	}

	private String[] processNote(String playing) {
		playing = "\"" + playing + "\"";
		return new String[]{playing, "\"none\"", "\"none\""};
	}
	
	private String[] processInterval(String playing) {
		playing = "\"" + playing + "\"";
		return new String[]{"\"none\"", playing, "\"none\""};
	}
	
	private String[] processChord(String playing) {
		playing = "\"" + playing + "\"";
		return new String[]{"\"none\"", "\"none\"", playing};
	}
	
	private String[] processNothing() {
		return new String[]{"\"none\"", "\"none\"", "\"none\""};
	}

	void reset() {
		this.clear();
		this.init_global_variables();
	}

	
//	  Replaces variable names in expression with pattern "$varName" or
//	  "${varName}" with values corresponding to these variables from the
//	  blackboard.
	 
	String processExpression(String expr) {

		// does not support strings
		Pattern pattern1 = Pattern.compile("(\\$(\\w+))");
		Pattern pattern2 = Pattern.compile("(\\$\\{(\\w+)\\})"); // " <-- this
																	// comment
																	// to avoid
																	// code-highlight
																	// issues in
																	// Atom
		// does not suppor 3 bb variables
		// Pattern pattern1 = Pattern.compile("([^\\\\]\\$(\\w+))");
		// Pattern pattern2 = Pattern.compile("([^\\\\]\\$\\{(\\w+)\\})"); //"
		// <-- this comment to avoid code-highlight issues in Atom

		expr = _processPattern(pattern1, expr);
		// println("Converted expression1: " +expr);
		expr = _processPattern(pattern2, expr);
		// println("Converted expression2: " +expr);
		return expr;
	}

	String _processPattern(Pattern pattern, String expr) {
		Matcher matcher = pattern.matcher(expr);
		while (matcher.find()) {
			String varName = matcher.group(2); // candidate var name in
												// blackboard
			if (containsKey(varName)) {
				String value = get(varName).toString();

				// if the result is another blackboard variable, try to get its
				// value
				if (!value.equals(varName) && value.contains("$"))
					try {
						value = (new Expression(value).eval(this)).toString();
					} catch (ScriptException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				expr = matcher.replaceFirst(value);
				matcher = pattern.matcher(expr);
			} else if (debug)
				System.out.println("Blackboard variable not found: " + varName);
		}
		if (debug)
			System.out.println("final expression: " + expr);

		return expr;
		// return expr.replaceAll("\\\\\\$", "$");
	}

	/////////////////
	// gui methods

	// draws both the header and the items
	public void draw() {
		// if the blackboard wasn't loaded yet
		if (p == null)
			return;
		draw_header_gui();
		draw_bb_items();
	}

	// draws the header
	void draw_header_gui() {

		p.noStroke();
		p.fill(255, 200);
		p.rectMode(p.CENTER);
		p.rect(x + mywidth, y, (mywidth * 3), myheight);
		p.rectMode(p.CORNERS);

		p.fill(50);
		p.textAlign(p.CENTER, p.CENTER);
		p.text("BLACKBOARD", x + mywidth, y);
	}

	// draws the items
	void draw_bb_items() {
		draw_header_gui();
		int i = 0;

		List<String> ordered = new ArrayList<String>(this.keySet());
		Collections.sort(ordered);

		for (String val : ordered) {
			if (!this.blacklisted(val)) {
				drawItem(val, this.get(val), x, y + (myheight * (i + 1)) + i + 1, mywidth, myheight);
				i++;
			}
		}
	}

	// list of memory items that should not be displyed to the use
	boolean blacklisted(String varname) {
		//String varname = element.getKey().toString();
		//if ((varname.contains("frequency") && varname.length() > 25)
		//		|| (varname.contains("amplitude") && varname.length() > 25)
		
		if (varname.equals("note") || varname.equals("interval") || varname.equals("chord"))
			// add a new item here
			return true;
		else
			return false;
	}

	void drawItem(String var_name, Object var_value, int posx, int posy, int mywidth, int myheight) {
		int xoffset = mywidth;
		posx += xoffset/4;
		mywidth = this.getWidth()/2;

		// header
		p.noStroke();
		p.fill(AbstractElementUi.blackboardBackgroundColor);
		p.rectMode(p.CENTER);
//		p.rect(posx, posy, mywidth, myheight);
//		p.rect(posx + xoffset, posy, mywidth, myheight);
//		p.rect(posx + xoffset + xoffset, posy, mywidth, myheight);
		p.rect(posx , posy, mywidth, myheight);
		p.rect(posx + 1 + mywidth, posy, mywidth-1, myheight);

		p.fill(AbstractElementUi.whiteColor);
		p.textAlign(p.CENTER, p.CENTER);

		String type_name = var_value.getClass().getName();
		// Object value = element.getValue();
		String value_string = var_value.toString();

		// in case it's a float, only exhibits two decimal points.
		if (var_value instanceof Float)
			value_string = round((float) var_value, 2).toString();
		if (var_value instanceof Double)
			value_string = round((float) ((double) var_value), 2).toString();

		//p.text(type_name.replace("java.lang.", ""), posx, posy);
//		p.text(var_name, posx + xoffset, posy);
//		p.text(value_string, posx + xoffset + xoffset + 5, posy);
		p.text(var_name, posx , posy);
		p.text(value_string, posx + mywidth + 5, posy);
	}

	// adding input osc support to the blackboard
	public void oscEvent(OscMessage msg) {
		if (debug) {
			System.out.print("### received an osc message.");
			System.out.print(" addrpattern: " + msg.addrPattern());
			System.out.print(" typetag: " + msg.typetag());
		}

		// gets the name
		String name = msg.addrPattern();
		// processing the address
		name = name.substring(1, name.length());
		name = name.replace("/", "_");

		String typetag = msg.typetag();
		int typetag_size = typetag.length();

		p.println(msg.typetag());

		for (int i = 0; i < typetag_size; i++) {

			// value will be stored in this variable
			Object value = null;

			// checks for the right data type

			// if (typetag.charAt(i).equals("i")) //integer
			if (typetag.charAt(i) == 'i')// integer
				value = msg.get(i).intValue();
			// else if (msg.checkTypetag("f")) //float
			if (typetag.charAt(i) == 'f')// float
				value = msg.get(i).floatValue();
			// else if (msg.checkTypetag("d")) //double
			if (typetag.charAt(i) == 'd')// double
				value = msg.get(i).doubleValue();
			// else if (msg.checkTypetag("s")) //string
			if (typetag.charAt(i) == 's')// string
				value = msg.get(i).stringValue();
			// else if (msg.checkTypetag("b")) //boolean
			if (typetag.charAt(i) == 'b')// boolean
				value = msg.get(i).booleanValue();
			// else if (msg.checkTypetag("l")) //long
			if (typetag.charAt(i) == 'l')// long
				value = msg.get(i).longValue();
			// else if (msg.checkTypetag("c")) //char
			if (typetag.charAt(i) == 'c')// char
				value = msg.get(i).charValue();

			if (!containsKey(name + "_" + i))
				put(name + "_" + i, value);
			else
				replace(name + "_" + i, value);
		}
	}

	public BigDecimal round(float d, int decimalPlace) {
		BigDecimal bd = new BigDecimal(Float.toString(d));
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd;
	}

}
