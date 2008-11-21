package mediacenter;


import javax.swing.JFrame;


/**
 * Main application.
 * 
 * @author Oscar Stigter
 */
public class Main extends JFrame implements Application {
    
    
    /** Serial version UID. */
    private static final long serialVersionUID = -1L;
    
    /** The screens. */
    private final Screen[] screens = {
            new MainScreen(this),
            new VideoScreen(this),
            new MusicScreen(this),
            new PicturesScreen(this),
    };
    
    /** The current screen. */
    private Screen screen;
	
	
    /**
     * Constructor.
     */
    public Main() {
	    super("Media Center");
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
        setScreen(Constants.MAIN);
	}
	
	
    /**
     * The application's entry point.
     * 
     * @param  args  command line arguments
     */
    public static void main(String[] args) {
		new Main();
	}
	
	
	public void setScreen(int index) {
	    if (index < 0 || index > screens.length - 1) {
	        throw new IllegalArgumentException(
	                "Invalid screen index: " + index);
	    }
	    
	    if (screen != null) {
	        getContentPane().remove(screen);
	    }
	    screen = screens[index];
        getContentPane().add(screen);
        validate();
        repaint();
	}
	
	
	public void back() {
	    setScreen(Constants.MAIN);
	}
	
	
    public void minimize() {
        setExtendedState(getExtendedState() | JFrame.ICONIFIED);
    }


	public void exit() {
		dispose();
	}


}
