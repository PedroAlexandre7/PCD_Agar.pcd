package game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;

import environment.Cell;
import environment.Coordinate;
import environment.Direction;
import environment.CellStats;

@SuppressWarnings("deprecation")
public class Game extends Observable {

	public static final int CELL_SIZE = 40, DIMY = 25, DIMX = 25, NUM_PLAYERS = 50, NUM_BOTS = 50, NUM_FINISHED_PLAYERS_TO_END_GAME = 3;
	public static final long REFRESH_INTERVAL = 100, MAX_WAITING_TIME_FOR_SPAWN = 4000, MAX_WAITING_TIME_FOR_MOVE = 2000, INITIAL_WAITING_TIME = 10000,
			DEADLOCK_WAITING_TIME = REFRESH_INTERVAL * 10;
	public static final byte MAX_INITIAL_STRENGTH = 3, MAX_STRENGTH = 10;

	public static final int SERVER_PORT = 8080;
	private ServerSocket serversocket;

	private CountDownLatch cdl = new CountDownLatch(NUM_FINISHED_PLAYERS_TO_END_GAME);
	private boolean hasStarted = false;
	private Cell[][] board;

	private ArrayList<Player> players = new ArrayList<>();
	private ArrayList<Player> humanPlayers = new ArrayList<>();
	private ArrayList<ObjectOutputStream> outs = new ArrayList<>();

	public Game() {
//		try {
//			serversocket = new ServerSocket(SERVER_PORT);
//		} catch (IOException e) {
//			System.err.println("SERVER: Cannot initialize server... aborting!");
//			System.exit(1);
//		}
		board = new Cell[Game.DIMX][Game.DIMY];
		for (int x = 0; x < Game.DIMX; x++)
			for (int y = 0; y < Game.DIMY; y++)
				board[x][y] = new Cell(new Coordinate(x, y), this);
	}

	public void startGame() throws InterruptedException {
		new CountDownThread().start();

		System.out.println("Creating bots...");
		for (int i = 0; i < NUM_BOTS; i++)
			players.add(new BotPlayer(this));

		// TODO Fase 6: criar e iniciar thread que aceita jogadores.

		System.out.println("Awaiting human players...");

		GameServer server = new GameServer();
		server.start();

		Thread.sleep(INITIAL_WAITING_TIME);
		for (Player player : players)
			player.start();

		// matar recebedor de jogadores
		System.out.println("Starting game...");
		synchronized (this) {
			hasStarted = true;
			notifyAll();
		}

		GameServer2 server2 = new GameServer2();
		server2.start();
		// TODO QUANDO ACABAR Close outs

	}

	private class GameServer extends Thread {
		@Override
		public void run() {
			try {
				serversocket = new ServerSocket(Game.SERVER_PORT);
			} catch (IOException e) {
				System.err.println("SERVER: Cannot initialize server... aborting!");
				System.exit(1);
			}

			startAcceptingPlayers();
		}

		public void startAcceptingPlayers() {
			while (!Game.this.hasStarted) {
				try {
					waitForConnection();
				} catch (IOException e) {
					System.out.println("IOException");
				}
			}

			System.out.println("Stoped Accepting Players");
		}

		public void waitForConnection() throws IOException {
			System.out.println("SERVER: Waiting for connection...");
			serversocket.setSoTimeout((int) INITIAL_WAITING_TIME);
			System.out.println("something");
			Socket socket = serversocket.accept();

			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			HumanPlayer p = new HumanPlayer(Game.this, (byte) 3);
			ConnectionHandler handler = new ConnectionHandler(p, in);
			handler.start();

			Game.this.addHumanPlayer(p);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//			lock.lock();
//			try {
			outs.add(out);
//			} finally {
//				lock.unlock();
//			}

		}
	}

	private class GameServer2 extends Thread {

		public GameServer2() {

		}

		@Override
		public void run() {
//			startAcceptingPlayers();
			System.out.println("Começar a enviar");
			sendCellStatMatrixPeriodically();
		}

		private void sendCellStatMatrixPeriodically() {
			while (!Game.this.isOver())
				sendCellStatMatrix();
			try {
				Thread.sleep(Game.REFRESH_INTERVAL);
			} catch (InterruptedException e) {
			}
			sendCellStatMatrix();

		}

		private void sendCellStatMatrix() {
			CellStats[][] matrix = createCellStatMatrix();
			for (ObjectOutputStream out : outs)
				try {
					out.writeObject(matrix);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			try {
				Thread.sleep(Game.REFRESH_INTERVAL / 5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private CellStats[][] createCellStatMatrix() {
			CellStats[][] csMatrix = new CellStats[Game.DIMX][Game.DIMY];
			for (Player p : players) {
				Cell cell = p.getCurrentCell();
				if (cell != null)
					csMatrix[cell.getPosition().x][cell.getPosition().y] = cell.convert();
			}
			return csMatrix;
		}

	}

	private class ConnectionHandler extends Thread {
		private HumanPlayer player;
		private ObjectInputStream in;

		public ConnectionHandler(HumanPlayer p, ObjectInputStream in) {
			player = p;
			this.in = in;
		}

		@Override
		public void run() {
			try {
				getStreams();
				processConnection();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				closeConnection();
			}

		}

		public void getStreams() throws IOException {

		}

		public void processConnection() throws IOException, ClassNotFoundException {
			System.out.println("Connection accepted!");
			while (true) {
				player.setCurrentDirection((Direction) in.readObject());
			}
		}

		public void closeConnection() {
			try {
				in.close();
//				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private class CountDownThread extends Thread { // <- Perguntar ao stor se é preciso criar esta classe.

		@Override
		public void run() {
			try {
				cdl.await();
				System.out.println("o cdl acabou");
				for (Player p : players)
					p.interrupt();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void addHumanPlayer(HumanPlayer p) {
		players.add(p);
		humanPlayers.add(p);
	}

	public synchronized void waitForStart() {
		try {
			while (!hasStarted)
				wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void countDown() {
		cdl.countDown();
	}

	public boolean isOver() {
		return cdl.getCountDown() == 0;
	}

	public Cell getCell(Coordinate at) {
		return board[at.x][at.y];
	}

	public static boolean isWithinBounds(Coordinate c) {
		return (c.x >= 0 && c.x < DIMX && c.y >= 0 && c.y < DIMY);
	}

	// Updates GUI. Should be called anytime the game state changes
	public void notifyChange() {
		setChanged();
		notifyObservers();
	}

	public Cell getRandomCell() {
		Cell newCell = getCell(new Coordinate((int) (Math.random() * Game.DIMX), (int) (Math.random() * Game.DIMY)));
		return newCell;
	}

}