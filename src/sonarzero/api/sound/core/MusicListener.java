/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sonarzero.api.sound.core;

/**
 *
 * @author terro
 */
public interface MusicListener {
    
    public void onChangeStatus(String status);
    
    public void onChangeTime(Object o,double time);
}
