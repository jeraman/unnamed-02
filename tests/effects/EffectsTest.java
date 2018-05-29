package effects;

import augmenters.AugmentedNote;
import augmenters.AugmentedNoteMemory;
import augmenters.MusicTheory;
import ddf.minim.Minim;
import ddf.minim.MultiChannelBuffer;
import ddf.minim.spi.AudioRecordingStream;
import ddf.minim.ugens.MoogFilter.Type;
import generators.Generator;
import generators.GeneratorFactory;
import javafx.util.Pair;
import processing.core.PApplet;
import util.MidiIO;

public class EffectsTest extends PApplet{
	AugmentedNoteMemory memory;
	AudioRecordingStream fileStream;
	
	MultiChannelBuffer buf;
	float sampleRate;
	
	
	public static void main(String[] args) {
		PApplet.main("effects.EffectsTest");
	}

	public void settings() {
		size(800, 600);
	}

	public void setup() {
		background(0);
		setupAudio();
		memory = new AugmentedNoteMemory();
		fileStream = GeneratorFactory.minim.loadFileStream("123go.mp3");
	}
	
	public void update() {
		memory.update();
	}
	
	public void setupAudio() {
		Minim minim = new Minim(this);
		GeneratorFactory.setup(minim);

		Pair<MultiChannelBuffer, Float> pair = GeneratorFactory.loadMultiChannelBufferFromFile("123go.mp3");
		buf = pair.getKey();
		sampleRate = pair.getValue();

		MidiIO.setup(this);
		
	}
	
	public void stop() {
		GeneratorFactory.close();
	}

	public void draw() {
		background(0);
		this.memory.size();
	}
	
	public void mousePressed() {
		Generator gen = GeneratorFactory.temporaryFMGen(60, 127, 1500);
		AugmentedNote newNote = new AugmentedNote(0, 60, 127, gen);
		newNote.noteOn();
	}
	
	public void noteOn(int channel, int pitch, int velocity) {
		System.out.println("hey");
		Generator gen = null;
		gen = GeneratorFactory.noteOnSampleFileGen(buf, sampleRate, pitch, velocity);
		//gen = GeneratorFactory.noteOnFMGen(pitch, velocity);
		//gen = GeneratorFactory.noteOnOscillatorGen(pitch, velocity);
		//gen = GeneratorFactory.noteOnLiveInpuGen(pitch, velocity);
		
		Effect fx = null;
		//fx = new HighPassFilterEffect(5000, sampleRate);
		//fx = new LowPassFilterEffect(200, sampleRate);
		//fx = new BandPassFilterEffect(1000, 100, sampleRate);
		//fx = new DelayEffect(0.5f, 0.9f, true, true);
		//fx = new MoogFilterEffect(200, 500, Type.LP);
		//fx = new FlangerEffect(1, 0.5f, 1, 0.5f, 0.5f, 0.5f);
		fx = new BitChrushEffect(5);
		//fx = new AdrsEffect(1.f, 1.f, 0.5f, 0.5f, 1.f, 0.f, 0.f);
		
		System.out.println(MusicTheory.freqFromMIDI(pitch));
		
		AugmentedNote newNote = new AugmentedNote(channel, pitch, velocity, gen, fx);
		//newNote.addArtificialChord("min7");
		newNote.addArtificialInterval("5");
		newNote.noteOn();
		memory.put(newNote);
	}

	public void noteOff(int channel, int pitch, int velocity) {
		AugmentedNote n = memory.remove(pitch);
		if (n == null) return;
		n.noteOff();
		if(n.getGenerator()!= null)
			n.close();
	}
}