package com.lprimat.codingame;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class PlayerTest {
	
	List<Data> datas; 
	
	@Before
	public void init() {
		datas = new ArrayList<>();
		datas.add(new Data(0,950,7000));
		datas.add(new Data(1,8000,7100));		
	}
	
	@Test
	public void verify_basic_next_enemy_pos() {
		Enemy enemy = new Enemy(0,8250,8499,10);
		enemy.target = new Data(0,8250,4500);;
		enemy.action(null);
		assertEquals(8250, enemy.pos.x);
		assertEquals(7999, enemy.pos.y);
	}
	
	@Test
	public void verify_complex_next_enemy_pos() {
		Enemy enemy = new Enemy(1,14005,8023,10);
		enemy.target = new Data(1,8000,7100);
		enemy.action(null);
		assertEquals(13510, enemy.pos.x);
		assertEquals(7947, enemy.pos.y);
	}
	
	@Test
	public void verify_complex_bis_next_enemy_pos() {
		Enemy enemy = new Enemy(0,2646,7789,10);
		enemy.target = new Data(0,950,7000);
		enemy.action(null);
		assertEquals(2192, enemy.pos.x);
		assertEquals(7578, enemy.pos.y);
	}
	
	@Test
	public void verify_enemy_target() {
		Enemy en0 = new Enemy(0,2646,7789,10, datas);
		Enemy en1 = new Enemy(1,14005,8023,10, datas);
		
		assertEquals(0, en0.target.id);
		assertEquals(1, en1.target.id);
	}
	
	@Test
	public void verify_life_lost_when_shoot() {
		Enemy en = new Enemy(1,14005,8023,10);
		Joueur player = new Joueur(5000,1000);
		player.target = en;
		player.shoot();
		assertEquals(8, en.life);
	}
	
	@Test
	public void verify_life_lost_when_shoot_bis() {
		Enemy en = new Enemy(0,3100,8000,10);
		Joueur player = new Joueur(5000,1000);
		player.target = en;
		player.shoot();
		assertEquals(7, en.life);
	}
	
	@Test
	public void should_have_a_score_of_110_and_do_5_action_when_only_shoot() {
		Joueur player = new Joueur(1100, 1200);
		Map<Integer, Data> datas = new HashMap<>();
		datas.put(0, new Data(0,8250,4500));
		Map<Integer, Enemy> enemies = new HashMap<>();
		enemies.put(0, new Enemy(0,8250,8999,10, datas.values()));
		player.target = enemies.get(0);
		
		List<Action> actions = new ArrayList<>();
		actions.add(new ShootClosestEnemyFromData());
		Game game = new Game(player, datas, enemies, actions);
		game.getListOfActions();
		int finalScore = 110;
		int nbActions = 5;
		
		assertEquals(nbActions, game.bestActions.size());
		assertEquals(finalScore, game.score);
	}
	
	@Test
	public void should_lost_game_when_player_do_nothing() {
		Joueur player = new Joueur(1100, 1200);
		Map<Integer, Data> datas = new HashMap<>();
		datas.put(0, new Data(0,8250,4500));
		Map<Integer, Enemy> enemies = new HashMap<>();
		enemies.put(0, new Enemy(0,8250,8999,10, datas.values()));
		
		List<Action> actions = new ArrayList<>();
		actions.add(new MoveToFixedPos());
		Game game = new Game(player, datas, enemies, actions);
		LinkedList<Action> actionsList =  game.getListOfActions();
		int nbActions = 9;
		int finalScore = 0;
		
		assertEquals(nbActions, actionsList.size());
		assertEquals(finalScore, game.score);
	}
	
	@Test
	public void should_switch_target_when_current_one_die() {
		Joueur player = new Joueur(3320, 3485);
		Map<Integer, Data> datas = new HashMap<>();
		datas.put(0, new Data(0, 950, 7000));
		datas.put(1, new Data(1, 8000, 7100));
		Map<Integer, Enemy> enemies = new HashMap<>();
		enemies.put(0, new Enemy(0, 1284, 7155, 4, datas.values()));
		enemies.put(1, new Enemy(1, 12520, 7795, 10, datas.values()));
		
		Action shoot = new ShootClosestEnemyFromData();
		Game game = new Game(player, datas, enemies);
		
		game.simulateOneAction(shoot);
		assertEquals(1, game.enemies.size());
		game.simulateOneAction(shoot);
		assertEquals(1, game.player.target.id);
	}
	
	@Test
	public void simulate_simple_test_cases_with_simple_actions() {
		Joueur player = new Joueur(1100, 1200);
		Map<Integer, Data> datas = new HashMap<>();
		datas.put(0, new Data(0,8250,4500));
		Map<Integer, Enemy> enemies = new HashMap<>();
		enemies.put(0, new Enemy(0,8250,8999,10, datas.values()));
		List<Action> actions = new ArrayList<>();
		actions.add(new MoveToData());
		actions.add(new ShootClosestEnemyFromData());
		
		Game game = new Game(player, datas, enemies, actions);
		LinkedList<Action> actionsList = game.getListOfActions();
		int bestScore = 131;
		assertEquals(bestScore, game.score);
	}
	
	@Test
	public void simulate_2_enemies_test_cases_with_simple_actions() {
		Joueur player = new Joueur(5000, 1000);
		Map<Integer, Data> datas = new HashMap<>();
		datas.put(0, new Data(0, 950, 7000));
		datas.put(1, new Data(1, 8000, 7100));
		Map<Integer, Enemy> enemies = new HashMap<>();
		enemies.put(0, new Enemy(0, 3100, 8000, 10, datas.values()));
		enemies.put(1, new Enemy(1, 14500, 8100, 10, datas.values()));
		List<Action> actions = new ArrayList<>();
		actions.add(new MoveToSafestPosition());
		actions.add(new MoveToDataInDanger());
		actions.add(new ShootClosestEnemyFromData());
		
		long start = System.currentTimeMillis();
		Game game = new Game(player, datas, enemies, actions);
		LinkedList<Action> actionsList = game.getListOfActions();
		long end = System.currentTimeMillis();
	    long elasped = end - start;
	    System.err.println("Time : " + elasped);
	    
		System.out.println(game.score);
	}
	
	@Test
	public void simulate_2_enemies_test_cases_with_simple_actions_bis() {
		Joueur player = new Joueur(6323, 3691);
		Map<Integer, Data> datas = new HashMap<>();
		datas.put(0, new Data(0, 950, 7000));
		datas.put(1, new Data(1, 8000, 7100));
		Map<Integer, Enemy> enemies = new HashMap<>();
		enemies.put(0, new Enemy(0, 1738, 7367, 10, datas.values()));
		enemies.put(1, new Enemy(1, 13015, 7871, 10, datas.values()));
		List<Action> actions = new ArrayList<>();
		actions.add(new MoveToData());
		actions.add(new ShootClosestEnemyFromData());
		long start = System.currentTimeMillis();
		Game game = new Game(player, datas, enemies, actions);
		long end = System.currentTimeMillis();
	    long elasped = end - start;
	    System.err.println("Time : " + elasped);
	    
		System.out.println(game.score);
	}
	
	@Test
	public void should_have_a_score_of_540_and_22_actions_when_test_case_is_rows() {
		Joueur player = new Joueur(0, 5000);
		Map<Integer, Data> datas = new HashMap<>();
		datas.put(0, new Data(0, 3500, 1500));
		datas.put(1, new Data(1, 2000, 8000));
		
		Map<Integer, Enemy> enemies = new HashMap<>();
		enemies.put(0, new Enemy(0, 5000, 1000, 10, datas.values()));
		enemies.put(1, new Enemy(1, 5000, 8000, 10, datas.values()));
		enemies.put(2, new Enemy(2, 7000, 1000, 10, datas.values()));
		enemies.put(3, new Enemy(3, 7000, 8000, 10, datas.values()));
		enemies.put(4, new Enemy(4, 9000, 2000, 10, datas.values()));
		enemies.put(5, new Enemy(5, 9000, 7000, 10, datas.values()));
		enemies.put(6, new Enemy(6, 11000, 3000, 10, datas.values()));
		enemies.put(7, new Enemy(7, 11000, 6000, 10, datas.values()));
		enemies.put(8, new Enemy(8, 13000, 4000, 10, datas.values()));
		enemies.put(9, new Enemy(9, 13000, 5000, 10, datas.values()));
		
		List<Action> actions = new ArrayList<>();
		
		actions.add(new MoveToDataInDanger());
		actions.add(new ShootClosestEnemyFromData());
		actions.add(new MoveToSafestPosition());
		
		long start = System.currentTimeMillis();
		Game game = new Game(player, datas, enemies, actions);
		LinkedList<Action> actionsList = game.getListOfActions();
		long end = System.currentTimeMillis();
	    long elasped = end - start;
	    System.err.println("Time : " + elasped);
	    
		//int nbActions = 21; old
	    int nbActions = 22;
		int finalScore = 540;
		
		assertEquals(nbActions, actionsList.size());
		assertEquals(finalScore, game.score);
	}
	
	@Test
	public void should_have_a_score_of_when_test_case_is_row_redux() {
		Joueur player = new Joueur(0, 5000);
		Map<Integer, Data> datas = new HashMap<>();
		datas.put(0, new Data(0, 2800, 1300));
		datas.put(1, new Data(1, 4500, 5000));
		datas.put(2, new Data(2, 2000, 7000));
		
		Map<Integer, Enemy> enemies = new HashMap<>();
		enemies.put(0, new Enemy(0, 5000, 1000, 10, datas.values()));
		enemies.put(1, new Enemy(1, 5000, 8999, 10, datas.values()));
		enemies.put(2, new Enemy(2, 7000, 1000, 10, datas.values()));
		enemies.put(3, new Enemy(3, 8000, 8000, 10, datas.values()));
		enemies.put(4, new Enemy(4, 10000, 2000, 10, datas.values()));
		enemies.put(5, new Enemy(5, 11000, 6000, 10, datas.values()));
		enemies.put(6, new Enemy(6, 13000, 3000, 10, datas.values()));
		enemies.put(7, new Enemy(7, 14000, 5000, 10, datas.values()));
		enemies.put(8, new Enemy(8, 15000, 3500, 10, datas.values()));
		enemies.put(9, new Enemy(9, 15000, 4000, 10, datas.values()));
		
		List<Action> actions = new ArrayList<>();
		actions.add(new MoveToData());
		actions.add(new ShootClosestEnemyFromData());
		
		long start = System.currentTimeMillis();
		Game game = new Game(player, datas, enemies, actions);
		LinkedList<Action> actionsList = game.getListOfActions();
		long end = System.currentTimeMillis();
	    long elasped = end - start;
	    System.err.println("Time : " + elasped);
	    
		System.out.println(game.score);
	}
	
	@Test
	public void should_have_a_score_of_when_test_case_is_get_in_close() {
		Joueur player = new Joueur(3000, 3000);
		Map<Integer, Data> datas = new HashMap<>();
		datas.put(0, new Data(0, 1000, 1000));
		
		Map<Integer, Enemy> enemies = new HashMap<>();
		enemies.put(0, new Enemy(0, 13900, 7940, 19, datas.values()));
		enemies.put(1, new Enemy(1, 15999, 6999, 10, datas.values()));
		
		List<Action> actions = new ArrayList<>();
		actions.add(new MoveToData());
		actions.add(new ShootClosestEnemyFromData());
		
		long start = System.currentTimeMillis();
		Game game = new Game(player, datas, enemies, actions);
		LinkedList<Action> actionsList = game.getListOfActions();
		long end = System.currentTimeMillis();
	    long elasped = end - start;
	    System.err.println("Time : " + elasped);
	    
		System.out.println(game.score);
	}
	
	@Test
	public void should_shoot_enemy_when_will_die_at_next_turn() {
		Joueur player = new Joueur(0, 500);
		Map<Integer, Data> datas = new HashMap<>();
		datas.put(1, new Data(1, 0, 500));
		
		Map<Integer, Enemy> enemies = new HashMap<>();
		enemies.put(6, new Enemy(6, 2169, 1946, 10, datas.values()));
		
		List<Action> actions = new ArrayList<>();
		actions.add(new MoveToData());
		actions.add(new ShootClosestEnemyFromData());
		
		long start = System.currentTimeMillis();
		Game game = new Game(player, datas, enemies, actions);
		LinkedList<Action> actionsList = game.getListOfActions();
		long end = System.currentTimeMillis();
	    long elasped = end - start;
	    System.err.println("Time : " + elasped);
	    
	    int score = 131;
	    int sizeAction = 1;
	    assertEquals(score, game.score);
	    assertEquals(sizeAction, actionsList.size());
	    assertEquals("[SHOOT 6]", actionsList.toString());
	}
	
	@Test
	public void should_shoot_target_when_game_is_almost_lost() {
		//CrackDown Turn 33
		Joueur player = new Joueur(1322, 1265);
		Map<Integer, Data> datas = new HashMap<>();
		datas.put(1, new Data(1, 37, 3575));
		
		Map<Integer, Enemy> enemies = new HashMap<>();
		enemies.put(0, new Enemy(0, 2662, 3168, 1, datas.values()));
		enemies.put(1, new Enemy(1, 712, 3453, 15, datas.values()));
		enemies.put(3, new Enemy(3, 2537, 3439, 1, datas.values()));
		enemies.put(4, new Enemy(4, 690, 3685, 5, datas.values()));
		enemies.put(8, new Enemy(8, 2887, 4275, 5, datas.values()));
		enemies.put(9, new Enemy(9, 2662, 3957, 1, datas.values()));
		enemies.put(12, new Enemy(12, 2537, 3690, 1, datas.values()));
		enemies.put(17, new Enemy(17, 1009, 3850, 15, datas.values()));
		
		List<Action> actions = new ArrayList<>();
		actions.add(new MoveToDataInDanger());
		actions.add(new ShootClosestEnemyFromData());
		actions.add(new MoveToSafestPosition());
		
		long start = System.currentTimeMillis();
		Game game = new Game(player, datas, enemies, actions);
		LinkedList<Action> actionsList = game.getListOfActions();
		int score = game.score;
		long end = System.currentTimeMillis();
	    long elasped = end - start;
	    
	    assertEquals("SHOOT 4", actionsList.get(0).toString());
	    assertEquals("SHOOT 1", actionsList.get(1).toString());
	    System.err.println("Time : " + elasped);
	}	
}