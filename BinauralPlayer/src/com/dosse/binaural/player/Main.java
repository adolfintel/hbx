/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.binaural.player;

import com.dosse.binaural.BinauralEnvelope;
import com.dosse.binaural.BinauralEnvelopePlayer;
import com.dosse.binaural.player.utils.FileChooser;
import com.dosse.binaural.player.utils.MessageBox;
import com.dosse.binaural.player.utils.YesNoCancelDialog;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

/**
 *
 * @author dosse
 */
public class Main extends javax.swing.JFrame {
    //some localization stuff

    private static String errTitle;
    private static String openErr;
    private static String scErr, resErr;
    private static String hbxDesc, hbsDesc, hbaDesc, hblDesc;
    private static String browserErr;
    private static String modifiedConfirm, exitConfirm, playStopConfirm, oneAtAtime;
    private static URI presetsURI;

    static {
        ResourceBundle bundle = ResourceBundle.getBundle("com/dosse/binaural/player/Main");
        errTitle = bundle.getString("Main.errTitle");
        openErr = bundle.getString("Main.openErr");
        scErr = bundle.getString("Main.scErr");
        resErr = bundle.getString("Main.resErr");
        hbxDesc = bundle.getString("Main.hbxDesc");
        hbsDesc = bundle.getString("Main.hbsDesc");
        hbaDesc = bundle.getString("Main.hbaDesc");
        hblDesc = bundle.getString("Main.hblDesc");
        browserErr = bundle.getString("Main.browserErr");
        modifiedConfirm = bundle.getString("Main.modifiedConfirm");
        exitConfirm = bundle.getString("Main.exitConfirm");
        playStopConfirm = bundle.getString("Main.playStopConfirm");
        oneAtAtime = bundle.getString("Main.oneAtAtime");
        try {
            presetsURI = new URI(bundle.getString("Main.downloadURL"));
        } catch (URISyntaxException ex) {
        }
    }
    //all the colors used in this application
    public static final Color darkButton_normal = new Color(32, 32, 32);
    public static final Color darkButton_pressed = new Color(16, 16, 16);
    public static final Color darkBackground = new Color(51, 51, 51);
    public static final Color close_normal = new Color(0.3f, 0.3f, 0.3f);
    public static final Color close_over = new Color(0.6f, 0.6f, 0.6f);
    public static final Color close_pressed = new Color(0.2f, 0.2f, 0.2f);
    public static final Color min_normal = new Color(0.25f,0.25f,0.25f);
    public static final Color min_over = new Color(0.6f, 0.6f, 0.6f);
    public static final Color min_pressed = new Color(0.2f, 0.2f, 0.2f);
    public static final Color titleBar_focused = new Color(32, 32, 32);
    public static final Color titleBar_unfocused = new Color(64, 64, 64);
    public static final Color about_normal = new Color(0.25f,0.25f,0.25f);
    public static final Color about_over = new Color(0.6f, 0.6f, 0.6f);
    public static final Color about_pressed = new Color(0.2f, 0.2f, 0.2f);
    public static final Color text = new Color(255, 255, 255);
    public static final Color greyText = new Color(204, 204, 204);
    public static final Color darkText = new Color(0, 0, 0);
    public static final Color brightButton_normal = new Color(153, 153, 153);
    public static final Color brightButton_pressed = new Color(128, 128, 128);
    public static final Color brightBackground = new Color(245, 245, 245);
    public static final Color brightTitleBar_focused = new Color(204, 204, 204);
    public static final Color brightTitleBar_unfocused = new Color(230, 230, 230);
    public static final Color brightButtonBar = new Color(220, 220, 220);
    public static final ColorUIResource metal_primary1 = new ColorUIResource(153, 153, 153);
    public static final ColorUIResource metal_primary2 = new ColorUIResource(192, 192, 192);
    public static final ColorUIResource metal_primary3 = new ColorUIResource(220, 220, 220);
    public static final ColorUIResource metal_secondary1 = new ColorUIResource(128, 128, 128);
    public static final ColorUIResource metal_secondary2 = new ColorUIResource(160, 160, 160);
    public static final ColorUIResource metal_secondary3 = new ColorUIResource(245, 245, 245);
    public static final Color brightLink_normal = new Color(192, 192, 255);
    public static final Color brightLink_over = new Color(240, 240, 255);
    //translucency (fade in-out) and drag n drop are only supported in windows
    public static boolean DISABLE_TRANSLUCENCY = !System.getProperty("os.name").toLowerCase().contains("win"), DRAG_N_DROP = System.getProperty("os.name").toLowerCase().contains("win");
    //this thread manages animations that involve resizing the window, not fade in/out, minimize and restore
    private AnimationThread animo;

    //fonts
    private static final Font loadFont(String s) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, Main.class.getResourceAsStream(s));
        } catch (Throwable ex) {
            return null;
        }
    }
    public static final Font reg = loadFont("/com/dosse/binaural/player/fonts/OpenSans-reg.ttf");
    public static final Font bold = reg.deriveFont(Font.BOLD);
    public static final Font fixedw = loadFont("/com/dosse/binaural/player/fonts/VeraMono.ttf");

    /**
     * Creates new form Main
     */
    public Main(String toOpen) {
        //<editor-fold defaultstate="collapsed" desc="MetalTheme (for swing stuff)">
        MetalLookAndFeel.setCurrentTheme(new MetalTheme() {
            @Override
            protected ColorUIResource getPrimary1() {
                return metal_primary1;
            }

            @Override
            protected ColorUIResource getPrimary2() {
                return metal_primary2;
            }

            @Override
            protected ColorUIResource getPrimary3() {
                return metal_primary3;
            }

            @Override
            protected ColorUIResource getSecondary1() {
                return metal_secondary1;
            }

            @Override
            protected ColorUIResource getSecondary2() {
                return metal_secondary2;
            }

            @Override
            protected ColorUIResource getSecondary3() {
                return metal_secondary3;
            }

            @Override
            public String getName() {
                return "HBX Metal Theme";
            }

            @Override
            public FontUIResource getControlTextFont() {
                return new FontUIResource(reg.deriveFont(14.0f));
            }

            @Override
            public FontUIResource getSystemTextFont() {
                return new FontUIResource(reg.deriveFont(14.0f));
            }

            @Override
            public FontUIResource getUserTextFont() {
                return new FontUIResource(reg.deriveFont(12.0f));
            }

            @Override
            public FontUIResource getMenuTextFont() {
                return new FontUIResource(reg.deriveFont(14.0f));
            }

            @Override
            public FontUIResource getWindowTitleFont() {
                return new FontUIResource(reg.deriveFont(14.0f));
            }

            @Override
            public FontUIResource getSubTextFont() {
                return new FontUIResource(reg.deriveFont(11.0f));
            }
        });
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Throwable t) {
        }
        //</editor-fold>
        initComponents();
        //<editor-fold defaultstate="collapsed" desc="Drag n Drop init">
        if (DRAG_N_DROP) {
            DropTargetListener dnd = new DropTargetListener() {
                @Override
                public void dragEnter(DropTargetDragEvent dtde) {
                }

                @Override
                public void dragOver(DropTargetDragEvent dtde) {
                }

                @Override
                public void dropActionChanged(DropTargetDragEvent dtde) {
                }

                @Override
                public void dragExit(DropTargetEvent dte) {
                }

                @Override
                public void drop(DropTargetDropEvent dtde) {
                    try {
                        Transferable tr = dtde.getTransferable();
                        if (tr.getTransferDataFlavors().length != 1) {
                            dtde.dropComplete(true);
                            MessageBox.error(oneAtAtime, errTitle);
                            return;
                        }
                        DataFlavor f = tr.getTransferDataFlavors()[0];
                        if (f.isFlavorJavaFileListType()) {
                            dtde.acceptDrop(DnDConstants.ACTION_COPY);
                            final List files = (List) tr.getTransferData(f);
                            if (files.size() != 1) {
                                dtde.dropComplete(true);
                                MessageBox.error(oneAtAtime, errTitle);
                                return;
                            }
                            new Thread() {
                                public void run() {
                                    if (modified || inEditor) {
                                        int sel = YesNoCancelDialog.confirm(modifiedConfirm, title.getText(), false);
                                        if (sel == YesNoCancelDialog.NO) {
                                            return;
                                        }
                                    }
                                    modified = false;
                                    File x = (File) (files.get(0));
                                    try {
                                        loadInPlayer(x);
                                    } catch (Exception ex) {
                                        MessageBox.error(openErr + ex.getMessage(), errTitle);
                                    }

                                }
                            }.start();
                        }
                        dtde.dropComplete(true);
                        return;
                    } catch (Throwable t) {
                        dtde.dropComplete(false);
                    }
                }
            };
            DropTarget t = new DropTarget(this, dnd);
        }
        //</editor-fold>
        //SET WINDOW ICON
        setIconImage(new ImageIcon(getClass().getResource("/com/dosse/binaural/player/icon.png")).getImage());
        //DEFAULT WINDOW SIZE AND LOCATION
        defW = 650 + getInsets().left + getInsets().right;
        defH = 78 + getInsets().top + getInsets().bottom;
        setSize(defW, defH);
        Dimension res = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(res.width / 2 - defW / 2, 20);
        //FADE IN
        if (!DISABLE_TRANSLUCENCY) {
            setOpacity(0);
            setVisible(true);
            fadeIn();
        } else {
            setVisible(true);
        }
        //START ANIMATION THREAD
        animo = new AnimationThread();
        animo.start();
        //LOAD FROM COMMAND LINE IF REQUIRED
        if (toOpen != null) {
            try {
                loadInPlayer(new File(toOpen));
            } catch (Exception ex) {
                MessageBox.error(openErr + ex.getMessage(), errTitle);
            }
        }
    }

    public Main() {
        this(null);
    }
    //<editor-fold defaultstate="collapsed" desc="ANIMATIONS">
    private int defW, defH; //default width and height

    //THIS THREAD DOES ALL THE ANIMATIONS THAT INVOLVE RESIZING THE WINDOW
    private class AnimationThread extends Thread {

        public double desiredW = defW, desiredH = defH; //desired width and height (can be set to any value)
        private long f = 0; //current frame

        @Override
        public void run() {
            setPriority(MIN_PRIORITY);
            double currentW = getWidth(), currentH = getHeight();
            for (;;) {
                long t = System.nanoTime();
                currentW = currentW * 0.8 + desiredW * 0.2;
                currentH = currentH * 0.8 + desiredH * 0.2;
                Toolkit.getDefaultToolkit().sync();
                setSize((int) currentW, (int) currentH);
                f++;
                //MAX SPEED 50 FPS
                try {
                    long diff = (System.nanoTime() - t) / 1000000L;
                    if (diff < 20) {
                        Thread.sleep(20 - diff);
                    }
                } catch (InterruptedException ex) {
                }
            }
        }
        //waits for n frames

        public void waitFrames(int n) {
            buttonsDisabled = true;
            long startF = f;
            while (f - startF < n) {
                try {
                    sleep(1);
                } catch (InterruptedException ex) {
                }
            }
            buttonsDisabled = false;
        }
    }

    private void fadeIn() {
        buttonsDisabled = true;
        for (float i = 0; i <= 1; i += 0.1) {
            long t = System.nanoTime();
            setOpacity(i);
            try {
                long diff = (System.nanoTime() - t) / 1000000L;
                if (diff < 20) {
                    Thread.sleep(20 - diff);
                }
            } catch (InterruptedException ex) {
            }
        }
        setOpacity(1);
        buttonsDisabled = false;
    }

    private void fadeOut() {
        buttonsDisabled = true;
        for (float i = 1; i >= 0; i -= 0.1) {
            long t = System.nanoTime();
            setOpacity(i);
            try {
                long diff = (System.nanoTime() - t) / 1000000L;
                if (diff < 20) {
                    Thread.sleep(20 - diff);
                }
            } catch (InterruptedException ex) {
            }
        }
        setOpacity(0);
        buttonsDisabled = false;
    }

    private void minimize() {
        buttonsDisabled = true;
        Point locationBeforeMinimizing = getLocation();
        for (float i = 1; i >= 0; i -= 0.05) {
            long t = System.nanoTime();
            setOpacity(i);
            setLocation((int) (getLocation().x * 0.9), (int) (getLocation().y * 0.9 + Toolkit.getDefaultToolkit().getScreenSize().height * 0.1));
            try {
                long diff = (System.nanoTime() - t) / 1000000L;
                if (diff < 10) {
                    Thread.sleep(10 - diff);
                }
            } catch (InterruptedException ex) {
            }
        }
        setOpacity(0);
        setLocation(locationBeforeMinimizing);
        buttonsDisabled = false;
    }

    private void restore() {
        buttonsDisabled = true;
        Point newLocation = getLocation();
        setLocation(0, Toolkit.getDefaultToolkit().getScreenSize().height);
        setOpacity(0);
        for (float i = 0; i <= 1; i += 0.05) {
            long t = System.nanoTime();
            setOpacity(i);
            setLocation((int) (getLocation().x * 0.75 + newLocation.x * 0.25), (int) (getLocation().y * 0.75 + newLocation.y * 0.25));
            try {
                long diff = (System.nanoTime() - t) / 1000000L;
                if (diff < 10) {
                    Thread.sleep(10 - diff);
                }
            } catch (InterruptedException ex) {
            }
        }
        setOpacity(1);
        buttonsDisabled = false;
    }
    //</editor-fold>

    private void destroyExpansionPanels(Container x) {
        for (Component c : x.getComponents()) {
            if (c instanceof IDeactivable) {
                ((IDeactivable) c).deactivate();
                remove(c);
            } else if (c instanceof Container) {
                destroyExpansionPanels((Container) c);
            }
        }
    }
    private PlayerPanel currentPlayerPanel = null;

    private void createPlayerPanel(BinauralEnvelope be) {
        destroyExpansionPanels(rootPane);
        PlayerPanel pp = new PlayerPanel(be) {
            @Override
            public void onDeactivation() {
                animo.desiredH = defH;
                animo.waitFrames(30);
            }
        };
        pp.setBounds(0, 78, 650, 130);
        add(pp);
        currentPlayerPanel = pp;
        animo.desiredH = defH + pp.getHeight();
        animo.waitFrames(30);
    }

    private void createEditorPanel(BinauralEnvelope be) {
        inEditor = true;
        destroyExpansionPanels(rootPane);
        EditorPanel pp = new EditorPanel(be) {
            @Override
            public void onDeactivation() {
                animo.desiredH = defH;
                animo.waitFrames(30);
                inEditor = false;
                if (isTestRequired()) {
                    modified = true;
                    lastOpened = getTestEnvelope();
                    new Thread() {
                        public void run() {
                            createPlayerPanel(lastOpened);
                        }
                    }.start();
                }
            }
        };
        pp.setBounds(0, 78, 650, 613);
        add(pp);
        animo.desiredH = defH + pp.getHeight();
        animo.waitFrames(30);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        titleBar = new javax.swing.JPanel();
        close = new javax.swing.JLabel();
        title = new javax.swing.JLabel();
        about = new javax.swing.JLabel();
        minimize = new javax.swing.JLabel();
        open = new javax.swing.JLabel();
        edit = new javax.swing.JLabel();
        download = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/dosse/binaural/player/Main"); // NOI18N
        setTitle(bundle.getString("Main.title")); // NOI18N
        setBackground(new java.awt.Color(51, 51, 51));
        setUndecorated(true);
        setResizable(false);
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowDeiconified(java.awt.event.WindowEvent evt) {
                formWindowDeiconified(evt);
            }
            public void windowIconified(java.awt.event.WindowEvent evt) {
                formWindowIconified(evt);
            }
        });

        mainPanel.setBackground(darkBackground);

        titleBar.setBackground(titleBar_focused);
        titleBar.setPreferredSize(new java.awt.Dimension(237, 21));
        titleBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                titleBarMousePressed(evt);
            }
        });
        titleBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                titleBarMouseDragged(evt);
            }
        });

        close.setBackground(close_normal);
        close.setFont(bold.deriveFont(15.0f));
        close.setForeground(greyText);
        close.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        close.setText(bundle.getString("Main.close.text")); // NOI18N
        close.setOpaque(true);
        close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                closeMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                closeMouseReleased(evt);
            }
        });

        title.setFont(bold.deriveFont(15.0f));
        title.setForeground(text);
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        title.setText(bundle.getString("Main.title.text")); // NOI18N

        about.setBackground(about_normal);
        about.setFont(bold.deriveFont(15.0f));
        about.setForeground(greyText);
        about.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        about.setText(bundle.getString("Main.about.text")); // NOI18N
        about.setOpaque(true);
        about.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                aboutMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                aboutMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                aboutMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                aboutMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                aboutMouseReleased(evt);
            }
        });

        minimize.setBackground(min_normal);
        minimize.setFont(bold.deriveFont(15.0f));
        minimize.setForeground(greyText);
        minimize.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        minimize.setText(bundle.getString("Main.minimize.text")); // NOI18N
        minimize.setOpaque(true);
        minimize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                minimizeMouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                minimizeMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                minimizeMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                minimizeMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                minimizeMousePressed(evt);
            }
        });

        javax.swing.GroupLayout titleBarLayout = new javax.swing.GroupLayout(titleBar);
        titleBar.setLayout(titleBarLayout);
        titleBarLayout.setHorizontalGroup(
            titleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, titleBarLayout.createSequentialGroup()
                .addComponent(about, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(title, javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minimize, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(close, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        titleBarLayout.setVerticalGroup(
            titleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titleBarLayout.createSequentialGroup()
                .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(about, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(close, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(minimize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        open.setBackground(darkButton_normal);
        open.setFont(reg.deriveFont(14.0f));
        open.setForeground(text);
        open.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        open.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/dosse/binaural/player/open.png"))); // NOI18N
        open.setText(bundle.getString("Main.open.text")); // NOI18N
        open.setOpaque(true);
        open.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                openMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                openMouseReleased(evt);
            }
        });

        edit.setBackground(darkButton_normal);
        edit.setFont(reg.deriveFont(14.0f));
        edit.setForeground(text);
        edit.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        edit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/dosse/binaural/player/edit.png"))); // NOI18N
        edit.setText(bundle.getString("Main.edit.text")); // NOI18N
        edit.setOpaque(true);
        edit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                editMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                editMouseReleased(evt);
            }
        });

        download.setFont(reg.deriveFont(10.0f));
        download.setForeground(brightLink_normal);
        download.setText(bundle.getString("Main.download.text")); // NOI18N
        download.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                downloadMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                downloadMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                downloadMouseExited(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(open, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(download, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(edit, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(titleBar, javax.swing.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(titleBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(open, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(edit, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(download, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public static boolean buttonsDisabled = false;
    private void closeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseClicked
        if (buttonsDisabled) {
            return;
        }
        new Thread() {
            public void run() {
                boolean alreadyConfirmed = false;
                if (!inEditor && currentPlayerPanel != null && currentPlayerPanel.isPlaying()) {
                    alreadyConfirmed = true;
                    int sel = YesNoCancelDialog.confirm(playStopConfirm, title.getText(), false);
                    if (sel == YesNoCancelDialog.NO) {
                        return;
                    }
                }
                if (modified || inEditor) {
                    alreadyConfirmed = true;
                    int sel = YesNoCancelDialog.confirm(modifiedConfirm, title.getText(), false);
                    if (sel == YesNoCancelDialog.NO) {
                        return;
                    }
                }
                if (!alreadyConfirmed) {
                    int sel = YesNoCancelDialog.confirm(exitConfirm, title.getText(), false);
                    if (sel == YesNoCancelDialog.NO) {
                        return;
                    }
                }
                if (!DISABLE_TRANSLUCENCY) {
                    fadeOut();
                }
                System.exit(0);
            }
        }.start();
    }//GEN-LAST:event_closeMouseClicked
    private int dragStartX, dragStartY;
    private void titleBarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleBarMousePressed
        dragStartX = evt.getX();
        dragStartY = evt.getY();
    }//GEN-LAST:event_titleBarMousePressed

    private void titleBarMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleBarMouseDragged
        setLocation(evt.getXOnScreen() - dragStartX, evt.getYOnScreen() - dragStartY);
    }//GEN-LAST:event_titleBarMouseDragged
    //3 FileFilters: one that accepts all .hbx and .hbs files, one that only accepts .hbs and one that only accepts .hbx
    public static final FileFilter ffAll = new FileFilter() {
        @Override
        public boolean accept(File f) {
            if (f.getName().toLowerCase().endsWith(".hbx") || f.getName().toLowerCase().endsWith(".hbs") || f.getName().toLowerCase().endsWith(".hbl") || f.isDirectory()) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getDescription() {
            return hbaDesc;
        }
    };
    public static final FileFilter ffS = new FileFilter() {
        @Override
        public boolean accept(File f) {
            if (f.getName().toLowerCase().endsWith(".hbs") || f.isDirectory()) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getDescription() {
            return hbsDesc;
        }
    };
    public static final FileFilter ffX = new FileFilter() {
        @Override
        public boolean accept(File f) {
            if (f.getName().toLowerCase().endsWith(".hbx") || f.isDirectory()) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getDescription() {
            return hbxDesc;
        }
    };
    public static final FileFilter ffL = new FileFilter() {
        @Override
        public boolean accept(File f) {
            if (f.getName().toLowerCase().endsWith(".hbl") || f.isDirectory()) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getDescription() {
            return hblDesc;
        }
    };
    //last opened (or edited) BinauralEnvelope
    private BinauralEnvelope lastOpened = null;
    private void closeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseEntered
        close.setBackground(close_over);
    }//GEN-LAST:event_closeMouseEntered

    private void closeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseExited
        close.setBackground(close_normal);
    }//GEN-LAST:event_closeMouseExited

    private void closeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMousePressed
        close.setBackground(close_pressed);
    }//GEN-LAST:event_closeMousePressed

    private void closeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseReleased
        close.setBackground(close_normal);
    }//GEN-LAST:event_closeMouseReleased

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        titleBar.setBackground(titleBar_focused);
    }//GEN-LAST:event_formWindowGainedFocus

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        titleBar.setBackground(titleBar_unfocused);
    }//GEN-LAST:event_formWindowLostFocus
    private boolean modified = false;

    private void loadInPlayer(File x) throws FileNotFoundException, IOException, Exception {
        if (x == null) {
            return;
        }
        BinauralEnvelope be = PresetCodec.loadPreset(x);
        if (!inEditor && currentPlayerPanel != null && currentPlayerPanel.isPlaying()) {
            int sel = YesNoCancelDialog.confirm(playStopConfirm, title.getText(), false);
            if (sel == YesNoCancelDialog.NO) {
                return;
            }
        }
        lastOpened = be;
        createPlayerPanel(be);
    }

    private void openMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openMouseClicked
        if (buttonsDisabled) {
            return;
        }
        new Thread() {
            public void run() {
                if (modified || inEditor) {
                    int sel = YesNoCancelDialog.confirm(modifiedConfirm, title.getText(), false);
                    if (sel == YesNoCancelDialog.NO) {
                        return;
                    }
                }
                modified = false;
                File x = (File) FileChooser.show(new FileFilter[]{ffAll}, true)[0];
                try {
                    loadInPlayer(x);
                } catch (Exception ex) {
                    MessageBox.error(openErr + ex.getMessage(), errTitle);
                }

            }
        }.start();
    }//GEN-LAST:event_openMouseClicked
    private boolean inEditor = false;
    private void editMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editMouseClicked
        if (buttonsDisabled || inEditor) {
            return;
        }
        new Thread() {
            public void run() {
                if (currentPlayerPanel != null && currentPlayerPanel.isPlaying()) {
                    int sel = YesNoCancelDialog.confirm(playStopConfirm, title.getText(), false);
                    if (sel == YesNoCancelDialog.NO) {
                        return;
                    }
                }
                createEditorPanel(lastOpened);
            }
        }.start();
    }//GEN-LAST:event_editMouseClicked
    //SOME GRAPHICS STUFF
    private void openMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openMousePressed
        open.setBackground(darkButton_pressed);
    }//GEN-LAST:event_openMousePressed
//SOME GRAPHICS STUFF
    private void openMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openMouseReleased
        open.setBackground(darkButton_normal);
    }//GEN-LAST:event_openMouseReleased
//SOME GRAPHICS STUFF
    private void editMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editMousePressed
        edit.setBackground(darkButton_pressed);
    }//GEN-LAST:event_editMousePressed
//SOME GRAPHICS STUFF
    private void editMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_editMouseReleased
        edit.setBackground(darkButton_normal);
    }//GEN-LAST:event_editMouseReleased

    private void aboutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutMouseClicked
        if (buttonsDisabled) {
            return;
        }
        AboutDialog.showDialog();
    }//GEN-LAST:event_aboutMouseClicked
//SOME GRAPHICS STUFF
    private void aboutMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutMousePressed
        about.setBackground(about_pressed);
    }//GEN-LAST:event_aboutMousePressed
//SOME GRAPHICS STUFF
    private void aboutMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutMouseReleased
        about.setBackground(about_normal);
    }//GEN-LAST:event_aboutMouseReleased

    private void minimizeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimizeMouseClicked
        if (buttonsDisabled) {
            return;
        }
        new Thread() {
            public void run() {

                if (!DISABLE_TRANSLUCENCY) {
                    minimize();
                }
                setState(ICONIFIED);

            }
        }.start();
    }//GEN-LAST:event_minimizeMouseClicked

    private void formWindowDeiconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowDeiconified
        if (buttonsDisabled) {
            return;
        }
        new Thread() {
            public void run() {
                if (!DISABLE_TRANSLUCENCY) {
                    restore();
                }
            }
        }.start();
    }//GEN-LAST:event_formWindowDeiconified
//SOME GRAPHICS STUFF
    private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
        minimizeMouseClicked(null);
    }//GEN-LAST:event_formWindowIconified
//SOME GRAPHICS STUFF
    private void minimizeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimizeMousePressed
        minimize.setBackground(min_pressed);
    }//GEN-LAST:event_minimizeMousePressed
//SOME GRAPHICS STUFF
    private void minimizeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimizeMouseReleased
        minimize.setBackground(min_normal);
    }//GEN-LAST:event_minimizeMouseReleased
//SOME GRAPHICS STUFF
    private void minimizeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimizeMouseEntered
        minimize.setBackground(min_over);
    }//GEN-LAST:event_minimizeMouseEntered
//SOME GRAPHICS STUFF
    private void minimizeMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimizeMouseExited
        minimize.setBackground(min_normal);
    }//GEN-LAST:event_minimizeMouseExited
//SOME GRAPHICS STUFF
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeMouseClicked(null);
    }//GEN-LAST:event_formWindowClosing
//SOME GRAPHICS STUFF
    private void downloadMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_downloadMouseEntered
        download.setForeground(brightLink_over);
    }//GEN-LAST:event_downloadMouseEntered
//SOME GRAPHICS STUFF
    private void downloadMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_downloadMouseExited
        download.setForeground(brightLink_normal);
    }//GEN-LAST:event_downloadMouseExited

    private void downloadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_downloadMouseClicked
        try {
            Desktop.getDesktop().browse(presetsURI);
        } catch (Throwable t) {
            MessageBox.error(browserErr + "<br>" + presetsURI.toString(), errTitle);
        }
    }//GEN-LAST:event_downloadMouseClicked
//SOME GRAPHICS STUFF
    private void aboutMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutMouseEntered
        about.setBackground(about_over);
    }//GEN-LAST:event_aboutMouseEntered
//SOME GRAPHICS STUFF
    private void aboutMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutMouseExited
        about.setBackground(about_normal);
    }//GEN-LAST:event_aboutMouseExited

    //returns true if there is a working sound card, false otherwise
    private static boolean checkSoundCard() {
        try {
            AudioFormat af = new AudioFormat(44100f, 16, 2, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(af, 8192);
            speaker.start();
            byte[] testTone = new byte[32768];
            double t = 0;
            for (int i = 0; i < testTone.length / 4; i++) {
                short s = (short) (Short.MAX_VALUE * 0.5 * Math.sin(Math.PI * 2 * t * 50));
                testTone[4 * i] = (byte) s;
                testTone[4 * i + 1] = (byte) (s >> 8);
                testTone[4 * i + 2] = (byte) s;
                testTone[4 * i + 3] = (byte) (s >> 8);
                t += 1.0 / 44100.0;
            }
            speaker.write(testTone, 0, testTone.length);
            speaker.flush();
            speaker.close();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean checkScreen() {
        Dimension res = Toolkit.getDefaultToolkit().getScreenSize();
        if (res.width < 1024 || res.height < 768) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            if (args.length == 0) {
                //START NORMALLY
                if (checkSoundCard()) {
                    if (checkScreen()) {
                        new Main();
                    } else {
                        MessageBox.error(resErr, errTitle);
                        System.exit(10);
                    }
                } else {
                    MessageBox.error(scErr, errTitle);
                    System.exit(11);
                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("--help")) {
                    showHelp();
                    System.exit(0);
                }
                //PLAY PRESET PASSED FROM COMMAND LINE
                if (checkSoundCard()) {
                    if (checkScreen()) {
                        new Main(args[0]);
                    } else {
                        MessageBox.error(resErr, errTitle);
                        System.exit(10);
                    }
                } else {
                    MessageBox.error(scErr, errTitle);
                    System.exit(11);
                }
            } else {
                if ((args.length == 3 || args.length == 4) && args[0].equalsIgnoreCase("--convert")) {
                    //CONVERSION MODE (COMMAND LINE)
                    String dstFormat = args[1];
                    if (!(dstFormat.equalsIgnoreCase("HBX") || dstFormat.equalsIgnoreCase("HBS") || dstFormat.equalsIgnoreCase("HBL") || dstFormat.equalsIgnoreCase("WAV"))) {
                        System.out.println("Unknown destination format " + dstFormat);
                        System.exit(1);
                    }
                    String sourceFile = args[2];
                    File s = new File(sourceFile);
                    String destFile = args.length == 4 ? args[3] : (args[2] + "." + dstFormat);
                    File d = new File(destFile);
                    try {
                        BinauralEnvelope source = PresetCodec.loadPreset(s);
                        FileOutputStream os = null;
                        try {
                            os = new FileOutputStream(d);
                        } catch (Throwable t) {
                            System.out.println("Can't write destination file");
                            System.exit(2);
                        }
                        try {
                            //<editor-fold defaultstate="collapsed" desc="HBX Conversion">
                            if (dstFormat.equalsIgnoreCase("HBX")) {
                                PresetCodec.toHBX(source, d);
                            }
                            //</editor-fold>
                            //<editor-fold defaultstate="collapsed" desc="HBS Conversion">
                            if (dstFormat.equalsIgnoreCase("HBS")) {
                                PresetCodec.toHBS(source, d);
                            }
                            //</editor-fold>
                            //<editor-fold defaultstate="collapsed" desc="HBL Conversion">
                            if (dstFormat.equalsIgnoreCase("HBL")) {
                                PresetCodec.toHBL(source, d);
                            }
                            //</editor-fold>
                            //<editor-fold defaultstate="collapsed" desc="WAV Conversion">
                            if (dstFormat.equalsIgnoreCase("WAV")) {
                                try {
                                    BinauralEnvelopePlayer bep = PresetCodec.toWAV(source, os);
                                    //"interface" with actual rendering thread to check progress
                                    while (bep.getPosition() < 1) {
                                        System.out.println("Rendering... " + bep.getPosition() * 100 + "%");
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException ex) {
                                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                    //render complete, kill BinauralEnvelopePlayer and close file
                                    try {
                                        bep.join();
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    try {
                                        os.close();
                                    } catch (IOException ex) {
                                    }
                                } catch (Exception ex) {
                                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            //</editor-fold>
                            System.out.println("Operation completed");
                            System.exit(0);
                        } catch (Throwable t) {
                            System.out.println("Conversion error");
                            System.exit(4);
                        }
                    } catch (Throwable t) {
                        System.out.println("Invalid source file");
                        System.exit(3);
                    }
                } else {
                    showHelp();
                    System.exit(5);
                }
            }
        } catch (Throwable e) {
            JOptionPane.showMessageDialog(new JOptionPane(), "Couldn't start application.\nPlease make sure that you have Java 7 or newer installed and that this .jar file is corrupt\n\nPress OK to exit.", "HBX", JOptionPane.ERROR_MESSAGE);
            System.exit(12);
        }

    }

    /**
     * shows help (command line)
     */
    private static void showHelp() {
        System.out.println("HBX Binaural Player - Command line help\n\nBinauralPlayer path\t\tplays a preset (GUI)\nBinauralPlayer --convert destinationFormat sourcePath [destinationPath]\t\tconverts a preset (Command line). Valid destinationFormat values are:\n\tHBX: HBX Preset\n\tHBS: Compressed HBX Preset\n\tHBL: XML Preset\n\tWAV: .wav file (44.1KHz 16 bit stereo)");
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel about;
    private javax.swing.JLabel close;
    private javax.swing.JLabel download;
    private javax.swing.JLabel edit;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel minimize;
    private javax.swing.JLabel open;
    private javax.swing.JLabel title;
    private javax.swing.JPanel titleBar;
    // End of variables declaration//GEN-END:variables
}
