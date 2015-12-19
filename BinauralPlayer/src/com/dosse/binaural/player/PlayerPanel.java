/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.binaural.player;

import com.dosse.binaural.BinauralEnvelope;
import com.dosse.binaural.BinauralEnvelopePlayer;
import java.util.ResourceBundle;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.ImageIcon;

/**
 * BinauralEnvelope player. this class just uses a BinauralEnvelopePlayer to
 * play the BinauralEnvelope
 *
 * @author dosse
 */
public abstract class PlayerPanel extends javax.swing.JPanel implements IDeactivable {
    //icons

    private ImageIcon play = new ImageIcon(getClass().getResource("/com/dosse/binaural/player/play.png"));
    private ImageIcon pause = new ImageIcon(getClass().getResource("/com/dosse/binaural/player/pause.png"));
    //"interface" with player
    private BEPThread bep;
    //localization crap
    private static String scErr;

    static {
        ResourceBundle bundle = ResourceBundle.getBundle("com/dosse/binaural/player/PlayerPanel");
        scErr = bundle.getString("PlayerPanel.scErr");
    }

    /**
     * Creates new form PlayerPanel
     */
    public PlayerPanel(BinauralEnvelope be) {
        initComponents();
        bep = new BEPThread(be);
        bep.start();
        playPause.setIcon(play);
    }
    private boolean inactive = false;
    public boolean isPlaying(){
        if(bep==null) return false; else return !bep.beplayer.paused;
    }
    @Override
    public boolean isDeactivated() {
        return inactive;
    }

    @Override
    public void deactivate() {
        if (inactive) {
            return;
        }
        inactive = true;
        onDeactivation();
        setVisible(false);
        if (bep != null) {
            bep.stopPlaying();
        }
    }
    /**
     * "interface" with the BinauralEnvelopePlayer
     **/
    private class BEPThread extends Thread {

        private BinauralEnvelopePlayer beplayer;
        private BinauralEnvelope be;
        private boolean killASAP = false;

        public BEPThread(BinauralEnvelope be) {
            try {
                this.be = be;
                playing = false;
                playPause.setIcon(play);
                createBEPlayer(be);
            } catch (LineUnavailableException ex) {
                playerError();
            }
        }

        public void pause() {
            beplayer.paused = true;
        }

        public void unPause() {
            beplayer.paused = false;
        }

        @Override
        public void run() {
            if (inactive) {
                return;
            }
            for (;;) {
                while (beplayer.getPosition() <= 1) {
                    if (killASAP) {
                        return;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                    }
                    if (killASAP) {
                        return;
                    }
                    prog.setValue((int) (100 * beplayer.getPosition()));
                    int len = (int) beplayer.getLength();
                    int pos = (int) (beplayer.getPosition() * beplayer.getLength());
                    int lenH = len / 3600;
                    len %= 3600;
                    int lenM = len / 60;
                    len %= 60;
                    int lenS = len;
                    int posH = pos / 3600;
                    pos %= 3600;
                    int posM = pos / 60;
                    pos %= 60;
                    int posS = pos;
                    time.setText((posH < 10 ? "0" + posH : posH) + ":" + (posM < 10 ? "0" + posM : posM) + ":" + (posS < 10 ? "0" + posS : posS) + " / " + (lenH < 10 ? "0" + lenH : lenH) + ":" + (lenM < 10 ? "0" + lenM : lenM) + ":" + (lenS < 10 ? "0" + lenS : lenS));
                }
                beplayer.stopPlaying();
                playPause.setIcon(play);
                playing = false;
                try {
                    createBEPlayer(be);
                } catch (LineUnavailableException ex) {
                    playerError();
                }
            }
        }

        private void createBEPlayer(BinauralEnvelope be) throws LineUnavailableException {
            beplayer = new BinauralEnvelopePlayer(be);
            beplayer.paused = true;
            beplayer.start();
        }

        private void playerError() {
            prog.setStringPainted(true);
            prog.setString(scErr);
            prog.setIndeterminate(true);
            deactivate();
        }

        public void stopPlaying() {
            beplayer.stopPlaying();
            killASAP = true;
        }

        private void setPosition(double p) {
            beplayer.setPosition(p);
        }

        private void setVolume(double d) {
            beplayer.setVolume(d);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        prog = new javax.swing.JProgressBar();
        playPause = new javax.swing.JLabel();
        time = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        vol = new javax.swing.JProgressBar();

        setBackground(Main.darkBackground);
        setForeground(new java.awt.Color(255, 255, 255));

        prog.setBackground(Main.darkButton_normal);
        prog.setForeground(Main.greyText);
        prog.setBorderPainted(false);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/dosse/binaural/player/PlayerPanel"); // NOI18N
        prog.setString(bundle.getString("PlayerPanel.prog.string")); // NOI18N
        prog.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                progMouseClicked(evt);
            }
        });
        prog.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                progMouseDragged(evt);
            }
        });

        playPause.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        playPause.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playPauseMouseClicked(evt);
            }
        });

        time.setFont(Main.reg.deriveFont(14.0f));
        time.setForeground(Main.text);
        time.setText(bundle.getString("PlayerPanel.time.text")); // NOI18N

        jLabel1.setFont(Main.bold.deriveFont(14.0f));
        jLabel1.setForeground(Main.text);
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/dosse/binaural/player/headphones.png"))); // NOI18N
        jLabel1.setText(bundle.getString("PlayerPanel.jLabel1.text")); // NOI18N

        jLabel2.setFont(Main.reg.deriveFont(14.0f));
        jLabel2.setForeground(Main.text);
        jLabel2.setText(bundle.getString("PlayerPanel.jLabel2.text")); // NOI18N
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        vol.setBackground(Main.darkButton_normal);
        vol.setForeground(Main.greyText);
        vol.setValue(100);
        vol.setBorderPainted(false);
        vol.setString(bundle.getString("PlayerPanel.vol.string")); // NOI18N
        vol.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                volMouseClicked(evt);
            }
        });
        vol.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                volMouseDragged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(prog, javax.swing.GroupLayout.DEFAULT_SIZE, 593, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(playPause, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(time, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(vol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(prog, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                    .addComponent(playPause, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(time)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                            .addComponent(vol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    //time slider dragged
    private void progMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_progMouseDragged
        if (Main.buttonsDisabled) {
            return;
        }
        if (inactive) {
            return;
        }
        double p = 1 - ((double) (prog.getWidth() - evt.getX()) / (double) prog.getWidth());
        bep.setPosition(p < 0 ? 0 : p > 1 ? 1 : p);
    }//GEN-LAST:event_progMouseDragged
    private boolean playing = false;
    private void playPauseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playPauseMouseClicked
        if (Main.buttonsDisabled) {
            return;
        }
        if (inactive) {
            return;
        }
        if (playing) {
            bep.pause();
            playPause.setIcon(play);
            playing = false;
        } else {
            bep.unPause();
            playPause.setIcon(pause);
            playing = true;
        }
    }//GEN-LAST:event_playPauseMouseClicked

    private void progMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_progMouseClicked
        progMouseDragged(evt);
    }//GEN-LAST:event_progMouseClicked

    private void volMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_volMouseClicked
        volMouseDragged(evt);
    }//GEN-LAST:event_volMouseClicked
    //volume slider dragged
    private void volMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_volMouseDragged
        if (Main.buttonsDisabled) {
            return;
        }
        if (inactive) {
            return;
        }
        double p = 1 - ((double) (vol.getWidth() - evt.getX()) / (double) vol.getWidth());
        p = p < 0 ? 0 : p > 1 ? 1 : p;
        bep.setVolume(p);
        vol.setValue((int) (100 * p));
    }//GEN-LAST:event_volMouseDragged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel playPause;
    private javax.swing.JProgressBar prog;
    private javax.swing.JLabel time;
    private javax.swing.JProgressBar vol;
    // End of variables declaration//GEN-END:variables
}
