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
public class ConversionException extends RuntimeException {
	public ConversionException(Throwable cause) {
		super("Failed to convert audio data", cause);
	}
}
