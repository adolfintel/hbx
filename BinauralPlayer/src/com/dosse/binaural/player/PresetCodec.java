/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dosse.binaural.player;

import com.dosse.binaural.BinauralEnvelope;
import com.dosse.binaural.BinauralEnvelopePlayer;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author dosse
 */
public class PresetCodec {

    /**
     * headers (magic numbers actually) of HBX and HBS files are calculated from
     * 2 simple strings (HBX and HBS respectively)
     *
     */
    public static final byte[] HBX_HEADER, HBS_HEADER;

    static {
        String h = "HBX";
        HBX_HEADER = new byte[h.length()];
        char[] arr = h.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            HBX_HEADER[i] = (byte) arr[i];
        }
        h = "HBS";
        HBS_HEADER = new byte[h.length()];
        arr = h.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            HBS_HEADER[i] = (byte) arr[i];
        }
    }

    /**
     * loads a preset from a file
     *
     * @param x input file
     * @return the BinauralEnvelope stored in the file
     * @throws FileNotFoundException if an IO error occurs
     * @throws IOException if an IO error occurs
     * @throws Exception if an IO error occurs
     */
    public static BinauralEnvelope loadPreset(File x) throws FileNotFoundException, IOException, Exception {
        if (x == null) {
            throw new Exception("Invalid file");
        }
        if (x.getName().toLowerCase().endsWith(".hbl")) {
            BufferedReader xmlFile = new BufferedReader(new FileReader(x));
            String xml = "";
            for (;;) {
                try {
                    String line = xmlFile.readLine();
                    if (line == null) {
                        break;
                    } else {
                        xml += line + "\n";
                    }
                } catch (IOException e) {
                    break;
                }
            }
            xmlFile.close();
            return BinauralEnvelope.fromXML(xml);
        } else {
            FileInputStream fis = new FileInputStream(x);
            boolean hbx = true, hbs = true;
            byte[] headerX = new byte[HBX_HEADER.length];
            byte[] headerS = new byte[HBS_HEADER.length];
            fis.read(headerX);
            fis.close();
            fis = new FileInputStream(x);
            fis.read(headerS);
            for (int i = 0; i < headerX.length; i++) {
                if (headerX[i] != HBX_HEADER[i]) {
                    hbx = false;
                }
            }
            for (int i = 0; i < headerS.length; i++) {
                if (headerS[i] != HBS_HEADER[i]) {
                    hbs = false;
                }
            }
            fis.close();
            fis = new FileInputStream(x);
            if (!hbx && !hbs) {
                throw new Exception("Invalid file");
            }
            if (hbx) {
                fis.read(headerX);
                ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(fis));
                BinauralEnvelope be = (BinauralEnvelope) (ois.readObject());
                ois.close();
                return be;
            }
            if (hbs) {
                fis.read(headerS);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (;;) {
                    int in = fis.read();
                    if (in == -1) {
                        break;
                    } else {
                        baos.write(in);
                    }
                }
                BinauralEnvelope be = BinauralEnvelope.fromHES(baos.toByteArray());
                fis.close();
                return be;
            }
        }
        throw new Exception("Invalid file");
    }

    /**
     * saves a BinauralEnvelope to a file (HBX format)
     *
     * @param be The BinauralEnvelope to save
     * @param f destination file
     * @throws Exception if some IO error occurs
     */
    public static void toHBX(BinauralEnvelope be, File f) throws Exception {
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(HBX_HEADER);
        GZIPOutputStream gos = new GZIPOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(gos);
        oos.writeObject(be);
        oos.flush();
        oos.close();
        gos.flush();
        gos.close();
        fos.flush();
        fos.close();
    }

    /**
     * saves a BinauralEnvelope to a file (HBS format)
     *
     * @param be The BinauralEnvelope to save
     * @param f destination file
     * @throws Exception if some IO error occurs
     */
    public static void toHBS(BinauralEnvelope be, File f) throws Exception {
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(HBS_HEADER);
        fos.write(be.toHES());
        fos.flush();
        fos.close();
    }

    /**
     * saves a BinauralEnvelope to a file (XML format)
     *
     * @param be The BinauralEnvelope to save
     * @param f destination file
     * @throws Exception if some IO error occurs
     */
    public static void toHBL(BinauralEnvelope be, File f) throws Exception {
        FileWriter fw = new FileWriter(f);
        fw.write(be.toXML());
        fw.close();
    }

//WAV HEADER STUFF
    private static void writeS(OutputStream out, String s) {
        for (int i = 0; i < s.length(); i++) {
            try {
                out.write(s.charAt(i));
            } catch (IOException ex) {
            }
        }
    }
//WAV HEADER STUFF

    private static void writeInt(OutputStream out, int val) {
        try {
            out.write(val);
            out.write(val >> 8);
            out.write(val >> 16);
            out.write(val >> 24);
        } catch (IOException ex) {
        }

    }
//WAV HEADER STUFF

    private static void writeShort(OutputStream out, short val) {
        try {
            out.write(val);
            out.write(val >> 8);
        } catch (IOException ex) {
        }
    }

    /**
     * renders a BinauralEnvelope to a wav file. (note: this method is
     * non-blocking!)
     *
     * @param be The BinauralEnvelope to render
     * @param f the OutputStream where the data will be written to
     * @return an instance of BinauralEnvelopePlayer. This can be used to check
     * progress. when the render is complete, it can be stopped with
     * .stopPlaying() at any time. .join() can be called when the render is
     * complete (or aborted) to stop the thread and then file can be closed
     * @throws Exception if the output file is invalid or some other IO error
     * occurs
     */
    public static BinauralEnvelopePlayer toWAV(BinauralEnvelope be, OutputStream os) throws Exception {
        //wav header stuff
        writeS(os, "RIFF");
        writeInt(os, (int) (Math.ceil(36 + be.getLength() * 44100 * 4)));
        writeS(os, "WAVE");
        writeS(os, "fmt ");
        writeInt(os, 16);
        writeShort(os, (short) 1);
        writeShort(os, (short) 2);
        writeInt(os, 44100);
        writeInt(os, 2 * 44100 * 16 / 8);
        writeShort(os, (short) (2 * 16 / 8));
        writeShort(os, (short) 16);
        writeS(os, "data");
        writeInt(os, (int) (Math.ceil(be.getLength() * 44100 * 4)));
        //start rendering
        BinauralEnvelopePlayer bep = new BinauralEnvelopePlayer(be, os);
        bep.start();
        return bep;
    }
}
