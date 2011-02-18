import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Printable ticket card. Holds max. 2 Tickets
 */
public class TicketCard implements Printable {

	private List<BufferedImage> images;
	
	public TicketCard(BufferedImage img1, BufferedImage img2) {
		images = new ArrayList<BufferedImage>();
		if(img1 != null) {
			images.add(img1);
		}
		if(img2 != null) {
			images.add(img2);
		}
	}
	
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
    	System.out.println("Drucke " + pageIndex);
    	
    	//no image? stop here
    	if(images.size() == 0) {
    		return NO_SUCH_PAGE;
    	}
    	
        Graphics2D g = (Graphics2D) graphics;
        g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        int ticketCounter = 0;
        int offset = 0;
        double scale = 0.45;
        
        int counter = 0;
        
        for (BufferedImage ticketCard : images) {
	        offset = (int)(ticketCounter * ticketCard.getHeight() * scale);
	        
	        int w = (int) (scale * ticketCard.getWidth());
	        int h = (int) (scale * ticketCard.getHeight()); 
            
            graphics.drawImage(ticketCard, 0, 50 + offset, w, h, null);   
            ticketCounter++;
            counter++;				
		}
        
		return PAGE_EXISTS;
	}
}
