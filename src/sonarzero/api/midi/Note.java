/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sonarzero.api.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 *
 * @author terro
 */
public class Note {

    private final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private String name;
    private int key;
    private int octave;
    private int velocity=0;
    private ShortMessage shmsg;
    public Note(int key,int velocity) {
        this.key = key;
        this.velocity=velocity;
        maker();
    }
    
    public Note(MidiMessage msg) throws InvalidMidiDataException {
        shmsg=(ShortMessage)msg;
        if(shmsg.getData1()<21||shmsg.getData1()>108)
            throw new InvalidMidiDataException("Out of Note's range");
        this.key = shmsg.getData1();
        this.velocity=shmsg.getData2();
        maker();
    }
    
    private void maker(){
        this.octave = (key / 12)-2;
        int note = key % 12;
        this.name = NOTE_NAMES[note];
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Note && this.key == ((Note) obj).key;
    }
    
    public int getKey(){
        return this.key;
    }
    
    public void setKey(int key){
        this.key = key;
        maker();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.key;
        return hash;
    }
    
    public int getVelocity(){
        return this.velocity;
    }

    public String getKeyName(boolean octave) {
        if(octave)
            return this.name+ this.octave;
        else 
            return this.name ;
    }
}