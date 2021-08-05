package org.cistercian.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;


/**
 * This class is the main class of the game.
 */
public class Drop extends ApplicationAdapter {

	// constants
	private static final int WORLD_WIDTH = 800;
	private static final int WORLD_HEIGHT = 480;
	private static final int SPRITE_SIZE = 64;
	private static final int RAINDROP_SPEED = 200;
	private static final int RAINDROP_INTERVAL = 1_000_000_000;
	private static final int BUCKET_SPEED = 200;

	// fields
	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Rectangle bucket;
	private Array<Rectangle> raindrops;
	private long lastDropTime;


	/**
	 * This method creates the various objects needed for the game. It runs once at the
	 * beginning of the game.
	 */
	@Override
	public void create () {
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
		rainMusic.play();

		// create the camera and SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
		batch = new SpriteBatch();

		// setup the bucket object
		bucket = new Rectangle();
		bucket.x = WORLD_WIDTH / 2 - SPRITE_SIZE / 2;
		bucket.y = 20;
		bucket.width = SPRITE_SIZE;
		bucket.height = SPRITE_SIZE;

		// setup the raindrops
		raindrops = new Array<Rectangle>();
		spawnRaindrop();
	}


	/**
	 * This method is called by every frame. It updates the game state and renders the game world.
	 */
	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0.2f, 1);
		camera.update();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		for (Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();

		// have bucket follow the mouse
		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - SPRITE_SIZE / 2;
		}

		// use cursor arrow keys to move the bucket
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			bucket.x -= BUCKET_SPEED * Gdx.graphics.getDeltaTime();
		}
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			bucket.x += BUCKET_SPEED * Gdx.graphics.getDeltaTime();
		}

		// constrain bucket x coordinate to screen width limits
		if (bucket.x < 0) {
			bucket.x = 0;
		}
		if (bucket.x > WORLD_WIDTH - SPRITE_SIZE) {
			bucket.x = WORLD_WIDTH - SPRITE_SIZE;
		}

		// add a new raindrop if it is time
		if (TimeUtils.nanoTime() - lastDropTime > RAINDROP_INTERVAL) {
			spawnRaindrop();
		}

		// move the raindrops
		for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
			Rectangle raindrop = iter.next();
			raindrop.y -= RAINDROP_SPEED * Gdx.graphics.getDeltaTime();

			// check to see if caught in bucket
			if (raindrop.overlaps(bucket)) {
				dropSound.play();
				iter.remove();
			}
		}
	}


	/**
	 * This method is called when the application is disposed in order to dispose of
	 * all internal resources.
	 */
	@Override
	public void dispose () {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}

	/* This method creates an individual raindrop and resets the lastDropTime. */
	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, WORLD_WIDTH - SPRITE_SIZE);
		raindrop.y = WORLD_HEIGHT;
		raindrop.width = SPRITE_SIZE;
		raindrop.height = SPRITE_SIZE;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}
}
