package soundengine.generators;

import ddf.minim.AudioOutput;
import ddf.minim.UGen;


public interface Generator {

	public void patchEffect(UGen effect);
	
	public void patchOutput(AudioOutput out);
	
	public void unpatchEffect(UGen effect);
	
	public void unpatchOutput(AudioOutput out);
	
	public void updateParameterFromString(String singleParameter);
	
	public void noteOn();
	
	public void noteOff();

	public void noteOffAfterDuration(int duration);
	
	public void attach (GeneratorObserver observer);
	
	public void notifyAllObservers();
	
	public void notifyAllObservers(String updatedParameter);
	
	public void unlinkOldObservers();
	
	public boolean isClosed();

	public void close();
		
//	public Generator cloneWithPitchAndVelocityIfUnlocked(int newPitch, int newVelocity);
	
	public Generator cloneWithPitchAndVelocity(int newPitch, int newVelocity); 

	public Generator cloneWithPitch(int newPitch);	
	
}

