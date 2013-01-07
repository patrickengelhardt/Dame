import java.rmi.RemoteException;

public interface ServerInterface extends java.rmi.Remote{
	public boolean login(PlayerInterface p) throws RemoteException;
	public boolean movePiece(int x, int y, int xt, int yt, boolean black) throws RemoteException;
}
