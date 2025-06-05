package iuh.fit.flappybirdclone;

import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ResourceManager: tải và cung cấp hình ảnh/âm thanh cho game.
 */
public class ResourceManager {
    // Hình ảnh
    private static Image birdImage;
    private static Image pipeImage;
    private static Image pipeRedImage;                 // ← THÊM biến này
    private static List<Image> backgroundImages = new ArrayList<>();

    // Âm thanh (Effects)
    private static AudioClip flapSound;
    private static AudioClip gameOverSound;
    private static AudioClip pointSound;

    // Nhạc nền Menu và Game (Music)
    private static MediaPlayer menuMusicPlayer;
    private static MediaPlayer gameMusicPlayer;

    // ← THÊM: cờ bật/tắt âm thanh
    private static boolean musicEnabled = true;
    private static boolean effectsEnabled = true;
    // ← KẾT THÊM

    // Trạng thái tải tài nguyên
    private static boolean resourcesLoaded = false;

    /**
     * Tải tất cả tài nguyên (gọi loadImages() + loadSounds()).
     */
    public static void loadResources() {
        try {
            System.out.println("Bắt đầu tải tài nguyên...");
            loadImages();
            loadSounds();
            resourcesLoaded = true;
            System.out.println("Tài nguyên đã được tải thành công!");
            System.out.println("Đã tải " + backgroundImages.size() + " background");
        } catch (Exception e) {
            System.err.println("Lỗi khi tải tài nguyên: " + e.getMessage());
            e.printStackTrace();
            resourcesLoaded = false;
        }
    }

    /**
     * Tải hình ảnh (chim, ống, ống đỏ, background).
     */
    private static void loadImages() {
        try {
            System.out.println("Đang tải hình ảnh...");

            // --- Tải hình chim (danh sách tên tham khảo) ---
            String[] birdNames = {"bird.png", "Bird.png", "flappy.png", "player.png"};
            birdImage = null;
            for (String name : birdNames) {
                Image img = loadImageFromResources("images/" + name);
                if (img != null) {
                    birdImage = img;
                    System.out.println("✓ Đã tải hình chim: " + name);
                    break;
                }
            }
            if (birdImage == null) {
                System.out.println("⚠ Không tìm thấy hình chim, sẽ vẽ hình tròn thay thế.");
            }

            // --- Tải hình ống ---
            String[] pipeNames = {"pipe.png", "Pipe.png", "obstacle.png", "tube.png"};
            pipeImage = null;
            for (String name : pipeNames) {
                Image img = loadImageFromResources("images/" + name);
                if (img != null) {
                    pipeImage = img;
                    System.out.println("✓ Đã tải hình ống: " + name);
                    break;
                }
            }
            if (pipeImage == null) {
                System.out.println("⚠ Không tìm thấy hình ống, sẽ vẽ hình chữ nhật thay thế.");
            }

            // ← THÊM: Tải hình ống đỏ (pipe-red.png)
            pipeRedImage = loadImageFromResources("images/pipe-red.png");
            if (pipeRedImage != null) {
                System.out.println("✓ Đã tải hình ống đỏ: pipe-red.png");
            } else {
                System.out.println("⚠ Không tìm thấy pipe-red.png");
            }
            // ← KẾT THÊM

            // --- Tải background (từ background1.png đến background10.png) ---
            backgroundImages.clear();
            for (int i = 1; i <= 10; i++) {
                Image bg = loadImageFromResources("images/background" + i + ".png");
                if (bg == null) {
                    bg = loadImageFromResources("images/background" + i + ".jpg");
                }
                if (bg != null) {
                    backgroundImages.add(bg);
                    System.out.println("✓ Đã tải background" + i);
                }
            }

            // Nếu vẫn rỗng (không có background1..10), thử các tên chung:
            if (backgroundImages.isEmpty()) {
                String[] bgNames = {"background.png", "Background.png", "bg.png", "bg.jpg", "sky.png", "sky.jpg"};
                for (String name : bgNames) {
                    Image bg = loadImageFromResources("images/" + name);
                    if (bg != null) {
                        backgroundImages.add(bg);
                        System.out.println("✓ Đã tải: " + name);
                        break;
                    }
                }
            }

            if (backgroundImages.isEmpty()) {
                System.out.println("⚠ Không tìm thấy background nào, sẽ vẽ gradient thay thế.");
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải hình ảnh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tải âm thanh (nhảy, game over, điểm) và nhạc nền Menu/Game.
     */
    private static void loadSounds() {
        try {
            System.out.println("Đang tải âm thanh...");

            // --- Âm thanh nhảy ---
            String[] flapNames = {"flap.wav", "Flap.wav", "jump.wav", "wing.wav", "fly.wav"};
            flapSound = null;
            for (String name : flapNames) {
                AudioClip clip = loadSoundFromResources("sounds/" + name);
                if (clip != null) {
                    flapSound = clip;
                    flapSound.setVolume(0.7);
                    System.out.println("✓ Đã tải âm thanh nhảy: " + name);
                    break;
                }
            }
            if (flapSound == null) {
                System.out.println("⚠ Không tìm thấy âm thanh nhảy.");
            }

            // --- Âm thanh game over ---
            String[] gameOverNames = {"gameover.wav", "GameOver.wav", "death.wav", "lose.wav"};
            gameOverSound = null;
            for (String name : gameOverNames) {
                AudioClip clip = loadSoundFromResources("sounds/" + name);
                if (clip != null) {
                    gameOverSound = clip;
                    gameOverSound.setVolume(0.8);
                    System.out.println("✓ Đã tải âm thanh game over: " + name);
                    break;
                }
            }
            if (gameOverSound == null) {
                System.out.println("⚠ Không tìm thấy âm thanh game over.");
            }

            // --- Âm thanh điểm (point.wav) ---
            String[] pointNames = {"point.wav", "Point.wav", "score.wav"};
            pointSound = null;
            for (String name : pointNames) {
                AudioClip clip = loadSoundFromResources("sounds/" + name);
                if (clip != null) {
                    pointSound = clip;
                    pointSound.setVolume(0.8);
                    System.out.println("✓ Đã tải âm thanh point: " + name);
                    break;
                }
            }
            if (pointSound == null) {
                System.out.println("⚠ Không tìm thấy âm thanh point.wav.");
            }

            // ----- Load nhạc nền Menu bằng Class.getResource(...) -----
            URL menuUrl = ResourceManager.class.getResource("/sounds/menuBackground.wav");
            if (menuUrl != null) {
                Media mediaMenu = new Media(menuUrl.toExternalForm());
                menuMusicPlayer = new MediaPlayer(mediaMenu);
                menuMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                System.out.println("✓ Đã tải nhạc nền Menu: menuBackground.wav");
            } else {
                System.out.println("⚠ Không tìm thấy menuBackground.wav");
            }
            // ----- Kết thúc load nhạc Menu -----

            // ----- Load nhạc nền Game bằng Class.getResource(...) -----
            URL gameUrl = ResourceManager.class.getResource("/sounds/gameBackground.wav");
            if (gameUrl != null) {
                Media mediaGame = new Media(gameUrl.toExternalForm());
                gameMusicPlayer = new MediaPlayer(mediaGame);
                gameMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                System.out.println("✓ Đã tải nhạc nền Game: gameBackground.wav");
            } else {
                System.out.println("⚠ Không tìm thấy gameBackground.wav");
            }
            // ----- Kết thúc load nhạc Game -----

        } catch (Exception e) {
            System.err.println("Lỗi tải âm thanh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper: tải một Image từ classpath (src/main/resources).
     * Trả về null nếu không tìm thấy.
     */
    private static Image loadImageFromResources(String path) {
        try {
            // Thử getResourceAsStream
            InputStream is = ResourceManager.class.getClassLoader().getResourceAsStream(path);
            if (is != null) {
                Image img = new Image(is);
                is.close();
                return img;
            }
            // Thử getResource → URL
            URL url = ResourceManager.class.getClassLoader().getResource(path);
            if (url != null) {
                return new Image(url.toExternalForm());
            }
            // Thử path gốc
            url = ResourceManager.class.getResource("/" + path);
            if (url != null) {
                return new Image(url.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải hình ảnh " + path + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Helper: tải một AudioClip từ classpath.
     * Trả về null nếu không tìm thấy.
     */
    private static AudioClip loadSoundFromResources(String path) {
        try {
            // Thử getResource
            URL url = ResourceManager.class.getClassLoader().getResource(path);
            if (url != null) {
                return new AudioClip(url.toExternalForm());
            }
            // Thử class.getResource
            url = ResourceManager.class.getResource("/" + path);
            if (url != null) {
                return new AudioClip(url.toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải âm thanh " + path + ": " + e.getMessage());
        }
        return null;
    }

    // Phát âm thanh nhảy – chỉ khi effectsEnabled = true
    public static void playFlapSound() {
        if (effectsEnabled && flapSound != null) {
            try { flapSound.play(); }
            catch (Exception e) {
                System.err.println("Lỗi phát sound flap: " + e.getMessage());
            }
        }
    }

    // Phát âm thanh game over – chỉ khi effectsEnabled = true
    public static void playGameOverSound() {
        if (effectsEnabled && gameOverSound != null) {
            try { gameOverSound.play(); }
            catch (Exception e) {
                System.err.println("Lỗi phát sound game over: " + e.getMessage());
            }
        }
    }

    // Phát âm thanh point (khi vượt qua ống) – chỉ khi effectsEnabled = true
    public static void playPointSound() {
        if (effectsEnabled && pointSound != null) {
            try { pointSound.play(); }
            catch (Exception e) {
                System.err.println("Lỗi phát sound point: " + e.getMessage());
            }
        }
    }

    // Lấy hình chim
    public static Image getBirdImage() {
        return birdImage;
    }

    // ← THÊM: cho phép đổi ảnh chim động (GIF) từ màn Chọn nhân vật
    public static void setBirdImage(Image customBird) {
        birdImage = customBird;
    }
    // ← KẾT THÊM

    // Lấy hình ống
    public static Image getPipeImage() {
        return pipeImage;
    }

    // ← THÊM: Lấy hình ống đỏ (HighScore pipe)
    public static Image getPipeRedImage() {
        return pipeRedImage;
    }
    // ← KẾT THÊM

    // Lấy background theo index (0-based), nếu out of range thì trả background cuối
    public static Image getBackgroundImage(int index) {
        if (backgroundImages.isEmpty()) {
            return null;
        }
        if (index < 0) index = 0;
        if (index >= backgroundImages.size()) index = backgroundImages.size() - 1;
        return backgroundImages.get(index);
    }

    // Tổng số background đã load
    public static int getBackgroundCount() {
        return backgroundImages.size();
    }

    // Kiểm tra đã load resources xong chưa
    public static boolean isResourcesLoaded() {
        return resourcesLoaded;
    }

    /**
     * In ra trạng thái đã load được bao nhiêu tài nguyên (debug).
     */
    public static void printLoadedResources() {
        System.out.println("=== TÌNH TRẠNG TÀI NGUYÊN ===");
        System.out.println("Bird Image: " + (birdImage != null ? "✓ Loaded" : "✗ Not loaded"));
        System.out.println("Pipe Image: " + (pipeImage != null ? "✓ Loaded" : "✗ Not loaded"));
        System.out.println("Pipe Red Image: " + (pipeRedImage != null ? "✓ Loaded" : "✗ Not loaded"));
        System.out.println("Background Images: " + backgroundImages.size() + " loaded");
        System.out.println("Flap Sound: " + (flapSound != null ? "✓ Loaded" : "✗ Not loaded"));
        System.out.println("GameOver Sound: " + (gameOverSound != null ? "✓ Loaded" : "✗ Not loaded"));
        System.out.println("Point Sound: " + (pointSound != null ? "✓ Loaded" : "✗ Not loaded"));
        System.out.println("Menu Music: " + (menuMusicPlayer != null ? "✓ Loaded" : "✗ Not loaded"));
        System.out.println("Game Music: " + (gameMusicPlayer != null ? "✓ Loaded" : "✗ Not loaded"));
        System.out.println("Music Enabled: " + (musicEnabled ? "On" : "Off"));
        System.out.println("Effects Enabled: " + (effectsEnabled ? "On" : "Off"));
        System.out.println("============================");
    }

    /**
     * Debug: in ra đường dẫn thư mục images, sounds, root resources để kiểm tra.
     */
    public static void debugResourceStructure() {
        System.out.println("=== DEBUG RESOURCE STRUCTURE ===");
        URL imagesUrl = ResourceManager.class.getClassLoader().getResource("images");
        System.out.println("Images folder: " + (imagesUrl != null ? imagesUrl.getPath() : "NOT FOUND"));
        URL soundsUrl = ResourceManager.class.getClassLoader().getResource("sounds");
        System.out.println("Sounds folder: " + (soundsUrl != null ? soundsUrl.getPath() : "NOT FOUND"));
        URL rootUrl = ResourceManager.class.getClassLoader().getResource("");
        System.out.println("Root resources: " + (rootUrl != null ? rootUrl.getPath() : "NOT FOUND"));
        System.out.println("===============================");
    }

    // ----- Thêm hai phương thức để bật/tắt nhạc nền Menu và Game -----

    /** Phát nhạc nền Menu (loop vô hạn) – chỉ khi musicEnabled = true. */
    public static void playMenuMusic() {
        if (musicEnabled && menuMusicPlayer != null) {
            try {
                menuMusicPlayer.play();
            } catch (Exception e) {
                System.err.println("Lỗi playMenuMusic(): " + e.getMessage());
            }
        }
    }

    /** Dừng nhạc nền Menu. */
    public static void stopMenuMusic() {
        if (menuMusicPlayer != null) {
            try {
                menuMusicPlayer.stop();
            } catch (Exception e) {
                System.err.println("Lỗi stopMenuMusic(): " + e.getMessage());
            }
        }
    }

    /** Phát nhạc nền Game (loop vô hạn) – chỉ khi musicEnabled = true. */
    public static void playGameMusic() {
        if (musicEnabled && gameMusicPlayer != null) {
            try {
                gameMusicPlayer.play();
            } catch (Exception e) {
                System.err.println("Lỗi playGameMusic(): " + e.getMessage());
            }
        }
    }

    /** Dừng nhạc nền Game. */
    public static void stopGameMusic() {
        if (gameMusicPlayer != null) {
            try {
                gameMusicPlayer.stop();
            } catch (Exception e) {
                System.err.println("Lỗi stopGameMusic(): " + e.getMessage());
            }
        }
    }

    // ← THÊM: Getter/Setter cho musicEnabled và effectsEnabled

    /**
     * @return true nếu Music (Menu + Game) đang được bật
     */
    public static boolean isMusicEnabled() {
        return musicEnabled;
    }

    /**
     * Bật/tắt toàn bộ phần Music (Menu + Game).
     * Nếu tắt ngay thì dừng mọi nhạc đang phát.
     */
    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (!enabled) {
            if (menuMusicPlayer != null) menuMusicPlayer.stop();
            if (gameMusicPlayer != null) gameMusicPlayer.stop();
        }
    }

    /**
     * @return true nếu Effects (flap, game over, point) đang được bật
     */
    public static boolean isEffectsEnabled() {
        return effectsEnabled;
    }

    /**
     * Bật/tắt toàn bộ phần Effects (âm thanh nhảy, game over, point).
     */
    public static void setEffectsEnabled(boolean enabled) {
        effectsEnabled = enabled;
    }
    // ← KẾT THÊM
}
