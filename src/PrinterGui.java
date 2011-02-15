import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class PrinterGui extends JFrame implements Printable {
    private JTextField ticketField; 
    private JButton submitButton;
    private JComboBox combo;
	
    private Properties prop = new Properties(); //container for properties
	
    private Map<String, String> data;
	
    private String field_header;
    private String field_description;
    private String field_footer;
	
	boolean loadingDone = false;
	
	/**
	 * Set up Swing GUI
	 */
	public PrinterGui() {
		loadProperties();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Container panel = getContentPane();
		LayoutManager lm = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(lm);
		
		JLabel label = new JLabel("Enter Ticket ID");
		ticketField = new JTextField();
		submitButton = new JButton("Print Ticket");
		
		String[] projects = prop.getProperty("trac_project").split("\\,");
		combo = new JComboBox(projects);
		JLabel projectLabel = new JLabel("Select Project");
		
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getTicketContents();
			}
		};
		
		KeyListener kl = new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					getTicketContents();
				}
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
			}
		};
		
		submitButton.addActionListener(al);
		ticketField.addKeyListener(kl);
		ticketField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
		
		int row = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = row;
		c.gridwidth = 1;
		c.weightx = 0.75;
		panel.add(projectLabel, c);
		
		c.gridx = 1;
		c.gridy = row;
		c.gridwidth = 2;
		c.weightx = 0.25;
		panel.add(combo, c);
		
		row++;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = row;
		c.weighty = 1;
		panel.add(label, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = row;
		c.weightx = 0.75;
		c.weighty = 1;
		panel.add(ticketField, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = row;
		c.weightx = 0.25;
		panel.add(submitButton, c);

		row++;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = row;
		c.gridwidth = 3;
		panel.add(new JLabel("TracPrinter: www.any-where.de"), c);

		this.setTitle("TRAC Ticlet Printer");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ticketField.transferFocus();
		
		pack();
		setVisible(true);
	}
	
	/**
	 * Load properties
	 */
	private void loadProperties() {
		try {
			prop.load(new FileInputStream("printer.properties"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Try to load ticket info by URL and start printing routine
	 */
	public void getTicketContents() {
		data = new HashMap<String, String>();
		try {
			URL url = new URL(prop.getProperty("trac_url") + combo.getSelectedItem() + "/ticket/" + ticketField.getText() + "?format=tab");
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String header = in.readLine();
			String[] keys = header.split("\t");
			String complete = "";
			String line = in.readLine();
			
			while(line != null) {
				complete += (line + " ");
				line = in.readLine();
			}
			
			String[] values = complete.split("\t");
			
			for(int i=0; i<keys.length; i++) {
				if(i < values.length) {
					data.put(keys[i], values[i]);
				}
			}
			
			field_header = prop.getProperty("field_header");
			field_description = prop.getProperty("field_description");
			field_footer = prop.getProperty("field_footer");
			
			for (String key : keys) {
				field_header = field_header.replaceAll("\\{" + key + "\\}", data.get(key));
				field_description = field_description.replaceAll("\\{" + key + "\\}", data.get(key));
				field_footer = field_footer.replaceAll("\\{" + key + "\\}", data.get(key));
			}
			loadingDone = true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(loadingDone) {
			printTicket();			
		}
	}

	/**
	 * Show print dialog
	 */
	private void printTicket() {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(this);
		
		if(printJob.printDialog()) {
			try {
				printJob.print();
			} catch (PrinterException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		if(pageIndex == 0) {
			Graphics2D g = (Graphics2D) graphics;
			g.setColor(Color.black);
			g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			
			int width = Integer.parseInt(prop.getProperty("ticket_width"));
			int height = Integer.parseInt(prop.getProperty("ticket_height"));
			int font_size_header = Integer.parseInt(prop.getProperty("font_size_header"));
			int font_size_description = Integer.parseInt(prop.getProperty("font_size_description"));
			
			int padding_text_left = Integer.parseInt(prop.getProperty("padding_text_left"));
			int distance_header_top = Integer.parseInt(prop.getProperty("distance_header_top"));
			
			graphics.drawRect(1, 1, width, height);
			graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, font_size_header));
			graphics.drawString(field_header, padding_text_left, distance_header_top);

			Font font = new Font(Font.SANS_SERIF, Font.PLAIN, font_size_description);
			graphics.setFont(font);
			
			String[] words = field_description.split(" ");
			String line = "";
			
			int pos = 0;
			int chars_per_line = Integer.parseInt(prop.getProperty("chars_per_line"));
			int line_height = Integer.parseInt(prop.getProperty("line_height"));
			int max_lines = Integer.parseInt(prop.getProperty("max_lines"));
			int distance_description_top = Integer.parseInt(prop.getProperty("distance_description_top"));
			int line_counter = 0;
			
			//build lines
			for (String word : words) {
				if(line_counter == 0 && word.startsWith("\"")) {
					word = word.substring(1);
				}
				if(word.endsWith("\"")) {
					word = word.substring(0, word.length()-1);
				}
				if(line_counter < max_lines) {
					line = line + word + " ";
					pos = pos + word.length();
					if(pos > chars_per_line) {
						graphics.drawString(line, padding_text_left, distance_description_top + line_counter * line_height);
						pos = 0;
						line_counter++;
						line = "";
					}
				}
			}
			
			graphics.drawString(line + "...", padding_text_left, distance_description_top + line_counter * line_height);
			
			graphics.drawString(field_footer, padding_text_left, height - 50 + 20);
			graphics.drawString(prop.getProperty("trac_url") + prop.getProperty("trac_project") + "/ticket/" + ticketField.getText(), padding_text_left, height - 50 + 20 + line_height);
			
			graphics.drawLine(1, 50, width, 50);
			graphics.drawLine(1, height-50, width, height-50);
			
			return PAGE_EXISTS;
		} else {
			return NO_SUCH_PAGE;
		}
	}
}
