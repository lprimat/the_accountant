package com.lprimat.codingame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class Player {
	
	static long deepCopyGlobalTime;
	static long hashCopyTime;

    public static void main(String args[]) {
    	String input = "5000;1000;2;0;950;7000;1;8000;7100;2;0;3100;8000;10;1;14500;8100;10";
    	Scanner in = new Scanner(input).useDelimiter(";");
    	//Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            int x = in.nextInt();
            int y = in.nextInt();
            Joueur player = new Joueur(x, y);
            System.err.println(x + ", " + y);
            Map<Integer, Data> datas = new HashMap<>();
            int dataCount = in.nextInt();
            for (int i = 0; i < dataCount; i++) {
                int dataId = in.nextInt();
                int dataX = in.nextInt();
                int dataY = in.nextInt();
                System.err.println(dataId + ", " + dataX + ", " + dataY);
                datas.put(i, new Data(dataId, dataX, dataY));
            }
            Map<Integer, Enemy> enemies = new HashMap<>();
            int enemyCount = in.nextInt();
            for (int i = 0; i < enemyCount; i++) {
                int enemyId = in.nextInt();
                int enemyX = in.nextInt();
                int enemyY = in.nextInt();
                int enemyLife = in.nextInt();
                System.err.println(enemyId + ", " + enemyX + ", " + enemyY + ", " + enemyLife);
                enemies.put(i, new Enemy(enemyId, enemyX, enemyY, enemyLife, datas.values()));
            }
            
            player.target = enemies.get(0);
	    	List<Action> actions = new ArrayList<>();
		    actions.add(new MoveToData());
		    actions.add(new ShootClosestEnemyFromData());
		    
		    long start = System.currentTimeMillis();
		    Game game = new Game(player, datas, enemies, actions, 0);
		    Action action = game.getFirstAction();
		    long end = System.currentTimeMillis();
		    long elasped = end - start;
		    System.err.println("DeepCopy time : " + deepCopyGlobalTime);
		    System.err.println("HashCopy time : " + hashCopyTime);
		    System.err.println("Time : " + elasped);
		    System.out.println(action.toString());
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

//DATA CAN BE IMMUTABLE
class Data {
	final int id;
	final Position pos;
	
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
	static final int MOVE_RANGE = 1000; 
	
	public Joueur(int x, int y) {
		this.pos = new Position(x, y);
		this.nbShot = 0;
	}
	
	public Joueur(int x, int y, int nbShot, Enemy target) {
		this.pos = new Position(x, y);
		this.nbShot = nbShot;
		this.target = target;
	}
	
	public Joueur clone() {
		return new Joueur(this.pos.x, this.pos.y, this.nbShot, this.target);
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

	//TODO REFACTOR !!!
	public void move(Position destination) {
		Position curPos = this.pos;
		double nextPosX = 0;
		double nextPosY = 0;
		//if enemy is too close from target, new position will be at target
		if (Utils.getDist(curPos, destination) <= MOVE_RANGE) {
			nextPosX = destination.x;
			nextPosY = destination.y;
		} else {
			if (destination.x == curPos.x) {
				nextPosX = curPos.x;
				nextPosY = curPos.y > destination.y ? curPos.y - MOVE_RANGE : curPos.y + MOVE_RANGE;
			} else {
				double posDivision = (double) (destination.y - curPos.y) / (double) (destination.x - curPos.x);
				double constant = MOVE_RANGE / (Math.sqrt(1 + Math.pow(posDivision, 2)));
				nextPosX = (int) (curPos.x + constant);
				if (Math.abs(curPos.x - destination.x) < Math.abs(nextPosX - destination.x)) {
					nextPosX = (curPos.x - constant);
				}
				nextPosY = ((nextPosX - curPos.x) * posDivision + curPos.y);
			}
		}
		this.pos.x = (int) nextPosX;
		this.pos.y = (int) nextPosY;
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
	
	public Enemy(int id, int x, int y, int life, Data data, double distanceFormTarget) {
		this.id = id;
		this.pos = new Position(x, y);
		this.life = life;
		this.distanceFormTarget = distanceFormTarget;
		this.target = data;
	}

	public Enemy clone() {
		return new Enemy(this.id, this.pos.x, this.pos.y, this.life, this.target, this.distanceFormTarget);
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
				//System.err.println("Target for e : " + this.id + " is : " + this.target.id);
			}
		}
	}
	
	
	public void action(Map<Integer, Data> datas) {
		//TODO CHANGE TARGET IF CURRENT ONE IS DEAD
		if (datas != null) {
			getTargetStatus(datas);
		}
		move();
		this.distanceFormTarget = Utils.getDist(this.pos, target.pos);
	}

	private void getTargetStatus(Map<Integer, Data> datas) {
		if (datas.get(target.id) == null) {
			this.target = null;
			this.distanceFormTarget = Integer.MAX_VALUE;
			getTargetAndDistanceFromIt(datas.values());
			//System.err.println("New target for e : " + this.id + " is : " + this.target.id);
		}
	}

	/*
	 * This method move enemy on his current position, target position and move range
	 */
	private void move() {
		Position targetPos = this.target.pos;
		Position curPos = this.pos;
		double nextPosX = 0;
		double nextPosY = 0;
		//if enemy is too close from target, new position will be at target
		if (distanceFormTarget <= MOVE_RANGE) {
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
	List<Action> actions;
	
	
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
	
	public Game(Joueur player, Map<Integer, Data> datas, Map<Integer, Enemy> enemies, List<Action> actions, int nbTurn) {
		super();
		this.player = player;
		this.datas = datas;
		this.enemies = enemies;
		this.status = Status.ONGOING;
		this.totalEnemyLifePoints = getTotalEnemyLifePoints();
		this.score = 0;
		this.nbTurn = nbTurn;
		this.actions = actions;
	}
	
	public Game clone() {
		Map<Integer, Enemy> enemiesClone = Utils.deepCopyEnemy(this.enemies);
		//long start = System.currentTimeMillis();
		//Map<Integer, Data> dataClone = new HashMap<>(this.datas);
		//long end = System.currentTimeMillis();
		//long elasped = end - start;
		//Player.hashCopyTime += elasped;
		Map<Integer, Data> dataClone = Utils.deepCopyData(this.datas);
		return new Game(player.clone(), dataClone, enemiesClone, this.actions, this.nbTurn);
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
	
	public int simulateAction(Action action) {
		int scoreMax = 0;
		enemiesAction();
		doPlayerAction(action);
		removeDeadEnemy();
		collectDatas();
		updateStatus();
		nbTurn++;
		
		if (status.equals(Status.ONGOING)) {
			for (Action a : actions) {
				Game game = this.clone();
				int sonScore = game.simulateAction(a);
				if (sonScore > scoreMax) {
					score = sonScore;
					scoreMax = sonScore;
				}
			}
		} else {
			computeScore();
		}
		//System.err.println("NBTURN : " + nbTurn);
		return score;
	}

	public Action getFirstAction() {
		Action firstAct = null;
		int scoreMax = 0;
		for (Action action : actions) {
			Game game = this.clone();
			int score = game.simulateAction(action);
			if (score > scoreMax) {
				scoreMax = score;
				firstAct = action;
			}
		}
		score = scoreMax;
		return firstAct;
	}

	private void enemiesAction() {
		for (Enemy e : this.enemies.values()) {
			e.action(this.datas);
		}
	}
	
	private void doPlayerAction(Action action) {
		if (action instanceof Shoot) {
			updatePlayerStatus();
			player.target = ((Shoot) action).getTarget(this.datas, this.enemies);
			player.shoot();
		} else if (action instanceof Move) {
			Move m = (Move) action;
			player.move(m.getDestination(this.datas, this.enemies));
			updatePlayerStatus();
		}
	}

	
	private void updatePlayerStatus() {
		for (Enemy e : this.enemies.values()) {
			double distFromPlayer = Utils.getDist(player.pos, e.pos);
			if (distFromPlayer <= 2000) {
				player.pos = new Position(-1, -1);
				break;
			}
		}
	}

	private void removeDeadEnemy() {
		for (Iterator<Map.Entry<Integer, Enemy>> iterator = enemies.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<Integer, Enemy> e = iterator.next();
			if (e.getValue().life <= 0) {
				//System.err.println("Dead enemy : " + e.getValue().id);
				iterator.remove();
				//TODO TRY TO COUNT THE NUMBER OF DEAD ENEMY
				score += 10;
			}
		}
	}
	

	private void collectDatas() {
		for (Enemy e : this.enemies.values()) {
			if (e.distanceFormTarget == 0) {
				//System.err.println("Dead data : " + e.target.id);
				datas.remove(e.target.id);
			}
		}
	}

	private void updateStatus() {
		if  (player.pos.x == -1 && player.pos.y == -1) {
			this.status = Status.DEAD;
		}
		if (enemies.isEmpty() || datas.isEmpty()) {
			this.status = Status.FAILURE;
		}
	}
	
	private void computeScore() {
		if (this.status.equals(Status.DEAD)) {
			score = 0;
		} else {
			score += datas.size() * 100;
			int bonusPoints = datas.size() * Math.max(0, (totalEnemyLifePoints - 3 * player.nbShot)) * 3;
			score += bonusPoints;
		}
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
	
	public static Map<Integer, Enemy> deepCopyEnemy(Map<Integer, Enemy> map) {
		long start = System.currentTimeMillis();
		Map<Integer, Enemy> copy = new HashMap<>();
		for (Map.Entry<Integer, Enemy> entry : map.entrySet()) {
			copy.put(entry.getKey(), entry.getValue().clone());
		}
		long end = System.currentTimeMillis();
		long elasped = end - start;
		Player.deepCopyGlobalTime += elasped;
		//System.err.println("deepCopyEnemy time : " + elasped);
		return copy;
	}
	
	public static Map<Integer, Data> deepCopyData(Map<Integer, Data> map) {
		long start = System.currentTimeMillis();
		Map<Integer, Data> copy = new HashMap<>();
		for (Map.Entry<Integer, Data> entry : map.entrySet()) {
			copy.put(entry.getKey(), entry.getValue());
		}
		long end = System.currentTimeMillis();
		long elasped = end - start;
		Player.hashCopyTime += elasped;
		//System.err.println("deepCopyEnemy time : " + elasped);
		return copy;
	}
}


enum Status {
	ONGOING,
	DEAD,
	FAILURE,
	FINISHED;
}

class Action {}

class Move extends Action {
	Position destination;
	
	public Move() {
		super();
	}

	public Position getDestination(Map<Integer, Data> datas, Map<Integer, Enemy> enemies) {
		return null;
	}

	@Override
	public String toString() {
		return "MOVE " + destination.x + " " + destination.y;
	}
}

class MoveToData extends Move {

	public MoveToData() {
		super();
	}
	
	@Override
	public Position getDestination(Map<Integer, Data> datas, Map<Integer, Enemy> enemies) {
		for (Data d : datas.values()) {
			destination = d.pos;
			return d.pos;
		}
		return null;
	}	
}

class Shoot extends Action {
	int targetId;
	
	public Shoot() {
		super();
	}
	
	public Enemy getTarget(Map<Integer, Data> datas, Map<Integer, Enemy> enemies){
		return null;
	};
	
	@Override
	public String toString() {
		return "SHOOT " + targetId;
	}
}

class ShootClosestEnemyFromData extends Shoot{

	public ShootClosestEnemyFromData() {
		super();
	}
	
	@Override
	public Enemy getTarget(Map<Integer, Data> datas, Map<Integer, Enemy> enemies) {
		double minDist = Integer.MAX_VALUE;
		Enemy target = null;
		for(Enemy e : enemies.values()) {
			if (e.distanceFormTarget < minDist) {
				minDist = e.distanceFormTarget;
				target = e;
			}
		}
		targetId = target != null ? target.id : 0;
		return target;
	}
}