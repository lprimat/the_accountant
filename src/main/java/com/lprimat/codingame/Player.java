package com.lprimat.codingame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class Player {
	
	static long deepCopyGlobalTime;
	static long hashCopyTime;
	static LinkedList<Action> actions = new LinkedList<>();
	public static long listCopyTime;
	public static long beginTime;
	public static long GetSafePosTime;
	public static CosSinTable cosSinTable;

    public static void main(String args[]) {
    	//String input = "5000;1000;2;0;950;7000;1;8000;7100;2;0;3100;8000;10;1;14500;8100;10";
    	//Scanner in = new Scanner(input).useDelimiter(";");
    	Scanner in = new Scanner(System.in);
    	cosSinTable = new CosSinTable();

        // game loop
    	int gameTurn = 0;
    	boolean firstTurn = true;
    	int timeoutMax = 1000;
    	Game game = null;
    	while (true) {
    		//Init Game
    		int x = in.nextInt();
    		long startTime = System.currentTimeMillis();
    		int y = in.nextInt();
    		Joueur player = new Joueur(x, y);
    		//System.err.println(x + ", " + y);
    		Map<Integer, Data> datas = new HashMap<>();
    		int dataCount = in.nextInt();
    		for (int i = 0; i < dataCount; i++) {
    		    int dataId = in.nextInt();
    		    int dataX = in.nextInt();
    		    int dataY = in.nextInt();
    		    datas.put(i, new Data(dataId, dataX, dataY));
    		    //System.err.println("datas.put(" + dataId + ", new Data(" + dataId + ", " + dataX + ", " + dataY +"));");
    		}
    		Map<Integer, Enemy> enemies = new HashMap<>();
    		int enemyCount = in.nextInt();
    		for (int i = 0; i < enemyCount; i++) {
    		    int enemyId = in.nextInt();
    		    int enemyX = in.nextInt();
    		    int enemyY = in.nextInt();
    		    int enemyLife = in.nextInt();
    		    //System.err.println("enemies.put(" + enemyId + ", new Enemy(" + 
    		    //enemyId + ", " + enemyX + ", " + enemyY + ", " + enemyLife +", datas.values());");
    		    enemies.put(i, new Enemy(enemyId, enemyX, enemyY, enemyLife, datas.values()));
    		}
    		
    		//TODO Save previous shot and death to be able to simulumate a correct score
    		//Idea just save the number of shot, total enemy life and number of dead enemies
    		if (firstTurn) {
    			game = generateGame(startTime, timeoutMax, player, datas, enemies);
    			firstTurn = false;
    		} 
    		
    		System.out.println(game.bestActions.get(gameTurn).toString());
        	gameTurn++;
        }
    }

	private static Game generateGame(long startTime, int timeoutMax, Joueur player, Map<Integer, Data> datas, Map<Integer, Enemy> enemies) {
		List<Action> actions = new ArrayList<>();
		actions.add(new MoveToDataInDanger());
		actions.add(new ShootClosestEnemyFromData());
		Game game = simulateActions(player, datas, enemies, actions, 150);
		
		actions = new ArrayList<>();
		actions.add(new ShootClosestEnemyFromData());
		actions.add(new MoveToDataInDanger());
		Game game2 = simulateActions(player, datas, enemies, actions, 150);
		
		if (game.score < game2.score) {
			game = game2;
		}
		
		actions = new ArrayList<>();
		actions.add(new ShootLowestEnemy());
		actions.add(new MoveToDataInDanger());
		Game game3 = simulateActions(player, datas, enemies, actions, 150);
		long elasped = System.currentTimeMillis() - startTime;
		
		if (game.score < game3.score) {
			game = game3;
		}
		
		actions = new ArrayList<>();
		actions.add(new ShootClosestEnemyFromData());
		actions.add(new MoveToSafestPosition()); 
		actions.add(new MoveToDataInDanger());
		Game game4 = simulateActions(player, datas, enemies, actions, timeoutMax - elasped - enemies.size() - datas.size());
		
		if (game.score < game4.score) {
			game = game4;
		}
		
		System.err.println("DeepCopy time : " + deepCopyGlobalTime);
		System.err.println("HashCopy time : " + hashCopyTime);
		return game;
	}

	private static Game simulateActions(Joueur player, Map<Integer, Data> datas, Map<Integer, Enemy> enemies, List<Action> actions, long timeout) {
		long start = System.currentTimeMillis();
		Game game = new Game(player, datas, enemies, actions, timeout);
		game.getListOfActions();
		int estimatedScore = game.score;
		long end = System.currentTimeMillis();
		long elasped = end - start;
		System.err.println("Time for simu : " + elasped);
		System.err.println("Estimated Score : " + estimatedScore);
		System.err.println("Number of simulated turn :" + (Game.nbTurn));
		Game.nbTurn = 0;
		return game;
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
	static final int SAFE_RANGE = 2000; 
	
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
	int nbTotalEnemies;
	List<Action> actions;
	LinkedList<Action> bestActions;
	Action playedAction;
	
	static final int MAX_WIDTH = 16000;
	static final int MAX_HEIGHT = 9000;
	private static long MAX_TIMEOUT = 950;
	static int nbTurn = 0;
	
	private void initCommonGameAttributes(Joueur player, Map<Integer, Data> datas, Map<Integer, Enemy> enemies) {
		this.player = player;
		this.datas = datas;
		this.enemies = enemies;
		this.status = Status.ONGOING;
		this.score = 0;
	}

	private void initVariablesGameAttributes(Map<Integer, Enemy> enemies) {
		this.nbTotalEnemies = enemies.size();
		this.totalEnemyLifePoints = getTotalEnemyLifePoints();
	}
	
	public Game(Joueur player, Map<Integer, Data> datas, Map<Integer, Enemy> enemies) {
		super();
		Player.beginTime = System.currentTimeMillis();
		initCommonGameAttributes(player, datas, enemies);
		initVariablesGameAttributes(enemies);
	}
	
	public Game(Joueur player, Map<Integer, Data> datas, Map<Integer, Enemy> enemies, List<Action> actions) {
		super();
		Player.beginTime = System.currentTimeMillis();
		initCommonGameAttributes(player, datas, enemies);
		initVariablesGameAttributes(enemies);
		this.actions = actions;
	}
	
	
	public Game(int nbTotalEnemies, int totalEnemyLifePoints, Joueur player, Map<Integer, Data> datas, Map<Integer, Enemy> enemies, List<Action> actions) {
		super();
		initCommonGameAttributes(player, datas, enemies);
		this.totalEnemyLifePoints = totalEnemyLifePoints;
		this.nbTotalEnemies = nbTotalEnemies;
		this.actions = actions;
	}
	
	public Game(Joueur player, Map<Integer, Data> datas, Map<Integer, Enemy> enemies, List<Action> actions, long remainingTime) {
		super();
		Player.beginTime = System.currentTimeMillis();
		MAX_TIMEOUT = remainingTime;
		initCommonGameAttributes(player, datas, enemies);
		initVariablesGameAttributes(enemies);
		this.actions = actions;
	}

	public Game clone() {
		Map<Integer, Enemy> enemiesClone = Utils.deepCopyEnemy(this.enemies);
		Map<Integer, Data> dataClone = Utils.deepCopyData(this.datas);
		return new Game(this.nbTotalEnemies, this.totalEnemyLifePoints, player.clone(), dataClone, enemiesClone, this.actions);
	}

	private int getTotalEnemyLifePoints() {
		int lifepoints = 0;
		for (Enemy e : enemies.values()) {
			lifepoints += e.life;
		}
		return lifepoints;
	}

	public void simulateOneAction(Action action) {
		enemiesAction();
		doPlayerAction(action);
		this.playedAction = action;
		collectDatas();
		updateStatus();
		Game.nbTurn++;
		
		if (!status.equals(Status.ONGOING)) {
			computeScore();
		}
	}
	
	public int simulateAction(Action action) {
		Action bestAction = null;
		LinkedList<Action> bestSonActions = null;
		int scoreMax = -1;
		enemiesAction();
		doPlayerAction(action);
		this.playedAction = action;
		collectDatas();
		updateStatus();
		Game.nbTurn++;
		
		if (System.currentTimeMillis() - Player.beginTime > Game.MAX_TIMEOUT) {
			this.bestActions = new LinkedList<>();
			return (nbTotalEnemies - enemies.size()) * 10;
		}
		if (status.equals(Status.ONGOING)) {
			for (Action a : actions) {
				Game game = this.clone();
				int sonScore = game.simulateAction(a.clone());
				
				if (sonScore > scoreMax) {
					score = sonScore;
					scoreMax = sonScore;
					bestAction = game.playedAction;
					bestSonActions = game.bestActions;
				}
			}
			this.bestActions = bestSonActions;
			this.bestActions.addFirst(bestAction);
		} else {
			computeScore();
			this.bestActions = new LinkedList<>();
		}
		return score;
	}

	public LinkedList<Action> getListOfActions() {
		Action firstAct = null;
		LinkedList<Action> bestSonActions = null;
		int scoreMax = -1;
		for (Action action : actions) {
			Game game = this.clone();
			int score = game.simulateAction(action.clone());
			if (score > scoreMax) {
				scoreMax = score;
				firstAct = game.playedAction;
				bestSonActions = game.bestActions;
			}
		}
		this.bestActions = bestSonActions;
		this.bestActions.addFirst(firstAct);
		score = scoreMax;
		return this.bestActions;
	}

	private void enemiesAction() {
		for (Enemy e : this.enemies.values()) {
			e.action(this.datas);
		}
	}
	
	private void doPlayerAction(Action action) {
		if (action instanceof Shoot) {
			updatePlayerStatus();
			if (player.pos.x != -1 && player.pos.y != -1) {  
				player.target = ((Shoot) action).getTarget(this);
				player.shoot();
				removeDeadEnemy(action);
			}
		} else if (action instanceof Move) {
			Move m = (Move) action;
			player.move(m.getDestination(this));
			updatePlayerStatus();
		}
	}

	
	private void updatePlayerStatus() {
		for (Enemy e : this.enemies.values()) {
			double distFromPlayer = Utils.getDist(player.pos, e.pos);
			if (distFromPlayer <= Joueur.SAFE_RANGE) {
				player.pos = new Position(-1, -1);
				break;
			}
		}
	}

	private void removeDeadEnemy(Action action) {
		Enemy e = enemies.get(((Shoot) action).targetId);
		if (e.life <= 0) {
			enemies.remove(e.id);
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
			return;
		}
		if (datas.isEmpty()) {
			this.status = Status.FAILURE;
			return;
		}
		if (enemies.isEmpty()) {
			this.status = Status.FINISHED;
			return;
		}
	}
	
	private void computeScore() {
		if (this.status.equals(Status.DEAD)) {
			score = 0;
		} else {
			score += (nbTotalEnemies - enemies.size()) * 10;
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
	
	public static LinkedList<Action> deepCopyActions(LinkedList<Action> list) {
		long start = System.currentTimeMillis();
		LinkedList<Action> copy = new LinkedList<>();
		for (Action a : list) {
			copy.add(a);
		}
		long end = System.currentTimeMillis();
		long elasped = end - start;
		Player.listCopyTime += elasped;
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

class Action {
	
	public Action clone() {
		return null;
	}
}

abstract class Move extends Action {
	Position destination;
	
	public Move() {
		super();
		this.destination = null;
	}

	public Move(Position dest) {
		this.destination = new Position(dest.x, dest.y);
	}

	public abstract Position getDestination(Game game);

	@Override
	public String toString() {
		return "MOVE " + destination.x + " " + destination.y;
	}
}

class MoveToFixedPos extends Move {
	
	public MoveToFixedPos() {
		super();
	}
	
	public MoveToFixedPos(Position dest) {
		this.destination = new Position(1100, 1200);
	}
	
	@Override
	public Position getDestination(Game game) {
		return new Position(5000, 2500);
	}
	
	@Override
	public MoveToFixedPos clone() {
		return new MoveToFixedPos(destination);
	}
}

class MoveToData extends Move {

	public MoveToData() {
		super();
	}
	
	public MoveToData(Position dest) {
		this.destination = new Position(dest.x, dest.y);
	}
	
	@Override
	public Position getDestination(Game game) {
		for (Data d : game.datas.values()) {
			this.destination = d.pos;
			return d.pos;
		}
		return destination;	
	}
	
	@Override
	public MoveToData clone() {
		return new MoveToData();
	}
}

class MoveToSafestPosition extends Move {
	
	public MoveToSafestPosition() {
		super();
	}
	
	@Override
	public MoveToSafestPosition clone() {
		return new MoveToSafestPosition();
	}

	@Override
	public Position getDestination(Game game) {
		List<Position> positionsFromRange = getAllPositionFromRange(game.player.pos, Joueur.MOVE_RANGE);
		Collection<Enemy> enemis = game.enemies.values();
		Position move = null;
		List<Enemy> dangerousEnemies = getDangerousEnemies(game.player.pos, enemis);
		for (Position position : positionsFromRange) {
			long start = System.currentTimeMillis();
			if (isSafePositionFromEnemis(position, dangerousEnemies)) {
					move = position;
				}
			long end = System.currentTimeMillis();
			long elasped = end - start;
			Player.GetSafePosTime += elasped;
		}
		if (move == null) {
			move = game.player.pos;
		}
		this.destination = move;
		return move;
	}
	
	private List<Enemy> getDangerousEnemies(Position playerPos, Collection<Enemy> enemis) {
		List<Enemy> dangerousEnemies = new ArrayList<>();
		for (Enemy e : enemis) {
			if ((Math.abs(playerPos.x - e.pos.x) <= Joueur.SAFE_RANGE + Joueur.MOVE_RANGE) && 
				(Math.abs(playerPos.y - e.pos.y) <= Joueur.SAFE_RANGE + Joueur.MOVE_RANGE)) {
				dangerousEnemies.add(e);
			}
		}	
		return dangerousEnemies;
	}

	private static Boolean isSafePositionFromEnemis(Position myPos, Collection<Enemy> enemis) {
		for (Enemy e : enemis) {
			double dist = Utils.getDist(myPos, e.pos);
			if (dist <= Joueur.SAFE_RANGE) {
				return false;
			}
		}
		return true;
	}
	
	private List<Position> getAllPositionFromRange(Position myPos, int moveRange) {
			List<Position> posFromRange = new ArrayList<>();
			for (int i = 500; i <= moveRange; i += 500) {
				for (int angle = 0; angle < 360; angle+= 36) {
					int x = getXFromPolarCoordinate(myPos, i, angle);
					int y = getYFromPolarCoordinate(myPos, i, angle);
					if (x >= 0 && x < Game.MAX_WIDTH && y >= 0 && y <= Game.MAX_HEIGHT) {
					    posFromRange.add(new Position(x, y));
					}
				}
			}
			
			return posFromRange;
		}
	
	//TODO Add CosSinTable
	private static int getXFromPolarCoordinate(Position o, int moveRange, int angle) {
		double cos = Player.cosSinTable.getCos(angle);
		return o.x + (int) (moveRange * cos);
	}
	
	private static int getYFromPolarCoordinate(Position o, int moveRange, int angle) {
		double sin = Player.cosSinTable.getSin(angle);
		return o.y + (int) (moveRange * sin);
	}
}

class MoveToDataInDanger extends Move {
	
	public MoveToDataInDanger() {
		super();
	}

	@Override
	public Position getDestination(Game game) {
		double minDist = Integer.MAX_VALUE;
		Position dest = null;
		for(Enemy e : game.enemies.values()) {
			if (e.distanceFormTarget < minDist) {
				minDist = e.distanceFormTarget;
				dest = e.target.pos;
			}
		}
		this.destination = dest;
		return dest;
	}
	
	@Override
	public MoveToDataInDanger clone() {
		return new MoveToDataInDanger();
	}
	
}

class MoveToEnemyClosestFromData extends Move {
	
	public MoveToEnemyClosestFromData() {
		super();
	}

	@Override
	public Position getDestination(Game game) {
		double minDist = Integer.MAX_VALUE;
		Position dest = null;
		for(Enemy e : game.enemies.values()) {
			if (e.distanceFormTarget < minDist) {
				minDist = e.distanceFormTarget;
				dest = e.pos;
			}
		}
		this.destination = dest;
		return dest;
	}
	
	@Override
	public MoveToEnemyClosestFromData clone() {
		return new MoveToEnemyClosestFromData();
	}
	
}

class Shoot extends Action {
	int targetId;
	
	public Shoot() {
		super();
		this.targetId = -1;
	}
	
	public Shoot(int targetId) {
		this.targetId = targetId;
	}

	public Enemy getTarget(Game game) {
		return null;
	};
	
	@Override
	public String toString() {
		return "SHOOT " + targetId;
	}
	
	@Override
	public Shoot clone() {
		return new Shoot();
	}
}

class ShootClosestEnemyFromData extends Shoot{

	public ShootClosestEnemyFromData() {
		super();
	}
	
	public ShootClosestEnemyFromData(int targetId) {
		this.targetId = targetId;
	}
	
	@Override
	public Enemy getTarget(Game game) {
		double minDist = Integer.MAX_VALUE;
		Enemy target = null;
		for(Enemy e : game.enemies.values()) {
			if (e.distanceFormTarget < minDist) {
				minDist = e.distanceFormTarget;
				target = e;
			}
		}
		this.targetId = target.id;
		return target;
	}
	
	@Override
	public ShootClosestEnemyFromData clone() {
		return new ShootClosestEnemyFromData(targetId);
	}
}

class ShootLowestEnemy extends Shoot{

	public ShootLowestEnemy() {
		super();
	}
	
	public ShootLowestEnemy(int targetId) {
		this.targetId = targetId;
	}
	
	@Override
	public Enemy getTarget(Game game) {
		Joueur player = game.player;
		double minLife = Integer.MAX_VALUE;
		Enemy target = null;
		for(Enemy e : game.enemies.values()) {
			int dps = Utils.getDps(player.pos, e.pos);
			if (e.life - dps <= 0) {
				target = e;
				break;
			}
			if (e.life - dps < minLife) {
				minLife = e.life - dps;
				target = e;
			}
		}
		this.targetId = target.id;
		return target;
	}
	
	@Override
	public ShootLowestEnemy clone() {
		return new ShootLowestEnemy(targetId);
	}
}

class CosSinTable {
	double[] cos = new double[361];
	double[] sin = new double[361];
	
	public CosSinTable() {
		for (int i = 0; i <= 360; i++) {
			cos[i] = Math.cos(Math.toRadians(i));
			sin[i] = Math.sin(Math.toRadians(i));
		}
	}

	public double getSin(int angle) {
		int angleCircle = angle % 360;
		return sin[angleCircle];
	}

	public double getCos(int angle) {
		int angleCircle = angle % 360;
		return cos[angleCircle];
	}
}
