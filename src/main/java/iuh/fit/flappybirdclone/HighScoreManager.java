package iuh.fit.flappybirdclone;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * HighScoreManager: đọc/ghi file highscore.txt (mỗi lần game over cập nhật).
 * File được lưu ở thư mục working directory (cùng cấp with target).
 */
public class HighScoreManager {
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    private static int currentHighScore = 0;
    private static int previousHighScore = 0;

    /**
     * Gọi khi khởi tạo Game lần đầu: load giá trị high score từ file nếu có.
     */
    public static void initialize() {
        try {
            Path p = Paths.get(HIGH_SCORE_FILE);
            if (Files.exists(p)) {
                BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE));
                String line = reader.readLine();
                if (line != null) {
                    try {
                        currentHighScore = Integer.parseInt(line.trim());
                    } catch (NumberFormatException ex) {
                        currentHighScore = 0;
                    }
                }
                reader.close();
            } else {
                // Nếu chưa có file, tạo file rỗng với giá trị 0
                currentHighScore = 0;
                writeHighScoreToFile(0);
            }
        } catch (IOException e) {
            System.err.println("Lỗi HighScoreManager.initialize(): " + e.getMessage());
            currentHighScore = 0;
        }
        // Khi mới khởi, previousHighScore = currentHighScore
        previousHighScore = currentHighScore;
    }

    /**
     * Khi game over xong, gọi onGameOver() để so sánh và ghi file nếu có kỷ lục mới.
     * @param score điểm vừa chơi được
     */
    public static void onGameOver(int score) {
        if (score > currentHighScore) {
            previousHighScore = currentHighScore;
            currentHighScore = score;
            writeHighScoreToFile(currentHighScore);
        } else {
            previousHighScore = currentHighScore;
        }
    }

    /**
     * Cập nhật high score, trả về true nếu có highscore mới.
     * @param score hiện tại
     * @return true nếu score > currentHighScore
     */
    public static boolean updateHighScore(int score) {
        if (score > currentHighScore) {
            currentHighScore = score;
            writeHighScoreToFile(currentHighScore);
            return true;
        }
        return false;
    }

    private static void writeHighScoreToFile(int value) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE));
            writer.write(Integer.toString(value));
            writer.close();
        } catch (IOException e) {
            System.err.println("Lỗi HighScoreManager.writeHighScoreToFile(): " + e.getMessage());
        }
    }

    public static int getCurrentHighScore() {
        return currentHighScore;
    }

    public static int getPreviousHighScore() {
        return previousHighScore;
    }
}