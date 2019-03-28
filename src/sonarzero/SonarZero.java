/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sonarzero;

import java.io.File;
import javax.swing.JOptionPane;
import sonarzero.api.sound.player.Music;
import sonarzero.api.sound.player.Player;

/**
 *
 * @author terro
 */
public class SonarZero {

    /**
     * @param args the command line arguments
     */
   public static void main(String[] args) {
       /* WaveWindow frame = new WaveWindow();
	frame.setSize(new Dimension(200,200));
	frame.setVisible(true);*/
       
       //initialize Player
		Player.init();
		//load a sound and music
		//note: you can also load with Files, URLs and InputStreams 
                Music song1 = Player.loadMusic(new File("C:\\Users\\terro\\Desktop\\wav\\mp3F.mp3"));    
//                Music song1 = Player.loadMusic(new File("C:\\Users\\terro\\Desktop\\wav\\Waltz - Percussions Section.wav"));              
                Music song2 = Player.loadMusic(new File("C:\\Users\\terro\\Desktop\\wav\\Waltz - Harpsichord & Voice.wav"));              
                Music song3 = Player.loadMusic(new File("C:\\Users\\terro\\Desktop\\wav\\Waltz - Strings Section.wav")); 
		
		//start playing the music on loop
		song1.play(true,0.3);
               // song2.play(true,0.5);
               // song3.play(true,0.2);
                song3.setLoop(false);
                song1.setPan(0.5);
                song3.setPan(1);
                JOptionPane.showMessageDialog(null, "");
                song1.setVolume(0.1);
                song3.setVolume(0.1);
                JOptionPane.showMessageDialog(null, "");
		//be sure to shutdown Player when done
		Player.shutdown();
    }
    
}
