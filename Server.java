import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;


public class Server /*extends UnicastRemoteObject*/ implements ServerInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5259766376291783211L;
	ArrayList<Coordinates> blackStone = new ArrayList<Coordinates>();
	ArrayList<Coordinates> whiteStone = new ArrayList<Coordinates>();
	Coordinates mustBeatAgain;
	
	PlayerInterface playerBlack;
	PlayerInterface playerWhite;
	boolean isBlacksTurn;
	boolean isPlayerInZugzwang=false;
	
	protected Server() throws RemoteException {
	}
	
	public synchronized boolean login(PlayerInterface p) throws RemoteException{
		if (playerWhite==null){
			playerWhite=p;
			playerWhite.makeMeBlack(false);
			return true;
		}
		else if (playerBlack==null) {
			playerBlack=p;
			playerBlack.makeMeBlack(true);
			for (int i=0;i<=2;i++){
				for (int o=i%2==0?1:0;o<=9;o+=2){
					playerBlack.setPiece(o, i, true);
					playerWhite.setPiece(o, i, true);
					blackStone.add(new Coordinates(o,i));
				}
			}
			
			for (int i=7;i<=9;i++){
				for (int o=i%2==0?1:0;o<=9;o+=2){
					playerBlack.setPiece(o, i, false);
					playerWhite.setPiece(o, i, false);
					whiteStone.add(new Coordinates(o,i));
				}
			}

			isBlacksTurn = false;
			playerWhite.myTurn(true,false);

			return true;
		}
		return false;
	}
	
	private boolean isInZugzwang(boolean black){
		if (black){
			for (Coordinates p: blackStone)
				if (canBeat(p.getX(), p.getY(), true)) return true;
		} else{
			for (Coordinates p: whiteStone)
				if (canBeat(p.getX(), p.getY(), false)) return true;
		}	
		return false;
	}
	
	private boolean canBeat(int x, int y, boolean black){
		if(black){
			if (isThereAPieceAnyway(x-1, y+1, false) && isAccessable(x-2, y+2)) return true;
			if (isThereAPieceAnyway(x+1, y+1, false) && isAccessable(x+2, y+2)) return true;
		}
		else {
			if (isThereAPieceAnyway(x-1, y-1, true) && isAccessable(x-2, y-2)) return true;
			if (isThereAPieceAnyway(x+1, y-1, true) && isAccessable(x+2, y-2)) return true;
		}
		return false;
		
	}
	
	private boolean isThereAPieceAnyway(int x, int y, boolean black){
		if (black){
			for (Coordinates p: blackStone)
				if (p.getX() == x && p.getY()==y) return true;
		} else {
			for (Coordinates p: whiteStone)
				if (p.getX() == x && p.getY()==y) return true;
		}
		return false;
	}
	
	private Coordinates pleaseHandMeThatPiece(int x, int y, boolean black){
		if (black){
			for (Coordinates p: blackStone)
				if (p.getX() == x && p.getY()==y) return p;
		} else {
			for (Coordinates p: whiteStone)
				if (p.getX() == x && p.getY()==y) return p;
		}
		return null;
	}
	private boolean isAccessable(int x,int y){
		if (x<0 || x>=10 || y<0 || y>10)
			return false;
		if (isThereAPieceAnyway(x, y, true) || isThereAPieceAnyway(x, y, false)) return false;
		return true;
	}
	
	public synchronized boolean movePiece(int x, int y, int xt, int yt, boolean black) throws RemoteException{
		if (black==isBlacksTurn && !(x>=10 || x<0 || y>=10 || y<0)){
			if (isBlacksTurn && yt==y+1 && (xt==x-1 || xt==x+1) && isAccessable(xt, yt)  && !isPlayerInZugzwang){
				Coordinates tmpCoord = pleaseHandMeThatPiece(x, y, true);
				if (tmpCoord == null) return false;
				tmpCoord.setX(xt);
				tmpCoord.setY(yt);
				playerWhite.movePiece(x, y, xt, yt);
				playerBlack.movePiece(x, y, xt, yt);
				isPlayerInZugzwang = isInZugzwang(false);
				isBlacksTurn=false;
				
				playerWhite.myTurn(true,isPlayerInZugzwang);
				playerBlack.myTurn(false,false);
				return true;
			}
			
			
			if (isBlacksTurn && yt==y+2 && ((xt==x+2 && isThereAPieceAnyway(x+1, y+1, false)) || (xt==x-2 && isThereAPieceAnyway(x-1, y+1, false))) && isAccessable(xt, yt) && (mustBeatAgain==null || pleaseHandMeThatPiece(x, y, true)==mustBeatAgain)){
				Coordinates tmpCoord = pleaseHandMeThatPiece(x, y, true);
				if (tmpCoord == null) return false;
				tmpCoord.setX(xt);
				tmpCoord.setY(yt);
				playerWhite.movePiece(x, y, xt, yt);
				playerBlack.movePiece(x, y, xt, yt);
				remove(pleaseHandMeThatPiece(xt==x+2?x+1:x-1, y+1, false), false);
				if(canBeat(xt, yt, true)){
					mustBeatAgain=tmpCoord;
					playerBlack.again();
				}
				else{
					mustBeatAgain=null;
					isPlayerInZugzwang = isInZugzwang(false);
					isBlacksTurn=false;
					playerWhite.myTurn(true,isPlayerInZugzwang);
					playerBlack.myTurn(false, false);		
				}
				checkWon();
				return true;
				}
		if (!isBlacksTurn && yt==y-1 && (xt==x-1 || xt==x+1)  && isAccessable(xt, yt) && !isPlayerInZugzwang){
			Coordinates tmpCoord = pleaseHandMeThatPiece(x, y, false);
			if (tmpCoord == null) return false;
			tmpCoord.setX(xt);
			tmpCoord.setY(yt);
			playerWhite.movePiece(x, y, xt, yt);
			playerBlack.movePiece(x, y, xt, yt);
			isPlayerInZugzwang = isInZugzwang(true);
			isBlacksTurn=true;
			playerBlack.myTurn(true,isPlayerInZugzwang);
			playerWhite.myTurn(false, false);
			return true;
		}
		
		if (!isBlacksTurn && yt==y-2 && ((xt==x+2 && isThereAPieceAnyway(x+1, y-1, true)) || (xt==x-2 && isThereAPieceAnyway(x-1, y-1, true))) && isAccessable(xt, yt) && (mustBeatAgain==null || pleaseHandMeThatPiece(x, y, false)==mustBeatAgain)){
			Coordinates tmpCoord = pleaseHandMeThatPiece(x, y, false);
			if (tmpCoord == null) return false;
			tmpCoord.setX(xt);
			tmpCoord.setY(yt);
			playerWhite.movePiece(x, y, xt, yt);
			playerBlack.movePiece(x, y, xt, yt);
			remove(pleaseHandMeThatPiece(xt==x+2?x+1:x-1, y-1, true), true);
			if(canBeat(xt, yt, false)){
				mustBeatAgain=tmpCoord;
				playerWhite.again();
			}
			else{
				mustBeatAgain=null;
				isPlayerInZugzwang = isInZugzwang(true);
				isBlacksTurn=true;
				playerBlack.myTurn(true,isPlayerInZugzwang);
				playerWhite.myTurn(false, false);		
			}
			checkWon();
			return true;
			}
		
		
		}
		return false;
	}
	
	private void remove(Coordinates c, boolean black) throws RemoteException{
		if (black) blackStone.remove(c);
		else whiteStone.remove(c);
		playerBlack.removePiece(c.getX(), c.getY());
		playerWhite.removePiece(c.getX(), c.getY());
	}
	
	private void checkWon() throws RemoteException{
		if (blackStone.isEmpty()){
			playerBlack.gameOver(false);
			playerWhite.gameOver(true);			
		}
		if (whiteStone.isEmpty()){
			playerBlack.gameOver(false);
			playerWhite.gameOver(true);			
		}
	}
	
	public static void main(String[] args) {
		try {
			Server serverEngine = new Server();
			final ServerInterface server = (ServerInterface) UnicastRemoteObject.exportObject(serverEngine, 1099);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("CheckerServer", server);
            System.out.println("Server bound");
          			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}
}