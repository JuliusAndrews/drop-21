package org.cistercian.drop;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {

    // Static constants
    private static final int SPRITE_SIZE = 64;
    private static final int RAINDROP_SPEED = 200;
	private static final int RAINDROP_INTERVAL = 1_000_000_000;
    private static final int BUCKET_SPEED = 500;

    // Constant fields
	final Drop game;

    // Variable fields
	Texture dropImage;
	Texture bucketImage;
	Sound dropSound;
	Music rainMusic;
	OrthographicCamera camera;
	Rectangle bucket;
	Array<Rectangle> raindrops;
	long lastDropTime;
	int dropsGathered;
	int dropsMissed;
    int highScore;
	int lifes = 3;
	int difficulty=15;
	int gameMode;
	int score;

    /**
     * Create the objects for this screen. Note that Screens use the constructor rather than a
     * create() method to create the game objects.
     * 
     * @param gam A reference to the central Game object.
     */
    public GameScreen(final Drop gam, int mode) {
		this.game = gam;
		gameMode = mode;

		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
		rainMusic.setLooping(true);

		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Drop.WORLD_WIDTH, Drop.WORLD_HEIGHT);

		// create a Rectangle to logically represent the bucket
		bucket = new Rectangle();
		bucket.x = Drop.WORLD_WIDTH / 2 - SPRITE_SIZE / 2; // center the bucket horizontally
		bucket.y = 20; // bottom left corner of the bucket is 20 pixels above
						// the bottom screen edge
		bucket.width = SPRITE_SIZE;
		bucket.height = SPRITE_SIZE;

		// create the raindrops array and spawn the first raindrop
		raindrops = new Array<Rectangle>();
		spawnRaindrop();

	}


    /* This method creates an individual raindrop and resets the lastDropTime. */
	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, Drop.WORLD_WIDTH - SPRITE_SIZE);
		raindrop.y = Drop.WORLD_HEIGHT;
		raindrop.width = SPRITE_SIZE;
		raindrop.height = SPRITE_SIZE;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}


    /**
	 * This method is called by every frame. It updates the game state and renders the game world.
	 */
	@Override
	public void render(float delta) {
		// clear the screen with a dark blue color. The
		// arguments to clear are the red, green
		// blue and alpha component in the range [0,1]
		// of the color to be used to clear the screen.
		ScreenUtils.clear(0, 0, 0.2f, 1);

		// tell the camera to update its matrices.
		camera.update();

		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		game.batch.setProjectionMatrix(camera.combined);

		// begin a new batch and draw the bucket and
		// all drops
		game.batch.begin();
		if (gameMode==1){
		game.font.draw(game.batch, "Drops Collected: " + dropsGathered, 0, Drop.WORLD_HEIGHT);
		} else {
		game.font.draw(game.batch, "Drops Dodged: " + dropsMissed, 0, Drop.WORLD_HEIGHT);
		}
        game.font.draw(game.batch, "High Score: " + highScore, 0, Drop.WORLD_HEIGHT - 15);
		game.font.draw(game.batch, "Lives: " + lifes, 0, Drop.WORLD_HEIGHT - 30);
		game.batch.draw(bucketImage, bucket.x, bucket.y);
		for (Rectangle raindrop : raindrops) {
			game.batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		game.batch.end();

		// process user input
		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - SPRITE_SIZE / 2;
			bucket.y = touchPos.y - SPRITE_SIZE / 2;
		}
		if (Gdx.input.isKeyPressed(Keys.LEFT))
			bucket.x -= (BUCKET_SPEED + score*difficulty)* Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Keys.RIGHT))
			bucket.x += (BUCKET_SPEED + score *difficulty)* Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Keys.UP))
			bucket.y += (BUCKET_SPEED + score *difficulty)* Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Keys.DOWN))
			bucket.y -= (BUCKET_SPEED + score *difficulty)* Gdx.graphics.getDeltaTime();

		// make sure the bucket stays within the screen bounds
		if (bucket.x < 0)
			bucket.x = 0;
		if (bucket.x > Drop.WORLD_WIDTH - SPRITE_SIZE)
			bucket.x = Drop.WORLD_WIDTH - SPRITE_SIZE;
		if (bucket.y < 0)
			bucket.y = 0;
		if (bucket.y > Drop.WORLD_HEIGHT - SPRITE_SIZE)
			bucket.y = Drop.WORLD_HEIGHT - SPRITE_SIZE;

		// check if we need to create a new raindrop
		if (TimeUtils.nanoTime() - lastDropTime > (RAINDROP_INTERVAL-(score*10_000_000)))
			spawnRaindrop();

		// move the raindrops, remove any that are beneath the bottom edge of
		// the screen or that hit the bucket. In the later case we play back
		// a sound effect as well.
		Iterator<Rectangle> iter = raindrops.iterator();
		while (iter.hasNext()) {
			score = dropsGathered + dropsMissed;
			Rectangle raindrop = iter.next();
			raindrop.y -= (RAINDROP_SPEED + score * difficulty )* Gdx.graphics.getDeltaTime();
			if (raindrop.y + SPRITE_SIZE < 0){
				dropsMissed++;
				dropsGathered = 0;
				if (gameMode == 1){
					lifes --;
					} else {
						dropSound.play();
						if (highScore < dropsMissed) {
							highScore = dropsMissed;
						}
					}
				iter.remove();
				
            }
			if (lifes <=0){
				game.setScreen(new GameOverScreen(game,highScore));
				dispose();
			}
			if (raindrop.overlaps(bucket)) {
				dropsGathered++;
				dropsMissed = 0;
				if (gameMode == 2){
					lifes --;
					} else {
						dropSound.play();
						if (highScore < dropsGathered) {
							highScore = dropsGathered;
						}
					}

				iter.remove();
			}
		}
	}


    @Override
	public void resize(int width, int height) {
	}


    /**
     * This method is called when the game screen becomes visible.
     */
    @Override
	public void show() {
		// start the playback of the background music
		// when the screen is shown
		rainMusic.play();
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


    /**
	 * This method is called when the application is disposed in order to dispose of
	 * all internal resources.
	 */
    @Override
	public void dispose() {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
	}
}