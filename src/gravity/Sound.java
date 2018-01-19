package gravity;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.scene.media.AudioClip;

/**
 * Responsible for loading sound media to be played using an id or key. Contains
 * all sounds for use later.
 * <p/>
 * User: cdea
 */
public class Sound {
	ExecutorService soundPool = Executors.newFixedThreadPool(5);
//	Map<String, AudioClip> soundEffectsMap = new HashMap<>();
	Map<String, ArrayList<AudioClip>> soundEffectsMap = new HashMap<>();
	Random random = new Random();

	/**
	 * Constructor to create a simple thread pool.
	 *
	 * @param numberOfThreads
	 *            - number of threads to use media players in the map.
	 */
	public Sound(int numberOfThreads) {
		soundPool = Executors.newFixedThreadPool(numberOfThreads);
		loadSoundEffect("shot", "shot0.mp3");
		loadSoundEffect("shot", "shot1.mp3");
		loadSoundEffect("shot", "shot2.mp3");
		loadSoundEffect("hitshield", "hitshield0.mp3");
		loadSoundEffect("hitshield", "hitshield1.mp3");
		loadSoundEffect("hitcraft", "hitcraft0.mp3");
		loadSoundEffect("hitcraft", "hitcraft1.mp3");
		loadSoundEffect("crash", "crash.mp3");
		loadSoundEffect("wall", "wall.mp3");
		loadSoundEffect("fire", "fire.mp3");
		loadSoundEffect("explosion", "explosion0.mp3");
		loadSoundEffect("explosion", "explosion1.mp3");
		loadSoundEffect("thrust", "thrust.mp3");
	}

	public void loadSoundEffect(String id, String fileName) {
		AudioClip sound = new AudioClip(getClass().getResource("../" + fileName).toExternalForm());
		ArrayList<AudioClip> list;

		if (soundEffectsMap.containsKey(id)) {
			list = soundEffectsMap.get(id);
		} else {
			list = new ArrayList<>();
			soundEffectsMap.put(id, list);
		}
		list.add(sound);
	}

	public void playSound(final String id)
    {
    	Runnable soundPlay = new Runnable() {
    		public void run()
    		{
    	    	ArrayList<AudioClip> list;
    	    	if (soundEffectsMap.containsKey(id))
    	    	{
    	    		list = soundEffectsMap.get(id);
    	    		AudioClip sound = list.get(random.nextInt(list.size()));
    	    		sound.play();
    	    	}
    	    	else
    	    	{
    	    		System.err.println("no such sound" + id);
    	    	}
    		}
    	};
    	soundPool.execute(soundPlay);
    }

	/**
	 * Load a sound into a map to later be played based on the id.
	 *
	 * @param id
	 *            - The identifier for a sound.
	 * @param url
	 *            - The url location of the media or audio resource. Usually in
	 *            src/main/resources directory.
	 */
//	public void loadSoundEffects(String id, URL url) {
//		AudioClip sound = new AudioClip(url.toExternalForm());
//		soundEffectsMap.put(id, sound);
//	}

	/**
	 * Lookup a name resource to play sound based on the id.
	 *
	 * @param id
	 *            identifier for a sound to be played.
	 */
//	public void playSound(final String id) {
//		Runnable soundPlay = new Runnable() {
//			@Override
//			public void run() {
//				soundEffectsMap.get(id).play();
//			}
//		};
//		soundPool.execute(soundPlay);
//	}

	/**
	 * Stop all threads and media players.
	 */
	public void shutdown() {
		soundPool.shutdown();
	}

}