import java.lang.reflect.InvocationTargetException;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.tracprinter.PrinterGui;

public class PrinterApplet extends JApplet {
	public void init() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					JPanel p = new JPanel();
					PrinterGui gui = new PrinterGui(p, getParameter("properties"));
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
