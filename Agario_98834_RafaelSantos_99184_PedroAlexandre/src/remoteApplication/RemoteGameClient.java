package remoteApplication;

import game.Game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Observable;

import environment.CellStats;

@SuppressWarnings("deprecation")
public class RemoteGameClient extends Observable {

	public static final String SERVER_ADRESS = "127.0.0.1";
	public static final String SERVER_ADRESS_ONLINE = "25.53.35.144";

	private BoardJComponent boardGui;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Socket socket;
//	private int id;

	public RemoteGameClient(BoardJComponent boardGui) {
		this.boardGui = boardGui;
	}

	public void runRemoteGameApp() {
		try {
			System.out.println("Connecting to server");
			connectToServer();
			System.out.println("Connectected to server");
			ScreenUpdaterThread updaterThread = new ScreenUpdaterThread();
			updaterThread.start();
			sendPlayerInput();
		} catch (IOException e) {// ERRO...
		} finally {// a fechar...
			try {
				socket.close();
			} catch (IOException e) {// ...
			}
		}
	}

	private void connectToServer() throws IOException {
		InetAddress endereco = InetAddress.getByName(SERVER_ADRESS);
		System.out.println("Endereco:" + endereco);
		socket = new Socket(endereco, Game.SERVER_PORT);
		socket.setSoTimeout(5000);
		System.out.println("Socket:" + socket);
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}

	public void sendPlayerInput() throws IOException {

		while (true)
			synchronized (boardGui) {
				try {
					boardGui.wait();
				} catch (InterruptedException e) {
				}
				out.writeObject(boardGui.getLastPressedDirection());
			}
	}

//	public CellStats getCellStats(Coordinate c) {
//		return gameCells[c.x][c.y];
//	}

	private class ScreenUpdaterThread extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					boardGui.setGameCells((CellStats[][]) in.readObject());
					if (!socket.isConnected())
						break;
					setChanged();
					notifyObservers();
				} catch (SocketTimeoutException e) {
				} catch (IOException e) {
					break;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			}
		}
	}

}
