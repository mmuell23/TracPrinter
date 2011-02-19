package de.tracprinter;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;


public class PrinterGui extends JFrame {
    private JTextField ticketField; 
    private JButton submitButton;
    private JComboBox combo;
	
    private Properties prop = new Properties(); //container for properties
	
    private Map<String, String> data;
	
	private Stack<BufferedImage> images;
	private List<Map<String, String>> toPrint;
	
	private int width;
	private int height;
	
	private static String user = null;
	private static String pass = null;
	
    static class TracAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return (new PasswordAuthentication(user, pass.toCharArray()));
        }
    }	
	
	/**
	 * Set up Swing GUI
	 */
	public PrinterGui() {
		loadPropertiesFromFile();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Container panel = getContentPane();
		arrangeElements(panel);
		init();
		
		this.setTitle("TracPrinter");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		pack();
		setVisible(true);
	}
	
	/**
	 * Set up GUI for applet
	 * @param panel
	 * @param propertiesUrl
	 */
	public PrinterGui(Container panel) {
		loadProperties();
		arrangeElements(panel);
		init();
	}
	
	private void init() {
	    toPrint = new ArrayList<Map<String,String>>();
	    images = new Stack<BufferedImage>();
	}
	
	private void arrangeElements(Container panel) {
		LayoutManager lm = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(lm);
		
		JLabel label = new JLabel("Enter Ticket IDs");
		ticketField = new JTextField();
		ticketField.setToolTipText("Enter Ticket IDs. Eg. id1,id2,id3,...");
		submitButton = new JButton("Print Ticket");
		
		String[] projects = prop.getProperty("trac_project").split("\\,");
		combo = new JComboBox(projects);
		JLabel projectLabel = new JLabel("Select Project");
		
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				startProcessing();
			}
		};
		
		KeyListener kl = new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					startProcessing();
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
		c.weightx = 0.5;
		panel.add(projectLabel, c);
		
		c.gridx = 1;
		c.gridy = row;
		c.gridwidth = 2;
		c.weightx = 0.5;
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
		c.weightx = 0.5;
		c.weighty = 1;
		panel.add(ticketField, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = row;
		c.weightx = 0.5;
		panel.add(submitButton, c);

		row++;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = row;
		c.gridwidth = 3;
		JLabel urlLabel = new JLabel("TracPrinter: www.any-where.de");
		urlLabel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				System.out.println("Mouse released.");
				Desktop d = null;
				if(Desktop.isDesktopSupported()) {
					d = Desktop.getDesktop();
					try {
						URI uri = new URI("http://www.any-where.de");
						d.browse(uri);	
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
			}
		});
		panel.add(urlLabel, c);

		ticketField.transferFocus();		
	}
	
	private void startProcessing() {
	    String[] ids = ticketField.getText().split(",");
	    for (String ticketId : ids) {
	        getTicketContents(ticketId.trim());
        }
	    createTicketImage();
	    
	    BufferedImage image1 = null;
	    BufferedImage image2 = null;
	    
	    if(!images.isEmpty()) {
	    	image1 = images.pop();
	    }
	    if(!images.isEmpty()) {
	    	image2 = images.pop();
	    }
	    
	    Book book = new Book();
	    PageFormat documentPageFormat = new PageFormat();
	    documentPageFormat.setOrientation(PageFormat.PORTRAIT);
	    
	    while(image1 != null || image2 != null) {
	    	TicketCard tc = new TicketCard(image1, image2);
	    	if(!images.isEmpty()) {
	    		image1 = images.pop();
	    	} else {
	    		image1 = null;
	    	}
		    if(!images.isEmpty()) {
		    	image2 = images.pop();
		    } else {
		    	image2 = null;
		    }
		    
		    book.append(tc, documentPageFormat);
	    }
	    
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPageable(book);

        if(printJob.printDialog()) {
            try {
                printJob.print();
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }	    
	}

	/**
	 * Load properties
	 */
	private void loadProperties() {
		try {
			prop.load(getClass().getResourceAsStream("/printer.properties"));
			setUserAndPass();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void loadPropertiesFromFile() {
		try {
			prop.load(new FileInputStream(new File("printer.properties")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadPropertiesFromUrl(String propertiesUrl) {
		try {
			URL properties = new URL(propertiesUrl);
			prop.load(properties.openStream());
			setUserAndPass();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
	}
	
	private void setUserAndPass() {
	    if(!prop.getProperty("trac_user").equals("")) {
	        user = prop.getProperty("trac_user");
	        pass = prop.getProperty("trac_pass");
	    }
	}
	
	/**
	 * Try to load ticket info by URL and start printing routine
	 */
	public void getTicketContents(String ticketId) {
		data = new HashMap<String, String>();
		try {
		    
		    if(user != null && pass != null) {
		        Authenticator.setDefault(new TracAuthenticator());
		    }
		    
			URL url = new URL(prop.getProperty("trac_url") + combo.getSelectedItem() + "/ticket/" + ticketId + "?format=tab");
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
			
			String field_header = prop.getProperty("field_header");
			String field_description = prop.getProperty("field_description");
			String field_footer = prop.getProperty("field_footer");
			
			for (String key : keys) {
			    field_header = field_header.replaceAll("\\{" + key + "\\}", data.get(key));
				field_description = field_description.replaceAll("\\{" + key + "\\}", data.get(key));
				field_footer = field_footer.replaceAll("\\{" + key + "\\}", data.get(key));
			}
			
			Map<String, String> d = new HashMap<String, String>();
			d.put("field_header", field_header);
			d.put("field_description", field_description);
			d.put("field_footer", field_footer);
			d.put("ticketId", ticketId);
			toPrint.add(d);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch  (Exception e) {
		    e.printStackTrace();
		}
	}

	/**
	 * Show print dialog
	 */
	private void createTicketImage() {
		String suffix = "jpg";
		
		//Store card as file
        width = Integer.parseInt(prop.getProperty("ticket_width"));
        height = Integer.parseInt(prop.getProperty("ticket_height"));		
		boolean keepFile = Boolean.parseBoolean(prop.getProperty("keep_image_as_file"));
		
		for (Map<String, String> data : toPrint) {
		    BufferedImage ticketCard = new BufferedImage(width + 100, height + 100, BufferedImage.TYPE_INT_RGB);
            createGraphics(ticketCard.createGraphics(), data);
            
            try {
                Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpeg");
                if (!writers.hasNext())
                    throw new IllegalStateException("No writers found");
                
                ImageWriter writer = (ImageWriter) writers.next();
                File file = new File(data.get("ticketId") + "." + suffix);
                ImageOutputStream ios = ImageIO.createImageOutputStream(new FileOutputStream(file));
                
                writer.setOutput(ios);
                
                ImageWriteParam param = writer.getDefaultWriteParam();
                
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(1.0f);
                
                writer.write(null, new IIOImage(ticketCard, null, null), param);
                
                if(!keepFile) {
                    file.delete();
                }
                images.push(ticketCard);
            } catch (IOException e1) {
                e1.printStackTrace();
            }  
        }

	}

	/**
	 * Paint ticket card
	 * @param graphics graphics object
	 * @param data text elements
	 */
	public void createGraphics(Graphics2D graphics, Map<String, String> data) {
	    int offset = 10;
	    
	    String field_header = data.get("field_header");
	    String field_description = data.get("field_description");
	    String field_footer = data.get("field_footer");
	    
	    graphics.setColor(Color.WHITE);
	    graphics.fillRect(0, 0, 10000,10000);
	    graphics.setColor(Color.BLACK);
	    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    
        int font_size_header = Integer.parseInt(prop.getProperty("font_size_header"));
        int font_size_footer = Integer.parseInt(prop.getProperty("font_size_footer"));
        int font_size_description = Integer.parseInt(prop.getProperty("font_size_description"));
        
        int max_chars_headline = Integer.parseInt(prop.getProperty("max_chars_headline"));
        int padding_text_left = Integer.parseInt(prop.getProperty("padding_text_left")) + offset;
        int distance_header_top = Integer.parseInt(prop.getProperty("distance_header_top")) + offset;
        int distance_footer_top = Integer.parseInt(prop.getProperty("distance_footer_top")) + offset;
        int distance_description_top = Integer.parseInt(prop.getProperty("distance_description_top")) + offset;
        
        int distance_line_top = Integer.parseInt(prop.getProperty("distance_line_top")) + offset;
        int distance_line_bottom = Integer.parseInt(prop.getProperty("distance_line_bottom")) + offset;

    	int max_chars_description = Integer.parseInt(prop.getProperty("max_chars_description"));
        
        int line_height_description = Integer.parseInt(prop.getProperty("line_height_description"));
        int line_height_footer = Integer.parseInt(prop.getProperty("line_height_footer"));
        
        int max_lines = Integer.parseInt(prop.getProperty("max_lines"));
        
        graphics.drawRect(offset, offset, width, height);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, font_size_header));
        if(field_header.length() > max_chars_headline) {
            field_header = field_header.substring(0, max_chars_headline - 3);
        }
        
        graphics.drawString(field_header, padding_text_left, distance_header_top);
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, font_size_description);
        graphics.setFont(font);
        
        String[] words = field_description.split(" ");
        String line = "";
        
        int pos = 0;
        
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
                if(pos + word.length() > max_chars_description) {
                    graphics.drawString(line, padding_text_left, distance_description_top + line_counter * line_height_description);
                    line_counter++;
                    line = word + " ";
                    pos = line.length();
                } else {
                    line = line + word + " ";
                    pos = pos + word.length();
                }
            }
        }
        
        graphics.drawString(line, padding_text_left, distance_description_top + line_counter * line_height_description);
        
        line_counter = 0;
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, font_size_footer));
        graphics.drawString(field_footer, padding_text_left, distance_footer_top);
        graphics.drawString(prop.getProperty("trac_url") + combo.getSelectedItem() + "/ticket/" + data.get("ticketId"), padding_text_left, distance_footer_top + line_height_footer);
        
        graphics.drawLine(offset, distance_line_top, width + offset, distance_line_top);
        graphics.drawLine(offset, distance_line_bottom, width + offset, distance_line_bottom);	    
	}	
}
