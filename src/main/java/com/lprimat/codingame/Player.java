package com.lprimat.codingame;
import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Shoot enemies before they collect all the incriminating data!
 * The closer you are to an enemy, the more damage you do but don't get too close or you'll get killed.
 **/
class Player {

    public static void main(String args[]) {
    	String input = "1100;1200;1;0;8250;4500;1;0;8250;8999;10";
    	Scanner in = new Scanner(input).useDelimiter(";");

        // game loop
        while (true) {
            int x = in.nextInt();
            int y = in.nextInt();
            int dataCount = in.nextInt();
            for (int i = 0; i < dataCount; i++) {
                int dataId = in.nextInt();
                int dataX = in.nextInt();
                int dataY = in.nextInt();
            }
            int enemyCount = in.nextInt();
            for (int i = 0; i < enemyCount; i++) {
                int enemyId = in.nextInt();
                int enemyX = in.nextInt();
                int enemyY = in.nextInt();
                int enemyLife = in.nextInt();
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println("MOVE 8000 4500"); // MOVE x y or SHOOT id
        }
    }
}

class Position {
	int x;
	int y;

	public Position(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
}

class Data {
	int id;
	Position pos;
	
	public Data(int id, int x, int y) {
		super();
		this.id = id;
		this.pos = new Position(x, y);
	}
}

class Joueur {
	Position pos;
	Enemy target;
	int nbShot;
	
	public Joueur(int x, int y) {
		this.pos = new Position(x, y);
		this.nbShot = 0;
	}
	
	/*
	 * Player shoot target
	 */
	public void shoot() {
		if (this.target != null) {
			int dps = Utils.getDps(this.pos, target.pos);
			target.getDamage(dps);
			this.nbShot++;
		}
	}
}

class Enemy {
	int id;
	Position pos;
	int life;
	Data target;
	double distanceFormTarget;
	static final int MOVE_RANGE = 500; 
	
	public Enemy(int id, int x, int y, int life) {
		super();
		this.id = id;
		this.pos = new Position(x, y);
		this.life = life;
		this.distanceFormTarget = Integer.MAX_VALUE;
		this.target = null;
	}

	public Enemy(int id, int x, int y, int life, Collection<Data> datas) {
		super();
		this.id = id;
		this.pos = new Position(x, y);
		this.life = life;
		this.distanceFormTarget = Integer.MAX_VALUE;
		getTargetAndDistanceFromIt(datas);
	}

	/*
	 * This method find the target (closest data) and setup the distance from it
	 */
	private void getTargetAndDistanceFromIt(Collection<Data> datas) {
		for (Data d : datas) {
			double dist = Utils.getDist(this.pos, d.pos);
			if (dist < this.distanceFormTarget) {
				this.target = d;
				this.distanceFormTarget = dist;
			}
		}
	}
	
	
	public void action() {
		//TODO CHANGE TARGET IF CURRENT ONE IS DEAD
		move();
		this.distanceFormTarget = Utils.getDist(this.pos, target.pos);
	}

	/*
	 * This method move enemy on his current position, target position and move range
	 */
	private void move() {
		Position targetPos = this.target.pos;
		Position curPos = this.pos;
		double nextPosX = 0;
		double nextPosY = 0;
		//if enemy is too close from target, new posisition will be at target
		if (distanceFormTarget <= 500) {
			nextPosX = targetPos.x;
			nextPosY = targetPos.y;
		} else {
			if (targetPos.x == curPos.x) {
				nextPosX = curPos.x;
				nextPosY = curPos.y > targetPos.y ? curPos.y - MOVE_RANGE : curPos.y + MOVE_RANGE;
			} else {
				double posDivision = (double) (targetPos.y - curPos.y) / (double) (targetPos.x - curPos.x);
				double constant = MOVE_RANGE / (Math.sqrt(1 + Math.pow(posDivision, 2)));
				nextPosX = (int) (curPos.x + constant);
				if (Math.abs(curPos.x - targetPos.x) < Math.abs(nextPosX - targetPos.x)) {
					nextPosX = (curPos.x - constant);
				}
				nextPosY = ((nextPosX - curPos.x) * posDivision + curPos.y);
			}
		}
		this.pos.x = (int) nextPosX;
		this.pos.y = (int) nextPosY;
	}
	
	public void getDamage(int dps) {
		this.life -= dps;
	}
}

class Game {
	Joueur player;
	Map<Integer, Data> datas;
	Map<Integer, Enemy> enemies;
	int score;
	Status status;
	int totalEnemyLifePoints;
	int nbTurn;
	
	
	public Game(Joueur player, Map<Integer, Data> datas, Map<Integer, Enemy> enemies) {
		super();
		this.player = player;
		this.datas = datas;
		this.enemies = enemies;
		this.status = Status.ONGOING;
		this.totalEnemyLifePoints = getTotalEnemyLifePoints();
		this.score = 0;
		this.nbTurn = 0;
	}

	private int getTotalEnemyLifePoints() {
		int lifepoints = 0;
		for (Enemy e : enemies.values()) {
			lifepoints += e.life;
		}
		return lifepoints;
	}

	public void simulate() {
		while (this.status.equals(Status.ONGOING)) {
			enemiesAction();
			//TODO move Player
			//TODO Check if player is dead
			player.shoot();
			removeDeadEnemy();
			collectDatas();
			updateStatus();
			nbTurn++;
		}
		computeScore();
	}

	private void enemiesAction() {
		for (Enemy e : this.enemies.values()) {
			e.action();
		}
	}
	
	private void removeDeadEnemy() {
		for (Enemy e : this.enemies.values()) {
			if (e.life <= 0) {
				enemies.remove(e.id);
				score += 10;
			}
		}
	}
	

	private void collectDatas() {
		for (Enemy e : this.enemies.values()) {
			if (e.distanceFormTarget == 0) {
				datas.remove(e.target.id);
			}
		}
	}

	private void updateStatus() {
		if (enemies.isEmpty()) {
			this.status = Status.FINISHED;
		}
		if (datas.isEmpty()) {
			this.status = Status.FAILURE;
		}
	}
	
	private void computeScore() {
		score += datas.size() * 100;
		int bonusPoints = datas.size() * Math.max(0, (totalEnemyLifePoints - 3 * player.nbShot)) * 3;
		score += bonusPoints;
	}

}

class Utils {
	
	/*
	 * This method return the euclidiean distance between two positions
	 */
	public static double getDist(Position a, Position b) {
		double powA = Math.pow(a.x - b.x, 2);
		double powB = Math.pow(a.y - b.y, 2);
		//Optim : Maybe save the cost of sqrt
		return Math.sqrt(powA + powB);
	}
	
	/*
	 * This method return the dps from shooter pos to target pos
	 */
	public static int getDps(Position shooter, Position target) {
		double dist = getDist(shooter, target);
		double dps = (125000 / Math.pow(dist, 1.2));
		
		return (int) Math.round(dps); 
	}
}


enum Status {
	ONGOING,
	FAILURE,
	FINISHED;
}
