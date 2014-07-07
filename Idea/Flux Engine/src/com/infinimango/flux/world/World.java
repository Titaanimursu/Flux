package com.infinimango.flux.world;

import com.infinimango.flux.Debug;
import com.infinimango.flux.graphics.SpriteSheet;
import com.infinimango.flux.world.entity.Creature;
import com.infinimango.flux.world.entity.Entity;
import com.infinimango.flux.world.entity.Item;
import com.infinimango.flux.world.entity.Player;
import com.infinimango.flux.world.tile.AnimatedTile;
import com.infinimango.flux.world.tile.Tile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class World {
	// Lists of all entities handled by the World
	public List<Entity> entities = new ArrayList<Entity>();
	public List<Creature> creatures = new ArrayList<Creature>();
	public List<Item> items = new ArrayList<Item>();
	public Player player;

	// Worlds tile map data
	public Tile tiles[][];
	public int data[][];

	// Rendering type of the world
	public static final int TYPE_EMPTY = 0;
	public static final int TYPE_STATIC = 1;
	public static final int TYPE_DYNAMIC = 2;
	int type = TYPE_EMPTY;

	// Looks of the tiles in a SpriteSheet
	SpriteSheet tileTextures;

	// Map dimensions in tiles and tile size
	public int width, height, tileWidth, tileHeight;

	// World generator
	TileGenerator gen;

	/**
	 * Creates and initializes a new empty game world for easy entity storage.
	 */
	public World(){
		type = TYPE_EMPTY;
	}

	/**
	 * Creates and initializes a new game world which uses textures from a SpriteSheet with tile data.
	 * @param width Width of the world map in tiles
	 * @param height Height of the world map int tiles
	 * @param tileTextures Textures of the tiles
	 */
	public World(int width, int height, SpriteSheet tileTextures){
		data = new int[width][height];
		this.width = width;
		this.height = height;
		this.tileTextures = tileTextures;
		tileWidth = tileTextures.getSpriteWidth();
		tileHeight = tileTextures.getSpriteHeight();
		type = TYPE_STATIC;
		Debug.out("New static world created");
		generate(data);
	}

	/**
	 * Creates and initializes a new generated game world which uses textures from a SpriteSheet with tile data.
	 * @param width Width of the world map in tiles
	 * @param height Height of the world map int tiles
	 * @param tileTextures Textures of the tiles
	 * @param gen Generator for the level design
	 */
	public World(int width, int height, SpriteSheet tileTextures, TileGenerator gen){
		this(width, height, tileTextures);
		this.gen = gen;
		gen.generate(data);
		Debug.out("World generated");
	}

	/**
	 * Creates and initializes a new game world which uses dynamic Tiles array.
	 * @param width Width of the world map in tiles
	 * @param height Height of the world map int tiles
	 * @param tileWidth Width of a single tile
	 * @param tileHeight Height of a single tile
	 */
	public World(int width, int height, int tileWidth, int tileHeight){
		tiles = new Tile[width][height];
		this.width = width;
		this.height = height;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		type = TYPE_DYNAMIC;
		Debug.out("New dynamic world created");
		generate(tiles);
	}

	/**
	 * Creates and initializes a new generated game world which uses dynamic Tiles array.
	 * @param width Width of the world map in tiles
	 * @param height Height of the world map int tiles
	 * @param tileWidth Width of a single tile
	 * @param tileHeight Height of a single tile
	 * @param gen Generator for the level design
	 */
	public World(int width, int height, int tileWidth, int tileHeight, TileGenerator gen){
		this(width, height, tileWidth, tileHeight);
		this.gen = gen;
		gen.generate(tiles);
		Debug.out("World generated");
	}

	public void generate(int data[][]){}
	public void generate(Tile tiles[][]){}

	/**
	 * Update all visible entities, Camera movement and tile animations.
	 */
	public void update(){
		if(type == TYPE_DYNAMIC) {
			int startX = (Camera.getX() / tileWidth) - 1;
			int startY = (Camera.getY() / tileHeight) - 1;
			for (int y = startY; y < (startY + height + 1); y++) {
				for (int x = startX; x < (startX + width + 1); x++) {
					if (tiles[x][y] instanceof AnimatedTile) ((AnimatedTile) tiles[x][y]).update();
				}
			}
		}

		player.update(this);

		for(Creature creature : creatures){
			creature.update(this);
		}

		for (int i = 0; i < items.size(); i++) {
			Item item =  items.get(i);
			if(item.isPickedUp()) {
				items.remove(i);
				continue;
			}
			if(player.intersects(item)) {
				item.pickedUpBy(player);
			}
		}

		Camera.update();
	}

	/**
	 * Render all visible entities, Camera movement and tile animations.
	 */
	public void render(Graphics g){
		if(type != TYPE_EMPTY) {
			int startX = (Camera.getX() / tileWidth) - 1;
			int startY = (Camera.getY() / tileHeight) - 1;
			if(startX < 0) startX = 0;
			if(startY < 0) startY = 0;
			for (int y = startY; y < (startY + height + 1); y++) {
				if(y >= height) break;
				for (int x = startX; x < (startX + width + 1); x++) {
					if(x >= width) break;
					if (type == TYPE_DYNAMIC) {
						tiles[x][y].render(x * tileWidth, y * tileHeight, g);
					} else {
						int tileX = x * tileWidth - Camera.getX();
						int tileY = y * tileHeight - Camera.getY();
						g.drawImage(tileTextures.extract(data[x][y]), tileX, tileY, null);
					}
				}
			}
		}

		for(Item item : items){
			if(!item.isOnScreen()) continue;
			item.render(g);
		}

		if(player != null) player.render(g);

		for(Creature creature : creatures){
			if(!creature.isOnScreen()) continue;
			creature.render(g);
		}

		for(Entity entity : entities){
			if(!entity.isOnScreen()) continue;
			entity.render(g);
		}
	}

	/**
	 * Adds a new entity to the game world. Can be used to add player, items or creatures.
	 * @param entity Instance of an entity to add to the world entity list.
	 */
	public void add(Entity entity){
		if(entity instanceof Player){
			player = (Player)entity;
		}else if(entity instanceof Creature){
			creatures.add((Creature)entity);
		}else if(entity instanceof Item){
			items.add((Item)entity);
		}else{
			entities.add(entity);
		}
	}

	/**
	 * Returns debug string with entity and creature amounts as well as if a player exists.
	 * @return Printable string containing world entity data
	 */
	public String getEntityString(){
		return "World - E:" + entities.size() + " C:" + creatures.size() + " I:" + items.size() + " P:" + (player != null);
	}

	/**
	 * Sets a new player to the world.
	 * @param player New player
	 */
	public void setPlayer(Player player){
		this.player = player;
	}

	/**
	 * Sets the world rendering type.
	 * @param type New type
	 */
	public void setType(int type){
		this.type = type;
	}

	/**
	 * Gets the world rendering type.
	 * @return World type
	 */
	public int getType(){
		return type;
	}

	/**
	 * Gets the worlds width in pixels.
	 * @return World width
	 */
	public int getWidthInPixels(){
		return width * tileWidth;
	}

	/**
	 * Gets the worlds height in pixels.
	 * @return World height
	 */
	public int getHeightInPixels(){
		return height * tileWidth;
	}
}
