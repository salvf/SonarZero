/*
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *     
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package sonarzero.api.sound.player;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import sonarzero.api.sound.core.ByteList;
import sonarzero.api.sound.core.MemMusic;
import sonarzero.api.sound.core.Mixer;
import sonarzero.api.sound.core.MusicListener;
import sonarzero.api.sound.core.StreamInfo;
import sonarzero.api.sound.core.StreamMusic;
import sonarzero.api.sound.core.UpdateRunner;


/**
 * Player is the main class of the Player system.  In order to use the
 Player system, it must be initialized.  After that, Music and Sound
 objects can be loaded and used.  When finished with the Player system, it
 must be shutdown.
 * 
 * @author Finn Kuusisto - TinySound
 * Edited by Salvador Vera
 */
public class Player{
	private static ArrayList<MusicListener> listeners= new ArrayList<>();
	public static final String VERSION = "1.1.1";

	/**
	 * The internal format used by Player.
	 */
	public static final AudioFormat FORMAT = new AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED, //linear signed PCM
			44100, //44.1kHz sampling rate
			16, //16-bit
			2, //2 channels fool
			4, //frame size 4 bytes (16-bit, 2 channel)
			44100, //same as sampling rate
			false //little-endian
			);
        
	
	//the system has only one mixer for both music and sounds
	private static Mixer mixer;
	//need a line to the speakers
	private static SourceDataLine outLine;
	//see if the system has been initialized
	private static boolean inited = false;
	//auto-updater for the system
	private static UpdateRunner autoUpdater;
	//counter for unique sound IDs
	private static int soundCount = 0;
	
	/**
	 * Initialize Tinysound.  This must be called before loading audio.
	 */
	public static void init() {
		if (Player.inited) {
			return;
		}
                listeners.forEach(listener->{
                    listener.onChangeStatus("Initing Sonar");
                });
		//try to open a line to the speakers
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				Player.FORMAT);
		if (!AudioSystem.isLineSupported(info)) {
		    System.err.println("Unsupported output format!");
		    return;
		}
		Player.outLine = Player.tryGetLine();
		if (Player.outLine == null) {
		    System.err.println("Output line unavailable!");
		    return;
		}
		//start the line and finish initialization
		Player.outLine.start();
		Player.finishInit();
                listeners.forEach(listener->{
                    listener.onChangeStatus("Inited Sonar");
                });
	}
        
        public static void addMusicListener(final MusicListener listener){
            listeners.add(listener);
        }
	
        public static ArrayList<MusicListener> getMusicListeners(){
            return listeners;
        }
        
        public static void removeMusicListener(final MusicListener listener){
            if(listeners != null)
                listeners.remove(listener);
        }
	/**
	 * Alternative function to initialize Player which should only be used by
         * those very familiar with the Java Sound API.  This function allows the
	 * line that is used for audio playback to be opened on a specific Mixer.
	 * @param info the Mixer.Info representing the desired Mixer
	 * @throws LineUnavailableException if a Line is not available from the
	 * specified Mixer
	 * @throws SecurityException if the specified Mixer or Line are unavailable
	 * due to security restrictions
	 * @throws IllegalArgumentException if the specified Mixer is not installed
	 * on the system
	 */
	public static void init(javax.sound.sampled.Mixer.Info info) 
			throws LineUnavailableException, SecurityException,
			IllegalArgumentException {
		if (Player.inited) {
			return;
		}
		//try to open a line to the speakers
		javax.sound.sampled.Mixer mixer = AudioSystem.getMixer(info);
		DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class,
				Player.FORMAT);
		Player.outLine = (SourceDataLine)mixer.getLine(lineInfo);
		Player.outLine.open(Player.FORMAT);
		//start the line and finish initialization
		Player.outLine.start();
		Player.finishInit();
	}
	
	/**
	 * Initializes the mixer and updater, and marks Player as initialized.
	 */
	private static void finishInit() {
		//now initialize the mixer
		Player.mixer = new Mixer();
		//initialize and start the updater
		Player.autoUpdater = new UpdateRunner(Player.mixer,
				Player.outLine);
		Thread updateThread = new Thread(Player.autoUpdater);
		try {
			updateThread.setDaemon(true);
			updateThread.setPriority(Thread.MAX_PRIORITY);
		} catch (Exception e) {}
		Player.inited = true;
		updateThread.start();
		//yield to potentially give the updater a chance
		Thread.yield();
	}
	
	/**
	 * Shutdown Player.
	 */
	public static void shutdown() {
		if (!Player.inited) {
			return;
		}
		Player.inited = false;
		//stop the auto-updater if running
		Player.autoUpdater.stop();
		Player.autoUpdater = null;
		Player.outLine.stop();
		Player.outLine.flush();
		Player.mixer.clearMusic();
		Player.mixer = null;
	}
	
	/**
	 * Determine if Player is initialized and ready for use.
	 * @return true if Player is initialized, false if Player has not been
 initialized or has subsequently been shutdown
	 */
	public static boolean isInitialized() {
		return Player.inited;
	}
	
	/**
	 * Get the global volume for all audio.
	 * @return the global volume for all audio, -1.0 if Player has not been
 initialized or has subsequently been shutdown
	 */
	public static double getGlobalVolume() {
		if (!Player.inited) {
			return -1.0;
		}
		return Player.mixer.getVolume();
	}
	
	/**
	 * Set the global volume.  This is an extra multiplier, not a replacement,
	 * for all Music and Sound volume settings.  It starts at 1.0.
	 * @param volume the global volume to set
	 */
	public static void setGlobalVolume(double volume) {
		if (!Player.inited) {
			return;
		}
		Player.mixer.setVolume(volume);
	}
	
	/**
	 * Load a Music by a resource name.  The resource must be on the classpath
	 * for this to work.  This will store audio data in memory.
	 * @param name name of the Music resource
	 * @return Music resource as specified, null if not found/loaded
	 */
	public static Music loadMusic(String name) {
		return Player.loadMusic(name, false);
	}
	
	/**
	 * Load a Music by a resource name.  The resource must be on the classpath
	 * for this to work.
	 * @param name name of the Music resource
	 * @param streamFromFile true if this Music should be streamed from a
	 * temporary file to reduce memory overhead
	 * @return Music resource as specified, null if not found/loaded
	 */
	public static Music loadMusic(String name, boolean streamFromFile) {
		//check if the system is initialized
		if (!Player.inited) {
			System.err.println("Sound not initialized!");
			return null;
		}
		//check for failure
		if (name == null) {
			return null;
		}
		//check for correct naming
		if (!name.startsWith("/")) {
			name = "/" + name;
		}
		URL url = Player.class.getResource(name);
		//check for failure to find resource
		if (url == null) {
			System.err.println("Unable to find resource " + name + "!");
			return null;
		}
		return Player.loadMusic(url, streamFromFile);
	}
	
	/**
	 * Load a Music by a File.  This will store audio data in memory.
	 * @param file the Music file to load
	 * @return Music from file as specified, null if not found/loaded
	 */
	public static Music loadMusic(File file) {
		return Player.loadMusic(file, false);
	}
	
	/**
	 * Load a Music by a File.
	 * @param file the Music file to load
	 * @param streamFromFile true if this Music should be streamed from a
	 * temporary file to reduce memory overhead
	 * @return Music from file as specified, null if not found/loaded
	 */
	public static Music loadMusic(File file, boolean streamFromFile) {
		//check if the system is initialized
		if (!Player.inited) {
			System.err.println("TinySound not initialized!");
			return null;
		}
		//check for failure
		if (file == null) {
			return null;
		}
		URL url = null;
		try {
			url = file.toURI().toURL();
		} catch (MalformedURLException e) {
			System.err.println("Unable to find file " + file + "!");
			return null;
		}
		return Player.loadMusic(url, streamFromFile);
	}
	
	/**
	 * Load a Music by a URL.  This will store audio data in memory.
	 * @param url the URL of the Music
	 * @return Music from URL as specified, null if not found/loaded
	 */
	public static Music loadMusic(URL url) {
		return Player.loadMusic(url, false);
	}
        
        /**
	 * Load a Music by a URL.
         * @param baos the ByteArrayOutputStream of the Music
	 * @param streamFromFile true if this Music should be streamed from a
	 * temporary file to reduce memory overhead
	 * @return Music from URL as specified, null if not found/loaded
	 */
	public static Music loadMusic(ByteArrayOutputStream baos, boolean streamFromFile) {
		//check if the system is initialized
		if (!Player.inited) {
			System.err.println("TinySound not initialized!");
			return null;
		}
		//check for failure
		if (baos == null) {
			return null;
		}
		//get a valid stream of audio data
		AudioInputStream audioStream = Player.getValidAudioStream(baos);
		//check for failure
		if (audioStream == null) {
			return null;
		}
                
                /*WaveFileWriter wav= new WaveFileWriter();
                wav.write(AudioSystem.getAudioInputStream(url), 
                        AudioFileFormat.Type.WAVE, 
                        new File(url.toURI()));
                */
                
                
		//try to read all the bytes
		byte[][] data = Player.readAllBytes(audioStream);
		//check for failure
		if (data == null) {
			return null;
		}
		//handle differently if streaming from a file
		if (streamFromFile) {
			StreamInfo info = Player.createFileStream(data);
			//check for failure
			if (info == null) {
				return null;
			}
			//try to create it
			StreamMusic sm = null;
			try {
				sm = new StreamMusic(info.URL, info.NUM_BYTES_PER_CHANNEL,
						Player.mixer, audioStream);
			} catch (IOException e) {
				System.err.println("Failed to create StreamMusic!");
			}
			return sm;
		}
                listeners.forEach(listener->{
                    listener.onChangeStatus("Loaded");
                });
		//construct the Music object and register it with the mixer
		return new MemMusic(data[0], data[1], Player.mixer, audioStream);
	}
	
	/**
	 * Load a Music by a URL.
	 * @param url the URL of the Music
	 * @param streamFromFile true if this Music should be streamed from a
	 * temporary file to reduce memory overhead
	 * @return Music from URL as specified, null if not found/loaded
	 */
	public static Music loadMusic(URL url, boolean streamFromFile) {
		//check if the system is initialized
		if (!Player.inited) {
			System.err.println("TinySound not initialized!");
			return null;
		}
		//check for failure
		if (url == null) {
			return null;
		}
		//get a valid stream of audio data
		AudioInputStream audioStream = Player.getValidAudioStream(url);
		//check for failure
		if (audioStream == null) {
			return null;
		}
                
                /*WaveFileWriter wav= new WaveFileWriter();
                wav.write(AudioSystem.getAudioInputStream(url), 
                        AudioFileFormat.Type.WAVE, 
                        new File(url.toURI()));
                */
                
                
		//try to read all the bytes
		byte[][] data = Player.readAllBytes(audioStream);
		//check for failure
		if (data == null) {
			return null;
		}
		//handle differently if streaming from a file
		if (streamFromFile) {
			StreamInfo info = Player.createFileStream(data);
			//check for failure
			if (info == null) {
				return null;
			}
			//try to create it
			StreamMusic sm = null;
			try {
				sm = new StreamMusic(info.URL, info.NUM_BYTES_PER_CHANNEL,
						Player.mixer, audioStream);
			} catch (IOException e) {
				System.err.println("Failed to create StreamMusic!");
			}
			return sm;
		}
                listeners.forEach(listener->{
                    listener.onChangeStatus("Loaded");
                });
		//construct the Music object and register it with the mixer
		return new MemMusic(data[0], data[1], Player.mixer, audioStream);
	}
		
	/**
	 * Reads all of the bytes from an AudioInputStream.
	 * @param stream the stream to read
	 * @return all bytes from the stream, null if error
	 */
	private static byte[][] readAllBytes(AudioInputStream stream) {
		//left and right channels
		byte[][] data = null;
		int numChannels = stream.getFormat().getChannels();
            //handle 1-channel
            switch (numChannels) {
            //handle 2-channel
                case 1:
                    byte[] left = Player.readAllBytesOneChannel(stream);
                    //check failure
                    if (left == null) {
                        return null;
                    }
                    data = new byte[2][];
                    data[0] = left;
                    data[1] = left; //don't copy for the right channel
                    break;
                case 2:
                    data = Player.readAllBytesTwoChannel(stream);
                    break;
                default:
                    //wtf?
                    System.err.println("Unable to read " + numChannels + " channels!");
                    break;
            }
		return data;
	}
	
	/**
	 * Reads all of the bytes from a 1-channel AudioInputStream.
	 * @param stream the stream to read
	 * @return all bytes from the stream, null if error
	 */
	private static byte[] readAllBytesOneChannel(AudioInputStream stream) {
		//read all the bytes (assuming 1-channel)
		byte[] data = null;
		try {
			data = Player.getBytes(stream);
		}
		catch (IOException e) {
			System.err.println("Error reading all bytes from stream!");
			return null;
		}
		finally {
			try { stream.close(); } catch (IOException e) {}
		}
		return data;
	}
	
	/**
	 * Reads all of the bytes from a 2-channel AudioInputStream.
	 * @param stream the stream to read
	 * @return all bytes from the stream, null if error
	 */
	private static byte[][] readAllBytesTwoChannel(AudioInputStream stream) {
		//read all the bytes (assuming 16-bit, 2-channel)
		byte[][] data = null;
		try {
			byte[] allBytes = Player.getBytes(stream);
			byte[] left = new byte[allBytes.length / 2];
			byte[] right = new byte[allBytes.length / 2];
			for (int i = 0, j = 0; i < allBytes.length; i += 4, j += 2) {
				//interleaved left then right
				left[j] = allBytes[i];
				left[j + 1] = allBytes[i + 1];
				right[j] = allBytes[i + 2];
				right[j + 1] = allBytes[i + 3];
			}
			data = new byte[2][];
			data[0] = left;
			data[1] = right;
		}
		catch (IOException e) {
			System.err.println("Error reading all bytes from stream!");
			return null;
		}
		finally {
			try { stream.close(); } catch (IOException e) {}
		}
		return data;
	}
        
        /**
	 * Gets and AudioInputStream in the Player system format.
	 * @param out ByteArrayOutputStream of the resource
	 * @return the specified stream as an AudioInputStream stream, null if
	 * failure
	 */
	public static AudioInputStream getValidAudioStream(ByteArrayOutputStream out) {
		AudioInputStream audioStream = null;
		try {
                        File f= File.createTempFile("sonartemp", "wav");
                        OutputStream outputStream = new FileOutputStream (f); 
                        out.writeTo(outputStream);
			audioStream = AudioSystem.getAudioInputStream(f);
                        System.out.println(f.getAbsolutePath());
                        f.deleteOnExit();
			AudioFormat streamFormat = audioStream.getFormat();
			//1-channel can also be treated as stereo
			AudioFormat mono16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					44100, 16, 1, 2, 44100, false);
			//1 or 2 channel 8-bit may be easy to convert
			AudioFormat mono8 =	new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					44100, 8, 1, 1, 44100, false);
			AudioFormat stereo8 =
				new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 8, 2, 2,
					44100, false);
			//now check formats (attempt conversion as needed)
			if (streamFormat.matches(Player.FORMAT) ||
					streamFormat.matches(mono16)) {
                            System.out.println("1 Format"+ streamFormat.matches(Player.FORMAT)+"  mono16 "+streamFormat.matches(mono16));
				return audioStream;
			} //check conversion to Player format
			else if (AudioSystem.isConversionSupported(Player.FORMAT,
					streamFormat)) {
                            System.out.println("To FORMAT Convert");
				audioStream = AudioSystem.getAudioInputStream(Player.FORMAT,
						audioStream);
			} //check conversion to mono alternate
			else if (AudioSystem.isConversionSupported(mono16, streamFormat)) {
                            System.out.println("To mono16 Convert");
				audioStream = AudioSystem.getAudioInputStream(mono16,
						audioStream);
			} //try convert from 8-bit, 2-channel
			else if (streamFormat.matches(stereo8) ||
					AudioSystem.isConversionSupported(stereo8, streamFormat)) {
                            System.out.println("2  stereo8 "+streamFormat.matches(stereo8));
				//convert to 8-bit stereo first?
				if (!streamFormat.matches(stereo8)) {
					audioStream = AudioSystem.getAudioInputStream(stereo8,
							audioStream);
				}
				audioStream = Player.convertStereo8Bit(audioStream);
			} //try convert from 8-bit, 1-channel
			else if (streamFormat.matches(mono8) ||
					AudioSystem.isConversionSupported(mono8, streamFormat)) {
                            System.out.println("2  mono8 "+streamFormat.matches(mono8));
				//convert to 8-bit mono first?
				if (!streamFormat.matches(mono8)) {
					audioStream = AudioSystem.getAudioInputStream(mono8,
							audioStream);
				}
				audioStream = Player.convertMono8Bit(audioStream);
			} //it's time to give up
			else {
				System.err.println("Unable to convert audio resource!");
				System.err.println(out);
				System.err.println(streamFormat);
				audioStream.close();
				return null;
			}
			//check the frame length
			long frameLength = audioStream.getFrameLength();
			//too long
			if (frameLength > Integer.MAX_VALUE) {
				System.err.println("Audio resource too long!");
				return null;
			}
		}
		catch (UnsupportedAudioFileException e) {
			System.err.println("Unsupported audio resource!\n" +
					e.getMessage());
			return null;
		}
		catch (IOException e) {
			System.err.println("Error getting resource stream!\n" +
					e.getMessage());
			return null;
		}
		return audioStream;
	}
	
	/**
	 * Gets and AudioInputStream in the Player system format.
	 * @param url URL of the resource
	 * @return the specified stream as an AudioInputStream stream, null if
	 * failure
	 */
	private static AudioInputStream getValidAudioStream(URL url) {
		AudioInputStream audioStream = null;
		try {
			audioStream = AudioSystem.getAudioInputStream(url);
                        
			AudioFormat streamFormat = audioStream.getFormat();
			//1-channel can also be treated as stereo
			AudioFormat mono16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					44100, 16, 1, 2, 44100, false);
			//1 or 2 channel 8-bit may be easy to convert
			AudioFormat mono8 =	new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					44100, 8, 1, 1, 44100, false);
			AudioFormat stereo8 =
				new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 8, 2, 2,
					44100, false);
			//now check formats (attempt conversion as needed)
			if (streamFormat.matches(Player.FORMAT) ||
					streamFormat.matches(mono16)) {
                            System.out.println("1 Format"+ streamFormat.matches(Player.FORMAT)+"  mono16 "+streamFormat.matches(mono16));
				return audioStream;
			} //check conversion to Player format
			else if (AudioSystem.isConversionSupported(Player.FORMAT,
					streamFormat)) {
                            System.out.println("To FORMAT Convert");
				audioStream = AudioSystem.getAudioInputStream(Player.FORMAT,
						audioStream);
			} //check conversion to mono alternate
			else if (AudioSystem.isConversionSupported(mono16, streamFormat)) {
                            System.out.println("To mono16 Convert");
				audioStream = AudioSystem.getAudioInputStream(mono16,
						audioStream);
			} //try convert from 8-bit, 2-channel
			else if (streamFormat.matches(stereo8) ||
					AudioSystem.isConversionSupported(stereo8, streamFormat)) {
                            System.out.println("2  stereo8 "+streamFormat.matches(stereo8));
				//convert to 8-bit stereo first?
				if (!streamFormat.matches(stereo8)) {
					audioStream = AudioSystem.getAudioInputStream(stereo8,
							audioStream);
				}
				audioStream = Player.convertStereo8Bit(audioStream);
			} //try convert from 8-bit, 1-channel
			else if (streamFormat.matches(mono8) ||
					AudioSystem.isConversionSupported(mono8, streamFormat)) {
                            System.out.println("2  mono8 "+streamFormat.matches(mono8));
				//convert to 8-bit mono first?
				if (!streamFormat.matches(mono8)) {
					audioStream = AudioSystem.getAudioInputStream(mono8,
							audioStream);
				}
				audioStream = Player.convertMono8Bit(audioStream);
			} //it's time to give up
			else {
				System.err.println("Unable to convert audio resource!");
				System.err.println(url);
				System.err.println(streamFormat);
				audioStream.close();
				return null;
			}
			//check the frame length
			long frameLength = audioStream.getFrameLength();
			//too long
			if (frameLength > Integer.MAX_VALUE) {
				System.err.println("Audio resource too long!");
				return null;
			}
		}
		catch (UnsupportedAudioFileException e) {
			System.err.println("Unsupported audio resource!\n" +
					e.getMessage());
			return null;
		}
		catch (IOException e) {
			System.err.println("Error getting resource stream!\n" +
					e.getMessage());
			return null;
		}
		return audioStream;
	}
	
	/**
	 * Converts an 8-bit, signed, 1-channel AudioInputStream to 16-bit, signed,
	 * 1-channel.
	 * @param stream stream to convert
	 * @return converted stream
	 */
	private static AudioInputStream convertMono8Bit(AudioInputStream stream) {
		//assuming 8-bit, 1-channel to 16-bit, 1-channel
		byte[] newData = null;
		try {
			byte[] data = Player.getBytes(stream);
			int newNumBytes = data.length * 2;
			//check if size overflowed
			if (newNumBytes < 0) {
				System.err.println("Audio resource too long!");
				return null;
			}
			newData = new byte[newNumBytes];
			//convert bytes one-by-one to int, and then to 16-bit
			for (int i = 0, j = 0; i < data.length; i++, j += 2) {
				//convert it to a double
				double floatVal = (double)data[i];
				floatVal /= (floatVal < 0) ? 128 : 127;
				if (floatVal < -1.0) { //just in case
					floatVal = -1.0;
				}
				else if (floatVal > 1.0) {
					floatVal = 1.0;
				}
				//convert it to an int and then to 2 bytes
				int val = (int)(floatVal * Short.MAX_VALUE);
				newData[j + 1] = (byte)((val >> 8) & 0xFF); //MSB
				newData[j] = (byte)(val & 0xFF); //LSB
			}
		}
		catch (IOException e) {
			System.err.println("Error reading all bytes from stream!");
			return null;
		}
		finally {
			try { stream.close(); } catch (IOException e) {}
		}
		AudioFormat mono16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				44100, 16, 1, 2, 44100, false);
		return new AudioInputStream(new ByteArrayInputStream(newData), mono16,
				newData.length / 2);
	}
	
	/**
	 * Converts an 8-bit, signed, 2-channel AudioInputStream to 16-bit, signed,
	 * 2-channel.
	 * @param stream stream to convert
	 * @return converted stream
	 */
	private static AudioInputStream convertStereo8Bit(AudioInputStream stream) {
		//assuming 8-bit, 2-channel to 16-bit, 2-channel
		byte[] newData = null;
		try {
			byte[] data = Player.getBytes(stream);
			int newNumBytes = data.length * 2 * 2;
			//check if size overflowed
			if (newNumBytes < 0) {
				System.err.println("Audio resource too long!");
				return null;
			}
			newData = new byte[newNumBytes];
			for (int i = 0, j = 0; i < data.length; i += 2, j += 4) {
				//convert them to doubles
				double leftFloatVal = (double)data[i];
				double rightFloatVal = (double)data[i + 1];
				leftFloatVal /= (leftFloatVal < 0) ? 128 : 127;
				rightFloatVal /= (rightFloatVal < 0) ? 128 : 127;
				if (leftFloatVal < -1.0) { //just in case
					leftFloatVal = -1.0;
				}
				else if (leftFloatVal > 1.0) {
					leftFloatVal = 1.0;
				}
				if (rightFloatVal < -1.0) { //just in case
					rightFloatVal = -1.0;
				}
				else if (rightFloatVal > 1.0) {
					rightFloatVal = 1.0;
				}
				//convert them to ints and then to 2 bytes each
				int leftVal = (int)(leftFloatVal * Short.MAX_VALUE);
				int rightVal = (int)(rightFloatVal * Short.MAX_VALUE);
				//left channel bytes
				newData[j + 1] = (byte)((leftVal >> 8) & 0xFF); //MSB
				newData[j] = (byte)(leftVal & 0xFF); //LSB
				//then right channel bytes
				newData[j + 3] = (byte)((rightVal >> 8) & 0xFF); //MSB
				newData[j + 2] = (byte)(rightVal & 0xFF); //LSB
			}
		}
		catch (IOException e) {
			System.err.println("Error reading all bytes from stream!");
			return null;
		}
		finally {
			try { stream.close(); } catch (IOException e) {}
		}
		AudioFormat stereo16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				44100, 16, 2, 4, 44100, false);
		return new AudioInputStream(new ByteArrayInputStream(newData), stereo16,
				newData.length / 4);
	}
	
	/**
	 * Read all of the bytes from an AudioInputStream.
	 * @param stream the stream from which to read bytes
	 * @return all bytes read from the AudioInputStream
	 * @throws IOException 
	 */
	private static byte[] getBytes(AudioInputStream stream)
			throws IOException {
		//buffer 1-sec at a time
		int bufSize = (int)Player.FORMAT.getSampleRate() *
			Player.FORMAT.getChannels() * Player.FORMAT.getFrameSize();
		byte[] buf = new byte[bufSize];
		ByteList list = new ByteList(bufSize);
		int numRead = 0;
		while ((numRead = stream.read(buf)) > -1)  {
			for (int i = 0; i < numRead; i++) {
				list.add(buf[i]);
			}
		}
		return list.asArray();
	}
	
	/**
	 * Dumps audio data to a temporary file for streaming and returns a
	 * StreamInfo for the stream.
	 * @param data the audio data to write to the temporary file
	 * @return a StreamInfo for the stream
	 */
	private static StreamInfo createFileStream(byte[][] data) {
		//first try to create a file for the data to live in
		File temp = null;
		try {
			temp = File.createTempFile("tiny", "sound");
			//make sure this file will be deleted on exit
			temp.deleteOnExit();
		} catch (IOException e) {
			System.err.println("Failed to create file for streaming!");
			return null;
		}
		//see if we can get the URL for this file
		URL url = null;
		try {
			url = temp.toURI().toURL();
		} catch (MalformedURLException e1) {
			System.err.println("Failed to get URL for stream file!");
			return null;
		}
		//we have the file, now we want to be able to write to it
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(temp),
					(512 * 1024)); //buffer 512kb
		} catch (FileNotFoundException e) {
			System.err.println("Failed to open stream file for writing!");
			return null;
		}
		//write the bytes to the file
		try {
			//write two at a time from each channel
			for (int i = 0; i < data[0].length; i += 2) {
				try {
					//first left
					out.write(data[0], i, 2);
					//then right
					out.write(data[1], i, 2);
				}
				catch (IOException e) {
					//hmm
					System.err.println("Failed writing bytes to stream file!");
					return null;
				}
			}
		}
		finally {
			try {
				out.close();
			} catch (IOException e) {
				//what?
				System.err.println("Failed closing stream file after writing!");
			}
		}
		return new StreamInfo(url, data[0].length);
	}
	
	/**
	 * Iterates through available JavaSound Mixers looking for one that can
	 * provide a line to the speakers.
	 * @return an opened SourceDataLine to the speakers
	 */
	private static SourceDataLine tryGetLine() {
		//first build our line info and get all available mixers
		DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class,
				Player.FORMAT);
		javax.sound.sampled.Mixer.Info[] mixerInfos =
			AudioSystem.getMixerInfo();
		//iterate through the mixers trying to find a line
		for (int i = 0; i < mixerInfos.length; i++) {
			javax.sound.sampled.Mixer mixer = null;
			try {
				//first try to actually get the mixer
				mixer = AudioSystem.getMixer(mixerInfos[i]);
			}
			catch (SecurityException | IllegalArgumentException e) {
				//not much we can do here
			}
                    //this should never happen since we were told the mixer exists
			//check if we got a mixer and our line is supported
			if (mixer == null || !mixer.isLineSupported(lineInfo)) {
				continue;
			}
			//see if we can actually get a line
			SourceDataLine line = null;
			try {
				line = (SourceDataLine)mixer.getLine(lineInfo);
				//don't try to open if already open
				if (!line.isOpen()) {
					line.open(Player.FORMAT);
				}
			}
			catch (LineUnavailableException | SecurityException e) {
				//we either failed to get or open
				//should we do anything here?
			}
                    //not much we can do here
			//check if we succeeded
			if (line != null && line.isOpen()) {			
				return line;
			}
		}
		//no good
		return null;
	}
        
        public static class Time{
            private int MINITES=0;
            private int SECONDS=0;
            
            public static Time toMinutes(double seconds){
                double auxsec=seconds;
                int presec= (int)auxsec/60;
                int sec =((int)auxsec>=60)?(int)auxsec-(60*presec):(int)auxsec;
                int min=presec;
                return new Time(min,sec);
            }
            
            private Time(int m, int s){
                this.MINITES = m;
                this.SECONDS = s;
            }
            
            public int getMinutes(){
                return MINITES;
            }
            
            public int getSeconds(){
                return SECONDS;
            }
            
            @Override
            public String toString(){
                return MINITES+":"+SECONDS;
            }
        }
       
}
