package bitmap;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ViewerFrame extends JFrame {
	JPanel contentPane;
	JMenuBar jMenuBar = new JMenuBar();
	BorderLayout borderLayout1 = new BorderLayout();
	Bitmap bmap = new Bitmap(32, 32);
	BitmapPanel drawPanel = new BitmapPanel(bmap);
	Bitmap bmap2 = new Bitmap(32, 32);
	BitmapPanel drawPanel2 = new BitmapPanel(bmap2);
	JPanel interactPanel = new JPanel();
	JButton nextButton = new JButton();
	JButton prevButton = new JButton();
	JButton goButton = new JButton();
	JTextField positonField = new JTextField(5);
	FlowLayout flowLayout1 = new FlowLayout();
	LetterViewer lv;
	JLabel label = new JLabel("   0");
	
	int current = 0;

	/** Construct the frame */
	public ViewerFrame(LetterViewer lv) {
		this.lv = lv;
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Component initialization */
	private void jbInit() throws Exception {
		// setIconImage(Toolkit.getDefaultToolkit().createImage(DigitsFrame.class.getResource("[Your Icon]")));
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(borderLayout1);
		this.setSize(new Dimension(760, 400));
		this.setTitle("Bitmap viewer");

		nextButton.setText("Next");
		nextButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextButton_actionPerformed(e);
			}
		});
		prevButton.setText("Prev");
		prevButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prevButton_actionPerformed(e);
			}
		});
		goButton.setText("Go");
		goButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				goButton_actionPerformed(e);
			}
		});

		interactPanel.setBackground(Color.lightGray);

		contentPane.add(interactPanel, BorderLayout.SOUTH);
		drawPanel.setPreferredSize(new Dimension(360,  360));
		drawPanel2.setPreferredSize(new Dimension(360,  360));
		contentPane.add(drawPanel, "West");
		contentPane.add(drawPanel2, "East");
		interactPanel.add(label);
		interactPanel.add(prevButton, null);
		interactPanel.add(nextButton, null);
		interactPanel.add(positonField, null);
		interactPanel.add(goButton, null);
		this.setJMenuBar(jMenuBar);
	}

	void nextButton_actionPerformed(ActionEvent e) {
		current++;
		drawPanel.loadBitmap(lv.get(current));
		drawPanel2.loadBitmap(Preprocessor.scaleBitmap(lv.get(current)));
		label.setText(String.format("%3d", current));
	}
	
	void prevButton_actionPerformed(ActionEvent e) {
		current--;
		drawPanel.loadBitmap(lv.get(current));
		drawPanel2.loadBitmap(Preprocessor.scaleBitmap(lv.get(current)));
		label.setText(String.format("%3d", current));
	}
	
	void goButton_actionPerformed(ActionEvent e) {
		try {
			Integer position = Integer.valueOf(positonField.getText());
			current = position;
			Bitmap b = lv.get(current);
			drawPanel.loadBitmap(lv.get(current));
			drawPanel2.loadBitmap(Preprocessor.scaleBitmap(lv.get(current)));
			label.setText(String.format("%3d", current));
		} catch (Exception error) {
		}
	}

	/** File | Exit action performed */
	public void jMenuFileExit_actionPerformed(ActionEvent e) {
		System.exit(0);
	}

	/** Help | About action performed */
	/** Overridden so we can exit when window is closed */
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			jMenuFileExit_actionPerformed(null);
		}
	}
}
