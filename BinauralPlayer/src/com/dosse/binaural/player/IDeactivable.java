/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.binaural.player;

/**
 *
 * @author dosse
 */
public interface IDeactivable {

    public boolean isDeactivated();

    public void deactivate();

    public void onDeactivation();
}
