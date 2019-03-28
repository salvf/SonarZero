
import java.io.File;
import java.util.List;
import javax.sound.midi.*;
import sonarzero.api.midi.Note;
import sonarzero.api.sound.player.Music;
import sonarzero.api.sound.player.Player;

/** Plays a midi file provided on command line */
public class MidiPlayer {

    public MidiPlayer() {
         MidiDevice device;
         
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {
            try {
                device = MidiSystem.getMidiDevice(info);
                //does the device have any transmitters?
                //if it does, add it to the device list
              System.out.println(info);
                //get all transmitters
                List<Transmitter> transmitters = device.getTransmitters();
                //and for each transmitter
                for(int j = 0; j<transmitters.size();j++) {
                    //create a new receiver
                    transmitters.get(j).setReceiver(
                            //using my own MidiInputReceiver
                            new MidiInputReceiver(device.getDeviceInfo().toString())
                    );
                }   Transmitter trans = device.getTransmitter();
                trans.setReceiver(new MidiInputReceiver(device.getDeviceInfo().toString()));
                //open each device
                device.open();
                //if code gets this far without throwing an exception
                //print a success message
               System.out.println(device.getDeviceInfo()+" Was Opened");
            }catch (MidiUnavailableException e) {}
        }
    }
    
    public class MidiInputReceiver implements Receiver {
        public String name;
        SysexMessage sys;
        Note note;
        ShortMessage shm;
        Music  song1,song2,song3;
        public MidiInputReceiver(String name) {
            this.name = name;
        }
        boolean b=true;
        @Override
        public void send(MidiMessage msg, long timeStamp) {
          //  System.out.println("midi received by "+name);
          if(msg.getStatus()!=248&&msg.getStatus()!=254) {
              
             // shm=(ShortMessage)msg;
               //note=new Note(shm.getData1(),shm.getData2());
              try {
                  note=new Note(msg);
                  if(note.getKey()==60&&note.getVelocity()!=0){
                      
                      if(song1==null){
                          Player.init();
                        song1 = Player.loadMusic(new File("C:\\Users\\terro\\Desktop\\ADTS1.mp3")); 
                        //song2 = Player.loadMusic(new File("C:\\Users\\terro\\Desktop\\wav\\Waltz - Harpsichord & Voice.wav"));              
                       // song3 = Player.loadMusic(new File("C:\\Users\\terro\\Desktop\\wav\\Waltz - Strings Section.wav"));  
                          System.out.println("LOADED");
                      } 
                      if(b){
                          if(song1.playing()){   
                              song1.resume();
//                              song2.resume();
//                              song3.resume();
                              System.out.println("resume "+song1.playing());
                          }   
                          else {
                            song1.play(false);
//                            song2.play(false);
//                            song3.play(false);
                              System.out.println("play "+song1.playing());
                          }  
                          b=false;
                        }
                      else {
                          song1.pause();
//                          song2.pause();
//                          song3.pause();
                          b=true;
                      }
                      System.out.println(b);
                  }
                      
                  System.out.println("Nota "+note.getKeyName(true)+" Velo "+note.getVelocity());
              } catch (InvalidMidiDataException ex) { }
              
            }
        }
        @Override
        public void close() {}
    }

    
    
    public static void main(String args[]) throws MidiUnavailableException {
        MidiPlayer midiPlayer = new MidiPlayer();
        /* MidiDevice.Info[] devicesinfo= MidiSystem.getMidiDeviceInfo();
        MidiDevice device;
        for (MidiDevice.Info info: devicesinfo) {
        device = MidiSystem.getMidiDevice(info);
        //does the device have any transmitters?
        //if it does, add it to the device list
        System.out.println(info);
        System.out.println(" Name: " + info.toString() +", Decription: " +info.getDescription() +", Vendor: " +info.getVendor());
        }*/

    }  

}