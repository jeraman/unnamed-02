package augmenters;

/**
 * Stores basic MIDI-related information
 * @author jeronimo
 *
 */
class BasicNote {
	private int pitch;
	private int velocity;
	private int channel;
	private int duration;
	
	protected BasicNote(int channel, int pitch, int velocity) {
		this(channel, pitch, velocity, Integer.MAX_VALUE);
	}

	protected BasicNote(int channel, int pitch, int velocity, int duration) {
		this.channel = channel;
		this.pitch = pitch;
		this.velocity = velocity;
		this.duration = duration;
	}
	
	boolean isNoteEquals(int wantedPitch) {
		return (this.pitch == wantedPitch);
	}

	protected int getDuration() {
		return duration;
	}
	
	protected void setDuration(int duration) {
		this.duration = duration;
	}
	
	protected int getPitch() {
		return pitch;
	}

	protected void setPitch(int pitch) {
		this.pitch = pitch;
	}
	
	protected int getVelocity() {
		return velocity;
	}

	protected void setVelocity(int velocity) {
		this.velocity = velocity;
	}

	protected int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}
}
