import java.lang.reflect.InvocationTargetException;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class PrinterApplet extends JApplet {
	public void init() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					JPanel p = new JPanel();
					PrinterGui gui = new PrinterGui(p);
					//p.add(new JLabel("test"));
					//add(new JLabel("test"));
					getContentPane().add(p);
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}	
	}
}
