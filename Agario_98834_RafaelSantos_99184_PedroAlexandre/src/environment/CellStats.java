package environment;

import java.io.Serializable;

public class CellStats implements Serializable {

	private static final long serialVersionUID = -563835311766103325L;
	
	private byte strength;
	private boolean isHumanPlayer;
	private int playerID;
	
	public CellStats(byte strength, boolean isHumanPlayer, int playerID) {
		this.strength = strength;
		this.isHumanPlayer = isHumanPlayer;
		this.playerID = playerID;
	}
	
	public byte getStrength() {
		return strength;
	}
	
	public boolean isHumanPlayer() {
		return isHumanPlayer;
	}
	
	public void setHumanPlayer(boolean isHumanPlayer) {
		this.isHumanPlayer = isHumanPlayer;
	}
		
	public void setStrength(byte strength) {
		this.strength = strength;
	}
	
	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}
	
	public int getPlayerID() {
		return playerID;
	}
	
}

	

