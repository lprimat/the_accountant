package com.lprimat.codingame;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
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
	public void simulate_simple_test_cases_without_actions() {
		Joueur player = new Joueur(1100, 1200);
		Map<Integer, Data> datas = new HashMap<>();
		datas.put(0, new Data(0,8250,4500));
		Map<Integer, Enemy> enemies = new HashMap<>();
		enemies.put(0, new Enemy(0,8250,8999,10, datas.values()));
		player.target = enemies.get(0);
		
		Game game = new Game(player, datas, enemies);
		game.simulate();
		int finalScore = 110;
		int nbShot = 5;
		
		assertEquals(nbShot, player.nbShot);
		assertEquals(finalScore, game.score);
	}
	
	@Test
	public void simulate_lost_game() {
		Joueur player = new Joueur(1100, 1200);
		Map<Integer, Data> datas = new HashMap<>();
		datas.put(0, new Data(0,8250,4500));
		Map<Integer, Enemy> enemies = new HashMap<>();
		enemies.put(0, new Enemy(0,8250,8999,10, datas.values()));
		
		Game game = new Game(player, datas, enemies);
		game.simulate();
		int finalScore = 0;
		int nbShot = 0;
		int nbTurn = 9;
		
		assertEquals(nbShot, player.nbShot);
		assertEquals(finalScore, game.score);
		assertEquals(nbTurn, game.nbTurn);
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
		
		Game game = new Game(player, datas, enemies, actions, 0);
		game.getFirstAction();
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
		actions.add(new MoveToData());
		actions.add(new ShootClosestEnemyFromData());
		
		Game game = new Game(player, datas, enemies, actions, 0);
		System.out.println(game.getFirstAction().toString());
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
		
		Game game = new Game(player, datas, enemies, actions, 0);
		System.out.println(game.getFirstAction().toString());
		System.out.println(game.score);
	}
}
