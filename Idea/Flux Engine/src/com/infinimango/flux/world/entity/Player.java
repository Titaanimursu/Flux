package com.infinimango.flux.world.entity;

import com.infinimango.flux.world.World;

public abstract class Player extends Creature {

	/**
	 * Creates a new playable character.
	 * @param x Horizontal location of the player
	 * @param y Vertical location of the player
	 */
	public Player(float x, float y){
		super(x, y);
		init();
	}

	/**
	 * Initializing method for loading all needed resources etc.
	 */
	public abstract void init();

	/**
	 * Updates the players actions.
	 * @param world Game world for checking collisions etc.
	 */
	public abstract void update(World world);
}
