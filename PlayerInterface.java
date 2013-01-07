import java.rmi.RemoteException;

public interface PlayerInterface extends java.rmi.Remote{
	public void makeMeBlack(boolean black) throws RemoteException;
	public void setPiece(int x, int y, boolean black) throws RemoteException;
	public void myTurn(boolean isIt, boolean zugzwang) throws RemoteException;
	public void movePiece(int x, int y, int xt, int yt) throws RemoteException;
	public void again() throws RemoteException;
	public void gameOver(boolean won) throws RemoteException;
	public void removePiece(int x, int y) throws RemoteException;

}
