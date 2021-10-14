package org.cistercian.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * This class implements an opening Main Menu screen for the
 * game. It shows how to draw text on the screen and switch
 * to another screen.
 */
public class MainMenuScreen implements Screen {

  final Drop game;

    // fields
	OrthographicCamera camera;

    /**
     * Create the objects for this screen. Note that Screens use the constructor rather than a
     * create() method to create the game objects.
     * 
     * @param gam A reference to the central Game object.
     */
	public MainMenuScreen(final Drop gam) {
		game = gam;

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Drop.WORLD_WIDTH, Drop.WORLD_HEIGHT);
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(0, 0, 0.2f, 1);

		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		game.font.draw(game.batch, "Welcome to Drop!!! ", 100, 150);
		game.font.draw(game.batch, "Tap anywhere to begin!", 100, 100);
		game.batch.end();

		if (Gdx.input.isTouched()) {
			game.setScreen(new GameScreen(game));
			dispose();
		}
	}

    // The following methods are required by the Screen interface, but are not needed for this screen.
	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}
}