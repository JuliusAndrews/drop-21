package org.cistercian.drop;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * This class is the main class of the game, which sets up the basics, and then
 * launches the first screen.
 */
public class Drop extends Game {

    // constants
    public static final int WORLD_WIDTH = 800;
    public static final int WORLD_HEIGHT = 480;

    // fields
    SpriteBatch batch;
	BitmapFont font;


  	/**
	 * This method creates the various objects needed for the game. It runs once at the
	 * beginning of the game.
	 */
	public void create() {
		batch = new SpriteBatch();
		// Use LibGDX's default Arial font.
		font = new BitmapFont();
		this.setScreen(new MainMenuScreen(this));
	}


	/**
	 * This method is called by every frame. It updates the game state and renders the game world.
	 */
	@Override
	public void render() {
		super.render(); // important!
	}


	/**
	 * This method is called when the application is disposed in order to dispose of
	 * all internal resources.
	 */
	@Override
    public void dispose() {
		batch.dispose();
		font.dispose();
	}
}