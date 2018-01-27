package gravity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.scene.media.AudioClip;

public class Sound {
	private static final ExecutorService soundPool = Executors.newFixedThreadPool(10);
	private static final Map<String, ArrayList<AudioClip>> soundEffectsMap = new HashMap<>();
	private static final Random random = new Random();
	public static final String BASE_DIR = "../sounds/";

	public Sound() {
		loadSoundEffect("shot", "shot0.mp3");
		loadSoundEffect("shot", "shot1.mp3");
		loadSoundEffect("shot", "shot2.mp3");
		loadSoundEffect("hitshield", "hitshield0.mp3");
		loadSoundEffect("hitshield", "hitshield1.mp3");
		loadSoundEffect("hitshield", "hitshield2.mp3");
		loadSoundEffect("hitshield", "hitshield3.mp3");
		loadSoundEffect("hitcraft", "hitcraft0.mp3");
		loadSoundEffect("hitcraft", "hitcraft1.mp3");
		loadSoundEffect("crash", "crash.mp3");
		loadSoundEffect("wall", "wall.mp3");
		loadSoundEffect("fire", "fire.mp3");
		loadSoundEffect("explosion", "explosion0.mp3");
		loadSoundEffect("explosion", "explosion1.mp3");
		loadSoundEffect("hiss", "hiss.mp3");
		loadSoundEffect("crackle", "crackle.mp3");
	}

	public void loadSoundEffect(String id, String fileName) {
		AudioClip sound = new AudioClip(getClass().getResource(BASE_DIR + fileName).toExternalForm());
		ArrayList<AudioClip> list;

		if (soundEffectsMap.containsKey(id)) {
			list = soundEffectsMap.get(id);
		} else {
			list = new ArrayList<>();
			soundEffectsMap.put(id, list);
		}
		list.add(sound);
	}

	public void playSound(final String id) {
		Runnable soundPlay = new Runnable() {
			public void run() {
				ArrayList<AudioClip> list;
				if (soundEffectsMap.containsKey(id)) {
					list = soundEffectsMap.get(id);
					AudioClip sound = list.get(random.nextInt(list.size()));
					sound.play();
				} else {
					System.err.println("no such sound" + id);
				}
			}
		};
		soundPool.execute(soundPlay);
	}

	public void shutdown() {
		soundPool.shutdown();
	}
}