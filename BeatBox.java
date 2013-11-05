import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;

public class BeatBox {

    JPanel mainPanel;
    JFrame theFrame;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    ArrayList<JCheckBox> checkBoxList;
    
    final String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat",
            "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap",
            "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
            "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
            "Open Hi Conga"};
            
    final int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56,
            58, 47, 67, 63};

    public static void main(String[] args) {
        new BeatBox().buildGui();
    }
    
    public void buildGui() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        theFrame = new JFrame("B E A T B O X");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        checkBoxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        
        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);
        
        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);
        
        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);
        
        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);
        
        JButton save = new JButton("Save");
        save.addActionListener(new MySaveListener());
        buttonBox.add(save);

        JButton restore = new JButton("Restore");
        restore.addActionListener(new MyRestoreListener());
        buttonBox.add(restore);
                
        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }
        
        background.add(BorderLayout.WEST, nameBox);
        background.add(BorderLayout.EAST, buttonBox);
        
        theFrame.getContentPane().add(background);
        
        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);
        
        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkBoxList.add(c);
            mainPanel.add(c);
        }
        
        setUpMidi();
        
        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
    }
    
    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void buildTrackAndStart() {
        int[] trackList = null;
        
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        
        for (int i = 0; i < 16; i++) {
            trackList = new int[16];
            
            int key = instruments[i];
            
            for (int j = 0; j < 16; j++) {
                JCheckBox jc = checkBoxList.get((16 * i) + j);
                if (jc.isSelected()) {
                    trackList[j] = key;
                } else {
                    trackList[j] = 0;
                }
            }
            
            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }
        
        track.add(makeEvent(192, 9, 1, 0, 15));
        
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            buildTrackAndStart();
        }
    }
    
    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            sequencer.stop();
        }
    }
    
    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor(tempoFactor * 1.03f);
        }
    }
    
    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor(tempoFactor * 0.97f);        
        }
    }
    
    public class MySaveListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            boolean[] checkBoxState = new boolean[256];
            
            for (int i = 0; i < 256; i++) {
                if (checkBoxList.get(i).isSelected()) {
                    checkBoxState[i] = true;
                } else {
                    checkBoxState[i] = false;
                }
            }
            try {
                FileOutputStream fos = new FileOutputStream("beat.ser");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                
                oos.writeObject(checkBoxState);      
                
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    public class MyRestoreListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            boolean[] checkBoxState = null;
            
            try {
                FileInputStream fis = new FileInputStream("beat.ser");
                ObjectInputStream ois = new ObjectInputStream(fis);
                
                checkBoxState = (boolean[]) ois.readObject();
                
                for (int i = 0; i < 256; i++) {
                    if (checkBoxState[i]) {
                        checkBoxList.get(i).setSelected(true);
                    } else {
                        checkBoxList.get(i).setSelected(false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }   
        }
    }
    
    public void makeTracks(int[] list) {
        for (int i = 0; i < 16; i++) {
            int key = list[i];
            
            if (key != 0) {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i + 1));                
            }
        }
    }
    
    public static MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;        
    }
}