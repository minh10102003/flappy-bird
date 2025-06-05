package iuh.fit.flappybirdclone;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Random; // ← THÊM import này

/**
 * GameLoop: chịu trách nhiệm vòng lặp chính của game (update + render).
 * Bổ sung constructor nhận callback (Runnable) để thông báo khi Game Over.
 * - Khi tạo ống mới, nếu chỉ số ống vừa tạo (createdPipeCount) trùng với trapPipeIndex,
 *   sẽ đánh dấu ống đó là trap. Mỗi 30 ống sẽ sinh một trap mới dựa trên LCG.
 */
public class GameLoop extends AnimationTimer {
    private GraphicsContext gc;
    private double sceneWidth;
    private double sceneHeight;

    private Bird bird;
    private List<Pipe> pipes;
    private List<Boolean> pipeScored;

    private long lastPipeTime;
    private static final long PIPE_INTERVAL = 1_750_000_000L; // 1.75 giây

    private int score;
    private boolean gameOver;
    private boolean gameStarted;
    private boolean newHighScore;

    // Background index + đếm số ống đã qua để thay đổi hình nền
    private int currentBackgroundIndex;
    private int pipesPassedAtLastBackgroundChange;

    // ---------- START: PHẦN THÊM để hỗ trợ trap pipe ----------
    private int createdPipeCount;      // số ống đã tạo
    private int trapPipeIndex;         // chỉ số ống sẽ đánh dấu trap
    private int currentTrapBlock;      // block hiện tại (bắt đầu từ 0 mỗi lần khởi động)
    private Random random = new Random();
    // Các tham số của LCG (chọn sẵn)
    private static final long LCG_M = 1L << 31;         // modulus (ví dụ: 2^31)
    private static final long LCG_A = 1103515245L;      // multiplier
    private static final long LCG_C = 12345L;           // increment
    private long lcgState;                              // trạng thái hiện tại của LCG
    // ---------- END: PHẦN THÊM ----------

    // ---------- START: PHẦN THÊM để đánh dấu HighScore pipe ----------
    // Lấy HighScore hiện tại (từ HighScoreManager) làm “chỉ số ống đỏ”
    private int highScorePipeIndex;    // ← THÊM biến này
    // ---------- END: PHẦN THÊM ----------

    // Callback khi Game Over (được gọi từ Main)
    private Runnable onGameOverCallback;

    /**
     * Constructor mặc định (3 tham số) – không có callback.
     */
    public GameLoop(GraphicsContext gc, double sceneWidth, double sceneHeight) {
        this(gc, sceneWidth, sceneHeight, null);
    }

    /**
     * Constructor đầy đủ (4 tham số):
     * @param gc              GraphicsContext để vẽ
     * @param sceneWidth      chiều rộng cửa sổ
     * @param sceneHeight     chiều cao cửa sổ
     * @param onGameOverCallback callback được gọi khi Game Over
     */
    public GameLoop(GraphicsContext gc, double sceneWidth, double sceneHeight, Runnable onGameOverCallback) {
        this.gc = gc;
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
        this.onGameOverCallback = onGameOverCallback;
        initializeGame();
    }

    /**
     * Khởi tạo trạng thái game (chim, pipes list, score, background index, v.v.)
     */
    private void initializeGame() {
        // Reset tốc độ ống về giá trị ban đầu
        Pipe.SPEED = 1.5;

        bird = new Bird(sceneWidth * 0.2, sceneHeight * 0.5, sceneHeight);
        pipes = new ArrayList<>();
        pipeScored = new ArrayList<>();

        score = 0;
        gameOver = false;
        gameStarted = false;
        lastPipeTime = 0;
        newHighScore = false;

        currentBackgroundIndex = 0;
        pipesPassedAtLastBackgroundChange = 0;

        // ---------- KHỞI TẠO cho trap pipe (LCG) ----------
        createdPipeCount = 0;
        currentTrapBlock = 0;

        // Chọn ngẫu nhiên trạng thái ban đầu của LCG bằng cách lấy một long >= 0:
        long rawSeed = random.nextLong();
        if (rawSeed < 0) rawSeed = -rawSeed;
        lcgState = rawSeed % LCG_M;  // LCG_M = 1<<31

        // Tính trapPipeIndex trong block 0 (ống 1..30):
        trapPipeIndex = (int)(lcgState % 30) + 1;
        System.out.println("Trap đầu tiên sẽ ở pipe thứ: " + trapPipeIndex);
        // ---------- KẾT THÚC khởi tạo trap pipe ----------

        // ---------- KHỞI TẠO cho HighScore pipe ----------
        int currentHS = HighScoreManager.getCurrentHighScore();
        if (currentHS > 0) {
            highScorePipeIndex = currentHS;
        } else {
            highScorePipeIndex = -1;
        }
        System.out.println("HighScore pipe sẽ là ống thứ: " + highScorePipeIndex);
    }


    @Override
    public void handle(long now) {
        // 1) Màn hình chờ nếu chưa bắt đầu
        if (!gameStarted) {
            clearScreen();
            drawStartScreen();
            drawBackground();
            bird.render(gc);
            drawStartInstructions();
            drawHighScore();
            return;
        }

        // 2) Màn hình “Game Over”
        if (gameOver) {
            clearScreen();
            renderGame();      // vẽ lại khung cảnh cuối cùng
            drawGameOverOverlay();
            return;
        }

        // 3) Nếu game đang chạy (started && !gameOver): update + render
        clearScreen();
        updateGame(now);
        renderGame();
        checkCollisions();
    }

    /**
     * Cập nhật logic game (chim, ống, tạo ống mới, tính điểm, trap pipe, đánh dấu HighScore pipe, thay background)
     */
    private void updateGame(long now) {
        bird.update();

        // Tạo ống mới dựa vào vị trí ống cuối (không dùng PIPE_INTERVAL nữa)
        if (pipes.isEmpty()) {
            Pipe newPipe = new Pipe(sceneWidth, sceneHeight);

            // Tăng số ống đã tạo
            createdPipeCount++;

            // ---------- Đánh dấu trap pipe nếu đến đúng index ----------
            if (createdPipeCount == trapPipeIndex) {
                newPipe.setTrap(true);
                System.out.println("Tạo trap pipe tại thứ tự: " + createdPipeCount);

                // Sau khi đánh dấu trap, update LCG và tính trapPipeIndex cho block kế tiếp
                currentTrapBlock++;
                lcgState = (LCG_A * lcgState + LCG_C) % LCG_M;
                trapPipeIndex = currentTrapBlock * 30 + (int)(lcgState % 30) + 1;
                System.out.println("Trap kế tiếp sẽ ở pipe thứ: " + trapPipeIndex);
            }

            // ---------- Đánh dấu HighScore pipe nếu đến đúng index ----------
            if (createdPipeCount == highScorePipeIndex) {
                newPipe.setHighScorePipe(true);
                System.out.println("Tạo HighScore pipe (đỏ) tại thứ tự: " + createdPipeCount);
            }

            pipes.add(newPipe);
            pipeScored.add(false);

        } else {
            Pipe lastPipe = pipes.get(pipes.size() - 1);
            // Khi ống cuối đã di chuyển đủ xa, spawn ống mới
            if (lastPipe.getX() < sceneWidth - 325) { // khoảng cách giữa 2 ống – có thể tùy chỉnh
                Pipe newPipe = new Pipe(sceneWidth, sceneHeight);

                // Tăng số ống đã tạo
                createdPipeCount++;

                // ---------- Đánh dấu trap pipe nếu đến đúng index ----------
                if (createdPipeCount == trapPipeIndex) {
                    newPipe.setTrap(true);
                    System.out.println("Tạo trap pipe tại thứ tự: " + createdPipeCount);

                    // Sau khi đánh dấu trap, update LCG và tính trapPipeIndex cho block kế tiếp
                    currentTrapBlock++;
                    lcgState = (LCG_A * lcgState + LCG_C) % LCG_M;
                    trapPipeIndex = currentTrapBlock * 30 + (int)(lcgState % 30) + 1;
                    System.out.println("Trap kế tiếp sẽ ở pipe thứ: " + trapPipeIndex);
                }

                // ---------- Đánh dấu HighScore pipe nếu đến đúng index ----------
                if (createdPipeCount == highScorePipeIndex) {
                    newPipe.setHighScorePipe(true);
                    System.out.println("Tạo HighScore pipe (đỏ) tại thứ tự: " + createdPipeCount);
                }

                pipes.add(newPipe);
                pipeScored.add(false);
            }
        }

        // Cập nhật từng ống, tính điểm, xóa ống ra khỏi màn hình
        for (int i = pipes.size() - 1; i >= 0; i--) {
            Pipe pipe = pipes.get(i);
            pipe.update();

            // Nếu là trap pipe và chim đang nằm giữa khe, thì co khe
            if (pipe.isTrap()) {
                double birdX = bird.getX();
                if (birdX > pipe.getX() && birdX < pipe.getX() + Pipe.WIDTH) {
                    pipe.shrinkGap();
                }
            }

            // Nếu ống đi ra khỏi màn hình, xóa nó
            if (pipe.isOffScreen()) {
                pipes.remove(i);
                pipeScored.remove(i);
            }
            // Nếu chim đã vượt qua ống mà chưa tính điểm, cộng điểm
            else if (i < pipeScored.size() && !pipeScored.get(i) && pipe.isPassed(bird.getX())) {
                score++;
                pipeScored.set(i, true);
                ResourceManager.playPointSound();

                // Cập nhật background và tăng tốc độ nếu đủ số ống đã qua
                updateBackgroundIndexAndSpeed();
                System.out.println("Điểm: " + score + " - Background: " + (currentBackgroundIndex + 1) +
                        " - SPEED=" + Pipe.SPEED);
            }
        }
    }

    /**
     * Khi chim vượt thêm mỗi 30 ống, sẽ chuyển sang background kế tiếp
     * và tăng tốc độ di chuyển của ống.
     */
    private void updateBackgroundIndexAndSpeed() {
        int passedSinceLastChange = score - pipesPassedAtLastBackgroundChange;
        if (passedSinceLastChange >= 30 && currentBackgroundIndex < ResourceManager.getBackgroundCount() - 1) {
            // Chuyển background
            currentBackgroundIndex++;
            // Ghi lại điểm khi đổi background để tính ống tiếp theo
            pipesPassedAtLastBackgroundChange = score;
            // Tăng tốc độ ống mỗi lần đổi background (ví dụ: +0.75)
            Pipe.SPEED += 0.75;
        }
    }

    /**
     * Kiểm tra va chạm: chim chạm sàn/trần hoặc chạm ống → Game Over.
     */
    private void checkCollisions() {
        // Chạm sàn hoặc trần
        if (bird.hitGroundOrRoof()) {
            triggerGameOver();
            return;
        }
        // Chạm ống
        for (Pipe pipe : pipes) {
            if (bird.collidesWith(pipe)) {
                triggerGameOver();
                return;
            }
        }
    }

    /**
     * Khi Game Over: tạm dừng, phát sound, cập nhật highscore, gọi callback (nếu có).
     */
    private void triggerGameOver() {
        if (!gameOver) {
            gameOver = true;
            ResourceManager.playGameOverSound();
            newHighScore = HighScoreManager.updateHighScore(score);
            HighScoreManager.onGameOver(score);
            System.out.println("Game Over! Điểm: " + score);
            if (newHighScore) {
                System.out.println("Kỷ lục mới!");
            }
            // Gọi callback để Main xử lý (hiển thị overlay)
            if (onGameOverCallback != null) {
                onGameOverCallback.run();
            }
        }
    }

    /**
     * Vẽ background (hình hoặc gradient fallback).
     */
    private void drawBackground() {
        if (ResourceManager.getBackgroundCount() > 0) {
            gc.drawImage(
                    ResourceManager.getBackgroundImage(currentBackgroundIndex),
                    0, 0, sceneWidth, sceneHeight
            );
        } else {
            // Fallback: gradient bầu trời + cỏ
            gc.setFill(Color.LIGHTBLUE);
            gc.fillRect(0, 0, sceneWidth, sceneHeight * 0.7);
            gc.setFill(Color.LIGHTGREEN);
            gc.fillRect(0, sceneHeight * 0.7, sceneWidth, sceneHeight * 0.3);
        }
    }

    /** Vẽ chim + pipes + điểm + highscore lên canvas. */
    private void renderGame() {
        drawBackground();

        // Vẽ tất cả ống (Pipe#render tự tự động chọn ảnh thường hoặc ảnh đỏ)
        for (Pipe pipe : pipes) {
            pipe.render(gc);
        }
        // Vẽ chim
        bird.render(gc);
        // Vẽ điểm
        drawScore();
        // Vẽ highscore
        drawHighScore();
    }

    /** Vẽ con số điểm ở giữa trên. */
    private void drawScore() {
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        String txt = String.valueOf(score);
        gc.strokeText(txt, sceneWidth / 2, 60);
        gc.fillText(txt, sceneWidth / 2, 60);
    }

    /** Vẽ highscore ở góc trên phải. */
    private void drawHighScore() {
        gc.setFill(Color.GOLD);
        gc.setStroke(Color.DARKORANGE);
        gc.setLineWidth(1);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        String hs = "High: " + HighScoreManager.getCurrentHighScore();
        gc.strokeText(hs, sceneWidth - 20, 30);
        gc.fillText(hs, sceneWidth - 20, 30);

        // Hiển thị previous nếu > 0
        int prev = HighScoreManager.getPreviousHighScore();
        if (prev > 0) {
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            String prevTxt = "Prev: " + prev;
            gc.strokeText(prevTxt, sceneWidth - 20, 50);
            gc.fillText(prevTxt, sceneWidth - 20, 50);
        }
    }

    /**
     * Vẽ màn hình chờ (Start Screen).
     */
    private void drawStartScreen() {
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        gc.strokeText("FLAPPY BIRD", sceneWidth / 2, sceneHeight / 2 - 100);
        gc.fillText("FLAPPY BIRD", sceneWidth / 2, sceneHeight / 2 - 100);
    }

    /**
     * Vẽ hướng dẫn ở màn hình chờ.
     */
    private void drawStartInstructions() {
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        gc.strokeText("Nhấn SPACE hoặc click chuột để bắt đầu", sceneWidth / 2, sceneHeight / 2 + 50);
        gc.fillText("Nhấn SPACE hoặc click chuột để bắt đầu", sceneWidth / 2, sceneHeight / 2 + 50);
        gc.strokeText("SPACE/Click để nhảy", sceneWidth / 2, sceneHeight / 2 + 80);
        gc.fillText("SPACE/Click để nhảy", sceneWidth / 2, sceneHeight / 2 + 80);
    }

    /**
     * Vẽ overlay "Game Over" (chỉ chữ & điểm, đã đưa lên cao để không đè lên hai nút).
     */
    private void drawGameOverOverlay() {
        // Không vẽ nền mờ (blur)
        // Chỉ vẽ "GAME OVER" và điểm số ở vị trí cao hơn

        // --- Vẽ chữ "GAME OVER" ---
        gc.setFill(Color.RED);
        gc.setStroke(Color.DARKRED);
        gc.setLineWidth(2);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        // Dời lên cao (sceneHeight/2 - 100) để hai nút không chồng lên
        gc.strokeText("GAME OVER", sceneWidth / 2, sceneHeight / 2 - 100);
        gc.fillText("GAME OVER", sceneWidth / 2, sceneHeight / 2 - 100);

        // --- Vẽ điểm hiện tại ngay dưới "GAME OVER" ---
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        String scoreText = "Điểm: " + score;
        gc.strokeText(scoreText, sceneWidth / 2, sceneHeight / 2 - 60);
        gc.fillText(scoreText, sceneWidth / 2, sceneHeight / 2 - 60);

        // --- Nếu có kỷ lục mới, vẽ thông báo ở giữa ---
        if (newHighScore) {
            gc.setFill(Color.GOLD);
            gc.setStroke(Color.DARKORANGE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            String trophy = "🏆 KỶ LỤC MỚI! 🏆";
            gc.strokeText(trophy, sceneWidth / 2, sceneHeight / 2 - 30);
            gc.fillText(trophy, sceneWidth / 2, sceneHeight / 2 - 30);
        }

        // Bỏ phần vẽ "Chọn CHƠI LẠI hoặc THOÁT"
        // (Để cho hai nút bên Main.java tự xuất hiện ở đáy mà không bị chồng lên)
    }

    /**
     * Xóa toàn bộ canvas.
     */
    private void clearScreen() {
        gc.clearRect(0, 0, sceneWidth, sceneHeight);
    }

    /**
     * Xử lý khi người chơi nhấn SPACE hoặc click (để chim flap),
     * hoặc để bắt đầu game lần đầu, hoặc play again sau game over.
     */
    public void handleInput() {
        if (!gameStarted) {
            gameStarted = true;
        } else if (gameOver) {
            // Sau khi game over, ignore ở đây (Main sẽ tái khởi GameLoop nếu cần)
        } else {
            bird.flap();
        }
    }
}
