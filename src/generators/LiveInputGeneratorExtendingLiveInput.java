package generators;

import augmenters.MusicTheory;
import ddf.minim.AudioOutput;
import ddf.minim.UGen;
import ddf.minim.spi.AudioStream;
import ddf.minim.ugens.Bypass;
import ddf.minim.ugens.LiveInput;
import ddf.minim.ugens.Multiplier;
import ddf.minim.ugens.Oscil;
import ddf.minim.ugens.Vocoder;
import ddf.minim.ugens.Waves;
import util.Util;


//TODO: DELETE THIS CLASS! NO LONGER NECESSARY TO THE PROJECT

public class LiveInputGeneratorExtendingLiveInput extends ModifiedLiveInput implements Generator,Runnable {
	
	//protected static LiveInput mic;
	
	private Vocoder vocode;
	private Oscil mod;
	private int duration;
	private int pitch;
	private int velocity;
	private boolean hasVocode;
	
	
	public LiveInputGeneratorExtendingLiveInput() {
		this(false, 0, 0);
	}
	
	public LiveInputGeneratorExtendingLiveInput(int pitch, int velocity) {
		this(true, pitch, velocity);
	}
	
	public LiveInputGeneratorExtendingLiveInput(boolean hasVocode, int pitch, int velocity) {
		super(GeneratorFactory.getInput());
		
		this.hasVocode = hasVocode;
		this.pitch = pitch;
		this.velocity = velocity;
		
		if (hasVocode) {
			vocode = new Vocoder(1024, 8);
			super.patch(vocode.modulator);
			mod = new Oscil((float) MusicTheory.freqFromMIDI(pitch), Util.mapFromMidiToAmplitude(velocity), Waves.SAW);
		}
	}

	
	@Override
	public void patchEffect(UGen effect) {
		if (hasVocode)
			mod.patch(vocode).patch(effect);
		else
			super.patch(effect);
	}

	@Override
	public void patchOutput(AudioOutput out) {
		if (hasVocode)
			mod.patch(vocode).patch(out);
		else
			super.patch(out);
	}

	@Override
	public void unpatchEffect(UGen effect) {
		if (hasVocode) {
			vocode.unpatch(effect);
			mod.unpatch(vocode);
		} else
			super.unpatch(effect);
	}

	@Override
	public void unpatchOutput(AudioOutput out) {
		if (hasVocode) {
			vocode.unpatch(out);
			mod.unpatch(vocode);
		} else
			super.unpatch(out);
	}
	

	@Override
	public void noteOn() {
		// TODO Auto-generated method stub
		GeneratorFactory.patch(this);
	}

	@Override
	public void noteOff() {
		// TODO Auto-generated method stub
		GeneratorFactory.unpatch(this);
	}
	
	public void noteOffAfterDuration(int duration) {
		this.duration = duration;
		System.out.println("waiting! " + duration);
		Runnable r = this;
		new Thread(r).start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Util.delay(this.duration);
		//stop playing!
		System.out.println("stop playing!");
		this.noteOff();
	}

	@Override
	public Generator cloneInADifferentPitch(int newPitch) {
		return new LiveInputGeneratorExtendingLiveInput(newPitch, this.velocity);
	}
	
	public void close() {
		super.close();
		this.vocode = null;
		this.mod = null;
	}

}