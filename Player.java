import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Player extends UnicastRemoteObject implements PlayerInterface{
	
	private static final long serialVersionUID = 8879660474359811389L;

	class Playstone extends JLabel{
		private static final long serialVersionUID = 5261924631951914962L;
		Coordinates whereAmI;
		boolean ownedByThisPlayer;
		
		
		Playstone(int x, int y, boolean black, boolean ownedByThisPlayer){
			super(black?blackPiece:whitePiece);
			whereAmI = new Coordinates(x,y);
			this.setCoordinates(x, y);
			this.ownedByThisPlayer = ownedByThisPlayer;
			if (ownedByThisPlayer){
				this.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (myTurn && !everyTurnIsTheSame && stoneInMyHand==null) {
							pickUp((Playstone)e.getSource());
							southLabel.setText("Waehlen Sie Ihr Ziel");
						}
					}
				});
			}
		}
		
		void setCoordinates(Coordinates c){
			setCoordinates(c.getX(),c.getY());
		}
		
		void setCoordinates(int x, int y){
			whereAmI.set(x,y);
			setBounds(((fieldLabel.getIcon().getIconWidth()-2)/10)*x+1,((fieldLabel.getIcon().getIconHeight()-2)/10)*y + 2,blackPiece.getIconWidth(), blackPiece.getIconHeight());
		}
		
		boolean areYouOneOfMine(){
			return ownedByThisPlayer;
		}

	}
	
	boolean everyTurnIsTheSame = false;
	boolean myTurn=false;
	boolean iAmBlack;
	boolean zugzwang;
	static ImageIcon blackPiece = new ImageIcon("black.png");
	static ImageIcon whitePiece = new ImageIcon("white.png");
	JLabel fieldLabel = new JLabel(new ImageIcon("grid.png"));
	Playstone stoneInMyHand;	
	ServerInterface server;
	HashMap<Integer, Playstone> stones = new HashMap<Integer, Playstone>();
	JLabel northLabel = new JLabel(" auf anderen Spieler");
	JLabel southLabel = new JLabel("");
	JFrame testFrame = new JFrame();
	
	void pickUp(Playstone p){
			stoneInMyHand = p;
		}
	
	Coordinates getCoordinates(int x, int y){
		if (x==191) x--;
		if (y==201) y--;
		return new Coordinates((x-1)/((fieldLabel.getIcon().getIconWidth()-2)/10),(y-1)/((fieldLabel.getIcon().getIconHeight()-2)/10));
	}

	
	Player() throws RemoteException, MalformedURLException, NotBoundException{
		testFrame = new JFrame("Taschen-Dame");
		ImageIcon playfield = new ImageIcon("grid.png");
		String serverURL = JOptionPane.showInputDialog(null,"Geben Sie die URL des Servers ein",
                "Eine Servereingabe",
                JOptionPane.WARNING_MESSAGE);
	
		if (serverURL==null) serverURL = "localhost";
		final int PORT = 1099;

		Registry registry = LocateRegistry.getRegistry(serverURL, PORT);
		server = (ServerInterface)registry.lookup("CheckersServer");
	
		
		
		//server = (ServerInterface)java.rmi.Naming.lookup("//"+serverURL+":1099/CheckersServer");
		testFrame.setSize(playfield.getIconWidth()+30, playfield.getIconHeight()+70);
		testFrame.setLayout(new BorderLayout());
		testFrame.add(northLabel,BorderLayout.NORTH);
		testFrame.add(southLabel,BorderLayout.SOUTH);
		JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout(null);
		fieldLabel.setBounds(10, 5, fieldLabel.getIcon().getIconWidth(), fieldLabel.getIcon().getIconHeight());
		server.login(this);
		fieldLabel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (stoneInMyHand!= null && e.getButton() == 3 && !everyTurnIsTheSame) {
					southLabel.setText("Waehlen Sie Ihren Stein");
					stoneInMyHand = null;}
				else if (stoneInMyHand != null && e.getButton()==1){
					Coordinates launch = stoneInMyHand.whereAmI;
					Coordinates target = getCoordinates(e.getX(), e.getY());
					try {
						server.movePiece(launch.getX(), launch.getY(), target.getX(), target.getY(),iAmBlack);
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
				}
				
	
		}
		});
		
		fieldPanel.add(fieldLabel);
		testFrame.add(fieldPanel, BorderLayout.CENTER);
		testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		testFrame.setResizable(false);
		testFrame.setVisible(true);

	}
	
	public void myTurn(boolean isIt, boolean zugzwang){
		myTurn=isIt;
		if (isIt){
			northLabel.setText((zugzwang?"Sie muessen Schlagen":"Sie sind am Zug"));
			southLabel.setText("Waehlen Sie Ihren Stein");
		}
		else {
			northLabel.setText("Warten auf den Mitspieler");
			southLabel.setText("");
			stoneInMyHand = null;
			myTurn = false;			
			everyTurnIsTheSame = false;
		}
	}
	
	public void setPiece(int x, int y, boolean black){
		Playstone setStone = new Playstone(x, y, black, (iAmBlack==black));
		stones.put(x*10+(y+1),setStone);
		fieldLabel.add(setStone);
		testFrame.repaint();
	}
	
	public void movePiece(int x, int y, int xt, int yt){
		Playstone toMove = stones.get(x*10+(y+1));
		stones.remove(toMove);
		toMove.setCoordinates(xt, yt);
		stones.put(xt*10+(yt+1), toMove);
	}
	
	public void removePiece(int x, int y){
		fieldLabel.remove(stones.get(x*10+(y+1)));
		stones.remove(stones.get(x*10+(y+1)));
	}
	
	public void makeMeBlack(boolean black){
		this.iAmBlack = black;
		testFrame.setTitle("Dame - " + (black?"Schwarz":"Weiß"));	
	}
	
	public void again(){
		northLabel.setText("Selber Stein nochmal");
		everyTurnIsTheSame = true;
	}

	public void gameOver(boolean won){
		if (won)
            JOptionPane.showMessageDialog(null,
                    "Sie haben gewonnen",
                    "Glueckwunsch",                                       
                    JOptionPane.WARNING_MESSAGE);
		else
            JOptionPane.showMessageDialog(null,
                    "Sie haben verloren",
                    "Schade",                                       
                    JOptionPane.WARNING_MESSAGE);
		System.exit(0);
	}
	
	public static void main(String[] args) {
		try {
			@SuppressWarnings("unused")
			Player test = new Player();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}