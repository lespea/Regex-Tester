package org.lespea.regextester;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

final public class Tester {
    static final Boolean  debug             = false;
    
    static final Color    NO_REGEX          = Color.DARK_GRAY;
    static final Color    OK_REGEX          = Color.GREEN;
    static final Color    BAD_REGEX         = Color.RED;
    
    static final String[] NoFindings        = { "No Matches Found" };
    
    private JButton       next;
    private JButton       prev;
    private JButton       run;
    private JCheckBox     autoFind;
    private JFrame        frmRegexTester;
    private JLabel        autoFindLabel;
    private JLabel        curLabel;
    private JLabel        maxLabel;
    private JLabel        regexLabel;
    private JLabel        regexStatus;
    private JLabel        sep;
    private JList<String> foundObjects      = new JList<String>();
    private JPanel        lowerPanel;
    private JPanel        upperPanel;
    private JScrollPane   areaScrollPanel;
    private JScrollPane   listScrollPanel;
    private JTextArea     searchText;
    private JTextField    userRegex;
    
    private final Action  findAction        = new FindAction();
    private final Action  prevAction        = new PrevAction();
    private final Action  nextAction        = new NextAction();
    private final Action  quitAction        = new QuitAction();
    
    private Pattern       userPattern       = Pattern.compile("");
    private int           cur;
    private int           max;
    
    private String[][]    findings          = { {} };
    LinkedList<String[]>  lFindings         = new LinkedList<String[]>();
    private String        lastCheckedString = "";
    
    private synchronized String[][] getFindings() {
        return this.findings;
    }
    
    private synchronized void setFindings(final String[][] findings) {
        this.findings = findings;
    }
    
    private synchronized void setNoRegex() {
        this.regexStatus.setForeground(NO_REGEX);
    }
    
    private synchronized void setOKRegex() {
        this.regexStatus.setForeground(OK_REGEX);
    }
    
    private synchronized void setBadRegex() {
        this.regexStatus.setForeground(BAD_REGEX);
    }
    
    private synchronized void setCur(final int i) {
        this.cur = i;
        this.curLabel.setText(Integer.toString(i));
    }
    
    private synchronized void setMax(final int i) {
        this.max = i;
        this.maxLabel.setText(Integer.toString(i));
    }
    
    private void parseText() {
        this.resetForms();
        
        if (this.userRegex.getText().length() > 0)
            try {
                this.userPattern = Pattern.compile(this.userRegex.getText());
                this.setOKRegex();
                
                Matcher findingMatcher = this.userPattern.matcher(this.searchText.getText());
                
                while (findingMatcher.find()) {
                    String[] parts = new String[findingMatcher.groupCount() + 1];
                    for (int i = 0; i <= findingMatcher.groupCount(); i++)
                        parts[i] = findingMatcher.group(i);
                    this.lFindings.add(parts);
                }
                
                String[][] aFindings = new String[this.lFindings.size()][];
                this.lFindings.toArray(aFindings);
                this.lFindings.clear();
                
                if (aFindings.length > 0) {
                    this.setFindings(aFindings);
                    this.setMax(aFindings.length);
                    this.nextFinding();
                }
                
                lastCheckedString = this.searchText.getText();
            } catch (PatternSyntaxException e) {
                this.setBadRegex();
            }
    }
    
    private void nextFinding() {
        if (debug) {
            System.out.println("in next");
            System.out.println(cur);
            System.out.println(max);
        }
        
        if (this.cur < this.max) {
            this.setCur(this.cur + 1);
            this.foundObjects.setListData(this.getFindings()[this.cur - 1]);
        }
        
        if (debug) {
            System.out.println("Next in done");
            System.out.println(cur);
            System.out.println(max);
        }
    }
    
    private void prevFinding() {
        if (debug) {
            System.out.println("In prev");
            System.out.println(cur);
            System.out.println(max);
        }
        
        if (this.cur > 1) {
            this.setCur(this.cur - 1);
            this.foundObjects.setListData(this.getFindings()[this.cur - 1]);
        }
        
        if (debug) {
            System.out.println("Prev in done");
            System.out.println(cur);
            System.out.println(max);
        }
    }
    
    private void resetForms() {
        this.setCur(0);
        this.setMax(0);
        this.setNoRegex();
        this.foundObjects.setToolTipText("Search results for the current finding");
        this.foundObjects.setListData(NoFindings);
        this.foundObjects.setSelectedIndex(-1);
        this.foundObjects.setEnabled(false);
    }
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Tester window = new Tester();
                    window.frmRegexTester.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Create the application.
     */
    public Tester() {
        this.initialize();
    }
    
    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        this.setupStructure();
        this.setupListeners();
        this.resetForms();
    }
    
    private void setupStructure() {
        this.frmRegexTester = new JFrame();
        this.frmRegexTester.setTitle("Regex Tester");
        this.frmRegexTester.setBounds(200, 200, 800, 600);
        this.frmRegexTester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 0, 0 };
        gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
        gridBagLayout.columnWeights = new double[] { 0.5, 0.5 };
        gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0 };
        this.frmRegexTester.getContentPane().setLayout(gridBagLayout);
        
        this.upperPanel = new JPanel();
        GridBagConstraints gbc_upperPanel = new GridBagConstraints();
        gbc_upperPanel.insets = new Insets(5, 0, 5, 0);
        gbc_upperPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_upperPanel.gridx = 0;
        gbc_upperPanel.gridy = 0;
        gbc_upperPanel.gridwidth = 2;
        this.frmRegexTester.getContentPane().add(this.upperPanel, gbc_upperPanel);
        this.upperPanel.setLayout(new BoxLayout(this.upperPanel, BoxLayout.X_AXIS));
        
        this.regexLabel = new JLabel("Regex:");
        this.regexLabel.setBorder(new EmptyBorder(0, 5, 5, 0));
        this.upperPanel.add(this.regexLabel);
        
        this.regexStatus = new JLabel("*");
        this.regexStatus.setToolTipText("Regular expression status");
        this.regexStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.regexStatus.setBorder(new EmptyBorder(0, 5, 0, 5));
        this.upperPanel.add(this.regexStatus);
        
        this.userRegex = new JTextField();
        this.userRegex.setToolTipText("Regex to search for");
        this.upperPanel.add(this.userRegex);
        
        this.searchText = new JTextArea();
        this.searchText.setToolTipText("Text to search through");
        
        this.areaScrollPanel = new JScrollPane(this.searchText);
        this.areaScrollPanel.setPreferredSize(new Dimension(1, 1));
        this.areaScrollPanel.setBorder(new TitledBorder(null, "Text to search through", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));
        GridBagConstraints gbc_areaScrollPanel = new GridBagConstraints();
        gbc_areaScrollPanel.insets = new Insets(0, 0, 5, 5);
        gbc_areaScrollPanel.fill = GridBagConstraints.BOTH;
        gbc_areaScrollPanel.gridx = 0;
        gbc_areaScrollPanel.gridy = 1;
        gbc_areaScrollPanel.weightx = 0.8;
        this.frmRegexTester.getContentPane().add(this.areaScrollPanel, gbc_areaScrollPanel);
        
        this.foundObjects.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.foundObjects.setPreferredSize(new Dimension(1, 1));
        
        this.listScrollPanel = new JScrollPane(this.foundObjects);
        this.listScrollPanel.setPreferredSize(new Dimension(1, 1));
        this.listScrollPanel.setBorder(new TitledBorder(null, "Finding parts", TitledBorder.LEADING, TitledBorder.TOP,
                null, null));
        GridBagConstraints gbc_listScrollPanel = new GridBagConstraints();
        gbc_listScrollPanel.insets = new Insets(0, 0, 5, 0);
        gbc_listScrollPanel.fill = GridBagConstraints.BOTH;
        gbc_listScrollPanel.gridx = 1;
        gbc_listScrollPanel.gridy = 1;
        gbc_listScrollPanel.weightx = 0.2;
        this.frmRegexTester.getContentPane().add(this.listScrollPanel, gbc_listScrollPanel);
        
        this.lowerPanel = new JPanel();
        GridBagConstraints gbc_lowerPanel = new GridBagConstraints();
        gbc_lowerPanel.insets = new Insets(0, 3, 5, 3);
        gbc_lowerPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_lowerPanel.gridx = 0;
        gbc_lowerPanel.gridy = 2;
        gbc_lowerPanel.gridwidth = 2;
        this.frmRegexTester.getContentPane().add(this.lowerPanel, gbc_lowerPanel);
        this.lowerPanel.setLayout(new BoxLayout(this.lowerPanel, BoxLayout.X_AXIS));
        
        this.prev = new JButton("⇐");
        prev.setMnemonic('j');
        this.prev.setToolTipText("Previous");
        this.prev.setAction(this.prevAction);
        this.prev.setEnabled(false);
        this.lowerPanel.add(this.prev);
        
        this.curLabel = new JLabel("0");
        this.curLabel.setToolTipText("Current Finding");
        this.curLabel.setBorder(new EmptyBorder(0, 10, 0, 5));
        this.lowerPanel.add(this.curLabel);
        
        this.sep = new JLabel("/");
        this.sep.setBorder(new EmptyBorder(0, 0, 0, 0));
        this.lowerPanel.add(this.sep);
        
        this.maxLabel = new JLabel("0");
        this.maxLabel.setToolTipText("Max Findings");
        this.maxLabel.setBorder(new EmptyBorder(0, 5, 0, 10));
        this.lowerPanel.add(this.maxLabel);
        
        this.next = new JButton("⇒");
        next.setMnemonic('k');
        this.next.setToolTipText("Next");
        this.next.setAction(this.nextAction);
        this.next.setEnabled(false);
        this.lowerPanel.add(this.next);
        
        this.lowerPanel.add(Box.createHorizontalGlue());
        
        this.autoFindLabel = new JLabel("Auto Find?");
        this.autoFindLabel.setToolTipText("Automatically search while the regex is being typed");
        this.lowerPanel.add(this.autoFindLabel);
        
        this.autoFind = new JCheckBox("");
        autoFind.setMnemonic('a');
        this.autoFind.setToolTipText("Automatically search while the regex is being typed");
        this.autoFind.setSelected(true);
        this.autoFind.setBorder(new EmptyBorder(0, 10, 0, 20));
        this.lowerPanel.add(this.autoFind);
        
        this.run = new JButton("Find");
        this.run.setToolTipText("Search using the given regex");
        this.run.setAction(this.findAction);
        this.run.setEnabled(false);
        this.lowerPanel.add(this.run);
        
    }
    
    private void setupListeners() {
        final String searchStr = "search";
        final String nextStr = "next";
        final String prevStr = "prev";
        
        final String quitStr = "quit";
        
        final int moveMod = InputEvent.ALT_DOWN_MASK;
        
        final JComponent root = this.frmRegexTester.getRootPane();
        
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                searchStr);
        root.getActionMap().put(searchStr, this.findAction);
        
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, moveMod, true), searchStr);
        root.getActionMap().put(searchStr, this.findAction);
        
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, moveMod, true), nextStr);
        root.getActionMap().put(nextStr, this.nextAction);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_K, moveMod, true),
                nextStr);
        root.getActionMap().put(nextStr, this.nextAction);
        
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, moveMod, true), prevStr);
        root.getActionMap().put(prevStr, this.prevAction);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_J, moveMod, true),
                prevStr);
        root.getActionMap().put(prevStr, this.prevAction);
        
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK, true), quitStr);
        root.getActionMap().put(quitStr, this.quitAction);
        
        this.userRegex.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (Tester.this.autoFind.isSelected() && !userRegex.getText().equals(userPattern.pattern())) {
                    if (debug) {
                        System.out.println("regex listener firing");
                        if (userPattern != null)
                            System.out.println(userPattern.pattern());
                    }
                    Tester.this.parseText();
                    if (debug) {
                        System.out.println("regex listener done");
                    }
                }
            }
        });
        
        this.searchText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (Tester.this.autoFind.isSelected() && !searchText.getText().equals(lastCheckedString)) {
                    if (debug) {
                        System.out.println("search listener firing");
                        System.out.println(lastCheckedString);
                    }
                    Tester.this.parseText();
                    if (debug) {
                        System.out.println("search listener done");
                    }
                }
            }
        });
        
        this.curLabel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                synchronized (Tester.this.maxLabel) {
                    if (Tester.this.cur > 1)
                        Tester.this.prev.setEnabled(true);
                    else
                        Tester.this.prev.setEnabled(false);
                    
                    if (Tester.this.cur == Tester.this.max)
                        Tester.this.next.setEnabled(false);
                    else if (Tester.this.max > 1)
                        Tester.this.next.setEnabled(true);
                }
            }
        });
        
        this.maxLabel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                synchronized (Tester.this.maxLabel) {
                    if (Tester.this.max > Tester.this.cur && Tester.this.max > 1)
                        Tester.this.next.setEnabled(true);
                    else
                        Tester.this.next.setEnabled(false);
                }
            }
        });
        
        this.autoFind.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (Tester.this.autoFind.isSelected())
                    Tester.this.run.setEnabled(false);
                else
                    Tester.this.run.setEnabled(true);
            }
        });
    }
    
    private class FindAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public FindAction() {
            this.putValue(NAME, "Find");
            this.putValue(SHORT_DESCRIPTION, "Search using the given regex");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            Tester.this.parseText();
        }
    }
    
    private class PrevAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public PrevAction() {
            this.putValue(NAME, "⇐");
            this.putValue(SHORT_DESCRIPTION, "Previous finding group");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (debug) {
                System.out.println("Calling prev");
                System.out.println(cur);
                System.out.println(max);
            }
            Tester.this.prevFinding();
            if (debug) {
                System.out.println("Prev done");
                System.out.println(cur);
                System.out.println(max);
            }
        }
    }
    
    private class NextAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public NextAction() {
            this.putValue(NAME, "⇒");
            this.putValue(SHORT_DESCRIPTION, "Next finding group");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (debug) {
                System.out.println("Calling next");
                System.out.println(cur);
                System.out.println(max);
            }
            Tester.this.nextFinding();
            if (debug) {
                System.out.println("Next done");
                System.out.println(cur);
                System.out.println(max);
            }
        }
    }
    
    private class QuitAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        
        public QuitAction() {
            this.putValue(NAME, "Quit");
            this.putValue(SHORT_DESCRIPTION, "Shut the application down");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
}
