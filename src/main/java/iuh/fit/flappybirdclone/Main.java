package iuh.fit.flappybirdclone;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    // Biến lưu Canvas + GraphicsContext
    private Canvas canvas;
    private GraphicsContext gc;

    // Đối tượng gameLoop
    private GameLoop gameLoop;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("=== KHỞI ĐỘNG FLAPPY BIRD CLONE ===");

        // --- ADD ICON LOADING HERE ---
        try {
            // Try to load icon from resources folder first
            URL iconUrl = getClass().getResource("/app-icon.ico");
            if (iconUrl != null) {
                primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
                System.out.println("✓ Đã tải icon từ resources/app-icon.ico");
            } else {
                // Try to load from external resources folder
                try {
                    Image iconImage = new Image("file:resources/app-icon.ico");
                    primaryStage.getIcons().add(iconImage);
                    System.out.println("✓ Đã tải icon từ resources/app-icon.ico (external)");
                } catch (Exception ex) {
                    System.err.println("⚠ Không thể tải icon: " + ex.getMessage());
                    System.err.println("  Hãy đảm bảo file app-icon.ico tồn tại trong:");
                    System.err.println("  - src/main/resources/app-icon.ico (để đóng gói vào JAR)");
                    System.err.println("  - resources/app-icon.ico (bên ngoài JAR)");
                }
            }
        } catch (Exception ex) {
            System.err.println("⚠ Lỗi khi tải icon: " + ex.getMessage());
        }
        // --- END ICON LOADING ---

        // 1) Debug cấu trúc resources
        ResourceManager.debugResourceStructure();

        // 2) Tải tài nguyên (ảnh, âm thanh)
        System.out.println("Đang tải tài nguyên...");
        ResourceManager.loadResources();
        ResourceManager.printLoadedResources();

        // 3) Phát nhạc nền Menu (nếu bật)
        ResourceManager.playMenuMusic();

        // 4) Khởi tạo hệ thống HighScore
        HighScoreManager.initialize();
        System.out.println("Đã khởi tạo hệ thống highscore");

        // 5) Hiển thị Menu chính
        createMenuScene(primaryStage);
    }

    /**
     * Tạo màn hình Menu với background và 3 nút: Play, Settings, Quit.
     */
    private void createMenuScene(Stage primaryStage) {
        // --- 1) Load font pixel từ resources ---
        Font pixelFont = null;
        try {
            pixelFont = Font.loadFont(
                    Main.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf"),
                    18
            );
            if (pixelFont == null) {
                System.err.println("⚠ Không load được font PressStart2P-Regular.ttf. Fallback default.");
            }
        } catch (Exception ex) {
            System.err.println("⚠ Lỗi khi load font pixel: " + ex.getMessage());
        }

        // --- 2) Tải ảnh menu.png (nếu có) ---
        URL menuUrl = Main.class.getResource("/images/menu.gif");
        Image menuBackgroundImage;
        if (menuUrl == null) {
            System.err.println("⚠ Không tìm thấy resource: images/menu.png. Sử dụng màu nền thay thế.");
            menuBackgroundImage = null;
        } else {
            menuBackgroundImage = new Image(
                    menuUrl.toExternalForm(),
                    WINDOW_WIDTH, WINDOW_HEIGHT,
                    false, true
            );
        }

        // --- 3) Tạo Pane tuyệt đối để đặt background + nút ---
        Pane menuPane = new Pane();
        menuPane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        if (menuBackgroundImage != null) {
            BackgroundImage bgImage = new BackgroundImage(
                    menuBackgroundImage,
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(
                            WINDOW_WIDTH, WINDOW_HEIGHT, false, false, false, false
                    )
            );
            menuPane.setBackground(new Background(bgImage));
        } else {
            menuPane.setBackground(new Background(
                    new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)
            ));
        }

        // --- 4) Định vị 3 nút (Play / Settings / Quit) ---
        final double BUTTON_WIDTH = 170;
        final double BUTTON_HEIGHT = 50;
        final double BUTTON_X = 328;
        final double PLAY_BUTTON_Y = 350;
        final double SETTINGS_BUTTON_Y = 415;
        final double QUIT_BUTTON_Y = 480;

        // Nút Play
        Button startButton = new Button("Play");
        if (pixelFont != null) {
            startButton.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 18));
        } else {
            startButton.setFont(Font.font("PressStart2P-Regular", FontWeight.BOLD, 18));
        }
        startButton.setPrefWidth(BUTTON_WIDTH);
        startButton.setPrefHeight(BUTTON_HEIGHT);
        startButton.setLayoutX(BUTTON_X);
        startButton.setLayoutY(PLAY_BUTTON_Y);
        startButton.setOnAction(event -> {
            // Dừng nhạc nền Menu trước khi vào màn Character Selection
            ResourceManager.stopMenuMusic();
            showCharacterSelection(primaryStage);
        });

        // Nút Settings (nếu có)
        Button settingsButton = new Button("Settings");
        if (pixelFont != null) {
            settingsButton.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 18));
        } else {
            settingsButton.setFont(Font.font("PressStart2P-Regular", FontWeight.BOLD, 18));
        }
        settingsButton.setPrefWidth(BUTTON_WIDTH);
        settingsButton.setPrefHeight(BUTTON_HEIGHT);
        settingsButton.setLayoutX(BUTTON_X);
        settingsButton.setLayoutY(SETTINGS_BUTTON_Y);
        settingsButton.setOnAction(event -> showSettingsDialog(primaryStage));

        // Nút Quit
        Button exitButton = new Button("Quit");
        if (pixelFont != null) {
            exitButton.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 18));
        } else {
            exitButton.setFont(Font.font("PressStart2P-Regular", FontWeight.BOLD, 18));
        }
        exitButton.setPrefWidth(BUTTON_WIDTH);
        exitButton.setPrefHeight(BUTTON_HEIGHT);
        exitButton.setLayoutX(BUTTON_X);
        exitButton.setLayoutY(QUIT_BUTTON_Y);
        exitButton.setOnAction(event -> Platform.exit());

        // Thêm 3 nút vào menuPane
        menuPane.getChildren().addAll(startButton, settingsButton, exitButton);

        // -----------------------
        // Phần THÊM: dòng © dưới cùng với font Arial
        // -----------------------
        Label copyrightLabel = new Label("\u00A9 Phát triển bởi Nguyễn Triệu Minh");
        // Ở đây chúng ép luôn dùng Arial cỡ 12
        copyrightLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        // Chọn màu chữ cho tương phản với nền (ví dụ nền LIGHTBLUE → trắng)
        copyrightLabel.setTextFill(Color.WHITE);
        // Giãn rộng bằng chiều ngang cửa sổ và canh giữa
        copyrightLabel.setPrefWidth(WINDOW_WIDTH);
        copyrightLabel.setAlignment(Pos.CENTER);
        // Đặt sát đáy: y = WINDOW_HEIGHT - 30
        copyrightLabel.setLayoutY(WINDOW_HEIGHT - 30);
        // Thêm vào menuPane
        menuPane.getChildren().add(copyrightLabel);

        // Tạo Scene và hiển thị
        Scene menuScene = new Scene(menuPane, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Flappy Bird Clone - Menu");
        primaryStage.setScene(menuScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Hiển thị dialog Settings (On/Off Music & Effects).
     */
    private void showSettingsDialog(Stage ownerStage) {
        Stage dialog = new Stage();
        dialog.initOwner(ownerStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Settings");

        CheckBox chkMusic = new CheckBox("Âm thanh nền");
        chkMusic.setSelected(ResourceManager.isMusicEnabled());
        chkMusic.setOnAction(e -> {
            ResourceManager.setMusicEnabled(chkMusic.isSelected());
            if (!chkMusic.isSelected()) {
                ResourceManager.stopMenuMusic();
                ResourceManager.stopGameMusic();
            } else {
                // Nếu đang ở menu, cho phát lại Menu Music
                ResourceManager.playMenuMusic();
            }
        });

        CheckBox chkEffects = new CheckBox("Âm thanh hiệu ứng");
        chkEffects.setSelected(ResourceManager.isEffectsEnabled());
        chkEffects.setOnAction(e -> ResourceManager.setEffectsEnabled(chkEffects.isSelected()));

        Button closeButton = new Button("OK");
        closeButton.setOnAction(e -> dialog.close());

        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().addAll(chkMusic, chkEffects, closeButton);

        Scene dlgScene = new Scene(vbox, 300, 200);
        dialog.setScene(dlgScene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }


    /**
     * --- PHẦN MỚI: Character Selection (Full Screen) ---
     * Hiển thị 5 GIF bird1.gif → bird5.gif trong src/main/resources/images,
     * thay vì pop-up, ta đổi luôn scene của primaryStage sang màn Character Selection.
     */
    private void showCharacterSelection(Stage primaryStage) {
        // 1) Load font pixel để dùng cho tiêu đề
        Font pixelFont = null;
        try {
            pixelFont = Font.loadFont(
                    Main.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf"),
                    20
            );
            if (pixelFont == null) {
                System.err.println("⚠ Không load được font PressStart2P-Regular.ttf. Dùng fallback.");
            }
        } catch (Exception ex) {
            System.err.println("⚠ Lỗi khi load font pixel: " + ex.getMessage());
        }

        // 2) Load 5 GIF từ /images/bird1.gif … /images/bird5.gif
        String[] birdFiles = {"bird1.gif", "bird2.gif", "bird3.gif", "bird4.gif", "bird5.gif"};
        List<Image> birdGifs = new ArrayList<>();

        for (String filename : birdFiles) {
            URL url = getClass().getResource("/images/" + filename);
            if (url != null) {
                Image gif = new Image(url.toExternalForm(), 200, 200, true, true);
                birdGifs.add(gif);
            } else {
                System.err.println("⚠ Không tìm thấy: /images/" + filename);
            }
        }

        // 3) Tạo một Label tiêu đề bên trên (dùng font pixel nếu load được)
        javafx.scene.control.Label lblTitle = new javafx.scene.control.Label("Select your bird");
        if (pixelFont != null) {
            lblTitle.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 20));
        } else {
            lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        }
        lblTitle.setPadding(new Insets(10));
        lblTitle.setAlignment(Pos.CENTER);

        // 4) HBox chứa 5 Button với mỗi Button có một ImageView GIF
        //    Giảm khoảng cách giữa các nút xuống còn 10px, padding top/bottom 20px
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(20, 0, 20, 0));

        for (int i = 0; i < birdGifs.size(); i++) {
            ImageView iv = new ImageView(birdGifs.get(i));
            Button btn = new Button();
            btn.setGraphic(iv);
            btn.setStyle("-fx-background-color: transparent;");

            final Image chosen = birdGifs.get(i);
            btn.setOnAction(e -> {
                // 1) Lưu GIF vừa chọn vào ResourceManager
                ResourceManager.setBirdImage(chosen);
                // 2) Chuyển sang Gameplay
                showGameScene(primaryStage);
            });

            hbox.getChildren().add(btn);
        }

        // 5) Bố cục chính: VBox bao quanh Label + HBox
        VBox root = new VBox(15);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(lblTitle, hbox);
        root.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        // 6) Tạo Scene cho màn Character Selection và set lên primaryStage
        Scene selectionScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Select your bird");
        primaryStage.setScene(selectionScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }




    /**
     * Phương thức này gọi khi người chơi bấm “Play” sau khi đã chọn nhân vật.
     * Nó sẽ:
     *   1) Tạo Canvas + GraphicsContext,
     *   2) Tạo overlayPane chứa các nút "Tiếp tục", "Chơi lại" + "Thoát" (ẩn ban đầu),
     *      đặt ngay dưới chữ “GAME OVER” và highscore (không làm mờ nền),
     *      và sử dụng font pixel cho cả ba nút này,
     *   3) Khởi tạo GameLoop với callback riêng khi Game Over chỉ hiển thị 2 nút,
     *   4) Thêm xử lý phím ESC để tạm dừng và hiển thị 3 nút.
     */
    private void showGameScene(Stage stage) {
        // --- 1) Tạo Canvas và GraphicsContext ---
        canvas = new Canvas(WINDOW_WIDTH, WINDOW_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        // --- 1.1) Load font pixel (PressStart2P) để sử dụng cho các nút ---
        Font pixelFont = null;
        try {
            pixelFont = Font.loadFont(
                    Main.class.getResourceAsStream("/fonts/PressStart2P-Regular.ttf"),
                    24  // Kích thước 24 cho các nút “Tiếp tục” / “Chơi lại” / “Thoát”
            );
            if (pixelFont == null) {
                System.err.println("⚠ Không load được font PressStart2P-Regular.ttf trong showGameScene, dùng Arial.");
            }
        } catch (Exception ex) {
            System.err.println("⚠ Lỗi khi load font pixel: " + ex.getMessage());
        }

        // --- 2) Tạo overlayPane (ẩn) chứa các nút "Tiếp tục", "Chơi lại" + "Thoát" ---
        VBox overlayPane = new VBox(10);
        overlayPane.setAlignment(Pos.CENTER);
        overlayPane.setVisible(false);

        // Nút "Tiếp tục"
        Button btnContinue = new Button("Resume");
        if (pixelFont != null) {
            btnContinue.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 18));
        } else {
            btnContinue.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        }
        btnContinue.setPrefWidth(250);
        btnContinue.setVisible(false);
        btnContinue.setOnAction(e -> {
            overlayPane.setVisible(false);
            if (gameLoop != null) {
                gameLoop.start();
            }
            if (ResourceManager.isMusicEnabled()) {
                ResourceManager.playGameMusic();
            }
        });

        // Nút "Chơi lại"
        Button btnReplay = new Button("Play Again");
        if (pixelFont != null) {
            btnReplay.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 18));
        } else {
            btnReplay.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        }
        btnReplay.setPrefWidth(250);
        btnReplay.setOnAction(e -> {
            overlayPane.setVisible(false);
            if (gameLoop != null) {
                gameLoop.stop();
            }
            // Khởi tạo lại GameLoop, callback chỉ hiển thị 2 nút (ẩn Resume)
            gameLoop = new GameLoop(gc, WINDOW_WIDTH, WINDOW_HEIGHT, () -> {
                btnContinue.setVisible(false);
                overlayPane.setVisible(true);
            });
            gameLoop.start();
            if (ResourceManager.isMusicEnabled()) {
                ResourceManager.playGameMusic();
            }
        });

        // Nút "Thoát"
        Button btnExitToMenu = new Button("Quit");
        if (pixelFont != null) {
            btnExitToMenu.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 18));
        } else {
            btnExitToMenu.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        }
        btnExitToMenu.setPrefWidth(250);
        btnExitToMenu.setOnAction(e -> {
            if (gameLoop != null) {
                gameLoop.stop();
            }
            ResourceManager.stopGameMusic();
            if (ResourceManager.isMusicEnabled()) {
                ResourceManager.playMenuMusic();
            }
            createMenuScene(stage);
        });

        overlayPane.getChildren().addAll(btnContinue, btnReplay, btnExitToMenu);

        // --- 3) StackPane chứa Canvas + overlayPane ---
        StackPane gameRoot = new StackPane();
        gameRoot.getChildren().addAll(canvas, overlayPane);

        // Đặt overlayPane ở TOP_CENTER, rồi "kéo xuống" (sceneHeight/2 - 60)
        StackPane.setAlignment(overlayPane, Pos.TOP_CENTER);
        StackPane.setMargin(overlayPane, new Insets((WINDOW_HEIGHT / 2.0) - 60, 0, 0, 0));

        // --- 4) Tạo Scene cho Gameplay ---
        Scene gameScene = new Scene(gameRoot, WINDOW_WIDTH, WINDOW_HEIGHT);

        // --- 5) Đăng ký xử lý bàn phím & chuột ---
        gameScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                if (gameLoop != null) {
                    gameLoop.handleInput();
                }
            }
            else if (event.getCode() == KeyCode.ESCAPE) {
                // Pause (ESC) → dừng gameLoop và hiển thị overlay (3 nút)
                if (gameLoop != null) {
                    gameLoop.stop();
                }
                btnContinue.setVisible(true);
                overlayPane.setVisible(true);
                if (ResourceManager.isMusicEnabled()) {
                    ResourceManager.stopGameMusic();
                }
            }
            else if (event.getCode() == KeyCode.R) {
                System.out.println("Reloading resources...");
                ResourceManager.loadResources();
                ResourceManager.printLoadedResources();
            }
            else if (event.getCode() == KeyCode.D) {
                ResourceManager.debugResourceStructure();
            }
        });

        gameScene.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (gameLoop != null) {
                    gameLoop.handleInput();
                }
            }
        });

        // --- 6) Hiển thị Stage ---
        stage.setTitle("Flappy Bird Clone - Gameplay");
        stage.setScene(gameScene);
        stage.setResizable(false);
        stage.show();

        // --- 7) Đặt focus để nhận sự kiện ---
        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        // --- 8) Phát nhạc nền Game (nếu bật) ---
        ResourceManager.playGameMusic();

        // --- 9) Khởi tạo và start GameLoop lần đầu ---
        gameLoop = new GameLoop(gc, WINDOW_WIDTH, WINDOW_HEIGHT, () -> {
            // Callback khi Game Over (hiển thị 2 nút, ẩn Resume)
            btnContinue.setVisible(false);
            overlayPane.setVisible(true);
        });
        gameLoop.start();

        System.out.println("Game đã khởi chạy thành công!");
    }

    public static void main(String[] args) {
        System.out.println("Khởi động Flappy Bird Clone...");
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("JavaFX version: " + System.getProperty("javafx.version"));
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        ResourceManager.stopMenuMusic();
        ResourceManager.stopGameMusic();
        System.out.println("Ứng dụng đã dừng hoàn toàn");
        super.stop();
    }
}
