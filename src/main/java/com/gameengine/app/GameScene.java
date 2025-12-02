package com.gameengine.app;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.core.GameLogic;
import com.gameengine.core.ParticleSystem;
import com.gameengine.game.*;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

import java.io.FileWriter;
import java.util.*;

import com.gameengine.core.GameEngine;

/**
 * 葫芦娃游戏场景类
 * 管理游戏中的所有对象和游戏逻辑
 */
public class GameScene extends Scene {
    private IRenderer renderer;
    private Random random;
    private float time;
    private GameLogic gameLogic;
    private int level;
    private GameEngine engine;

    // 录制系统
    private boolean isRecording;

    @SuppressWarnings("unused")
    private FileWriter recordingWriter;
    private float recordingTimer = 0f;
    private static final float RECORDING_INTERVAL = 0.02f; // 每0.02秒记录一次
    private float keyTimer = 0f;

    // 粒子效果系统
    private ParticleSystem playerParticles;
    private List<ParticleSystem> collisionParticles;
    private Map<GameObject, ParticleSystem> EnemyParticles;

    // 时间系统
    private boolean waitingReturn;
    private float waitInputTimer;
    private float freezeTimer;
    private final float inputCooldown = 0.25f;
    private final float freezeDelay = 0.20f;

    public GameScene(String name, GameEngine engine) {
        super(name);
        this.random = new Random();
        this.time = 0;
        this.engine = engine;
        this.isRecording = false;
        this.recordingWriter = null;
    }

    public void setRecording(FileWriter fw) {
        this.isRecording = true;
        this.recordingWriter = fw;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.gameLogic = new GameLogic(this, engine);
        this.level = 1;
        this.renderer = engine.getRenderer();

        // 创建初始游戏对象
        createHulu();
        createEnemyWizard();
        // 初始游戏关卡
        level1();

        // 初始化粒子效果
        collisionParticles = new ArrayList<>();
        EnemyParticles = new HashMap<>();

        playerParticles = new ParticleSystem(renderer, new Vector2(renderer.getWidth() / 2.0f, renderer.getHeight() / 2.0f));
        playerParticles.setActive(true);

        // 初始化时间系统
        this.waitingReturn = false;
        this.waitInputTimer = 0f;
        this.freezeTimer = 0f;

    }

    public float getTime() {
        return this.time;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        time += deltaTime;

        // 检查ESC键退回主界面
        if (engine.getInputManager().isKeyJustPressed(256)) { // GLFW_KEY_ESCAPE（Esc 键）
            // 创建主菜单场景
            MenuScene menuScene = new MenuScene(engine, "MainMenu");
            engine.setScene(menuScene);
        }

        // 录制按钮
        if (engine.getInputManager().isKeyJustPressed(82)) { // GLFW_KEY_ENTER（回车键）
            if (!isRecording) {
                System.out.println("开始录制游戏...");
                try {
                    this.recordingWriter = new FileWriter("recordings/recording_" + System.currentTimeMillis() + ".txt", true);
                    this.isRecording = true;
                } catch (Exception e) {
                    System.err.println("无法创建录制文件: " + e.getMessage());
                    this.isRecording = false;
                    this.recordingWriter = null;
                }
            } else {
                System.out.println("结束录制游戏");
                this.isRecording = false;
                this.recordingWriter = null;
                // 手动关闭 FileWriter
                if (this.recordingWriter != null) {
                    try {
                        this.recordingWriter.flush();
                        this.recordingWriter.close();
                    } catch (Exception e) {
                        System.err.println("关闭录制文件时出错: " + e.getMessage());
                    }
                    this.recordingWriter = null;
                }
            }
        }

        // 游戏使用到的逻辑规则
        gameLogic.handlePlayerInput();
        gameLogic.updatePhysics();
        gameLogic.updateEnemyMovement(deltaTime);
        gameLogic.updateAttack(deltaTime);
        gameLogic.updateEnemyAttack(deltaTime);
        gameLogic.checkEntityAlive();

        // 记录游戏过程（每0.1秒记录一次）
        if (isRecording && recordingWriter != null) {
            recordingTimer += deltaTime;
            keyTimer += deltaTime;

            if (recordingTimer >= RECORDING_INTERVAL) {
                gameLogic.updateRecords(keyTimer, recordingWriter);
                recordingTimer = 0f;
            }
        }

        boolean wasGameOver = gameLogic.isGameOver();
        gameLogic.checkAiCollisions(deltaTime);
        
        if (gameLogic.isGameOver() && !wasGameOver) {
            GameObject player = gameLogic.getPlayer();
            if (player != null) {
                TransformComponent transform = player.getComponent(TransformComponent.class);
                if (transform != null) {
                    ParticleSystem.Config cfg = new ParticleSystem.Config();
                    cfg.initialCount = 0;
                    cfg.spawnRate = 9999f;
                    cfg.opacityMultiplier = 1.0f;
                    cfg.minRenderSize = 3.0f;
                    cfg.burstSpeedMin = 250f;
                    cfg.burstSpeedMax = 520f;
                    cfg.burstLifeMin = 0.5f;
                    cfg.burstLifeMax = 1.2f;
                    cfg.burstSizeMin = 18f;
                    cfg.burstSizeMax = 42f;
                    cfg.burstR = 1.0f;
                    cfg.burstGMin = 0.0f;
                    cfg.burstGMax = 0.05f;
                    cfg.burstB = 0.0f;
                    ParticleSystem explosion = new ParticleSystem(renderer, transform.getPosition(), cfg);
                    explosion.burst(180);
                    collisionParticles.add(explosion);
                    waitingReturn = true;
                    waitInputTimer = 0f;
                    freezeTimer = 0f;
                }
            }

            if (waitingReturn) {
                waitInputTimer += deltaTime;
                freezeTimer += deltaTime;
            }
        }

        // 游戏所处的关卡
        if (gameLogic.checkEnemiesDied()) {
            this.level++;

            if (this.level == 2) {
                level2();
            } else {
                level3();
            }
        }

        // 游戏粒子效果
        updateParticles(deltaTime);

        if (waitingReturn) {
            waitInputTimer += deltaTime;
            freezeTimer += deltaTime;
        }
    }

    private void updateParticles(float deltaTime) {
        boolean freeze = waitingReturn && freezeTimer >= freezeDelay;

        if (playerParticles != null && !freeze) {
            GameObject player = gameLogic.getPlayer();
            if (player != null) {
                TransformComponent transform = player.getComponent(TransformComponent.class);
                if (transform != null) {
                    Vector2 playerPos = transform.getPosition();
                    playerParticles.setPosition(playerPos);
                }
            }
            playerParticles.update(deltaTime);
        }

        List<GameObject> Enemies = gameLogic.getEnemies();
        if (!freeze) {
            for (GameObject Enemy : Enemies) {
                if (Enemy != null && Enemy.isActive()) {
                    ParticleSystem particles = EnemyParticles.get(Enemy);
                    if (particles == null) {
                        TransformComponent transform = Enemy.getComponent(TransformComponent.class);
                        if (transform != null) {
                            particles = new ParticleSystem((IRenderer) renderer, transform.getPosition(), ParticleSystem.Config.light());
                            particles.setActive(true);
                            EnemyParticles.put(Enemy, particles);
                        }
                    }
                    if (particles != null) {
                        TransformComponent transform = Enemy.getComponent(TransformComponent.class);
                        if (transform != null) {
                            particles.setPosition(transform.getPosition());
                        }
                        particles.update(deltaTime);
                    }
                }
            }
        }

        List<GameObject> toRemove = new ArrayList<>();
        for (Map.Entry<GameObject, ParticleSystem> entry : EnemyParticles.entrySet()) {
            if (!entry.getKey().isActive() || !Enemies.contains(entry.getKey())) {
                toRemove.add(entry.getKey());
            }
        }
        for (GameObject removed : toRemove) {
            EnemyParticles.remove(removed);
        }

        for (int i = collisionParticles.size() - 1; i >= 0; i--) {
            ParticleSystem ps = collisionParticles.get(i);
            if (ps != null) {
                if (!freeze) {
                    ps.update(deltaTime);
                }
            }
        }
    }

    @Override
    public void render() {
        // 绘制背景（基于图片）
        renderer.drawImage(
            "resources/picture/game_scene.png",
            0, 0,
            800, 600,
            1.0f
        );
        super.render();

        renderParticles();

        if (gameLogic.isGameOver()) {
            float cx = renderer.getWidth() / 2.0f;
            float cy = renderer.getHeight() / 2.0f;
            renderer.drawRect(0, 0, renderer.getWidth(), renderer.getHeight(), 0.0f, 0.0f, 0.0f, 0.35f);
            renderer.drawRect(cx - 200, cy - 60, 400, 120, 0.0f, 0.0f, 0.0f, 0.7f);
            renderer.drawText("GAME OVER", cx - 100, cy - 10, 1.0f, 1.0f, 1.0f, 1.0f, cy + 40);
            renderer.drawText("PRESS ANY KEY TO RETURN", cx - 180, cy + 30, 0.8f, 0.8f, 0.8f, 1.0f, cy + 40);
        }
        
        // 渲染level数在场景正上方
        renderLevel();
        
        // 渲染玩家血条在屏幕左上角
        renderPlayerHealthBar();
        
        // 渲染技能冷却条在屏幕右上角
        renderSkillCooldownBar();
        
        // 渲染录制按钮提示在屏幕左下角
        renderRecordingHint();
    }
    
    /**
     * 在屏幕左下角渲染录制按钮提示
     */
    private void renderRecordingHint() {
        int hintX = 10;
        int hintY = renderer.getHeight() - 40;
        
        if (isRecording) {
            // 录制中显示红色圆点和停止提示
            renderer.drawCircle(hintX + 8, hintY + 10, 6, 16, 1.0f, 0.0f, 0.0f, 1.0f);
            renderer.drawText("录制中... (R 停止)", hintX + 20, hintY + 15, 14, 1.0f, 0.3f, 0.3f, 1.0f);
        } else {
            // 未录制时显示灰色圆点和开始提示
            renderer.drawCircle(hintX + 8, hintY + 10, 6, 16, 0.5f, 0.5f, 0.5f, 1.0f);
            renderer.drawText("按 R 开始录制", hintX + 20, hintY + 15, 14, 0.7f, 0.7f, 0.7f, 1.0f);
        }
    }
    
    private void renderParticles() {
        if (playerParticles != null) {
            int count = playerParticles.getParticleCount();
            if (count > 0) {
                playerParticles.render();
            }
        }

        for (ParticleSystem ps : EnemyParticles.values()) {
            if (ps != null && ps.getParticleCount() > 0) {
                ps.render();
            }
        }

        for (ParticleSystem ps : collisionParticles) {
            if (ps != null && ps.getParticleCount() > 0) {
                ps.render();
            }
        }
    }

    /**
     * 在屏幕左上角渲染玩家血条
     */
    private void renderPlayerHealthBar() {
        // 查找玩家对象
        GameObject player = null;
        for (GameObject obj : getGameObjects()) {
            if ("Player".equals(obj.getidentity())) {
                player = obj;
                break;
            }
        }
        
        if (player != null) {
            LifeFeatureComponent lifeFeature = player.getComponent(LifeFeatureComponent.class);
            if (lifeFeature != null) {
                int currentHealth = lifeFeature.getBlood();
                int maxHealth = 100;
                
                // // 绘制半透明黑色背景框
                // renderer.drawRect(10, 10, 160, 40, 0.0f, 0.0f, 0.0f, 0.7f);
                
                // 绘制"玩家血量"标签
                renderer.drawText("玩家血量", 20, 20, 20, 1.0f, 1.0f, 1.0f, 1.0f);
                
                // 绘制血条
                renderer.drawHealthBar(20, 50, 120, 10, currentHealth, maxHealth);
                
                // 绘制血量数值
                String healthText = currentHealth + " / " + maxHealth;
                renderer.drawText(healthText, 145, 60, 12, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }
    
    /**
     * 在屏幕右上角渲染技能冷却条
     */
    private void renderSkillCooldownBar() {
        if (gameLogic != null) {
            float cooldownPercentage = gameLogic.getSkillCooldownPercentage();
            
            // 冷却条的位置和尺寸（根据地图尺寸800x600调整位置）
            int barX = 610;  // 右上角 x 坐标
            int barY = 10;   // 右上角 y 坐标
            int barWidth = 180;
            int barHeight = 40;
            
            // // 绘制半透明黑色背景框
            // renderer.drawRect(barX, barY, barWidth, barHeight, 0.0f, 0.0f, 0.0f, 0.7f);
            
            // 绘制"技能冷却"标签
            renderer.drawText("技能冷却 (J)", barX + 10, barY + 10, 20, 1.0f, 1.0f, 1.0f, 1.0f);
            
            // 绘制冷却条
            int cooldownBarWidth = 140;
            int cooldownBarHeight = 10;
            int cooldownBarX = barX + 20;
            int cooldownBarY = barY + 25;
            
            // // 绘制冷却条背景（灰色）
            // renderer.drawRect(cooldownBarX, cooldownBarY, cooldownBarWidth, cooldownBarHeight, 
            //                 0.3f, 0.3f, 0.3f, 1.0f);
            
            // 根据冷却状态绘制冷却进度条
            int filledWidth = (int)(cooldownBarWidth * cooldownPercentage);
            
            if (cooldownPercentage >= 1.0f) {
                // 冷却完成，绘制绿色条
                renderer.drawRect(cooldownBarX, cooldownBarY+10, filledWidth, cooldownBarHeight, 
                                0.0f, 1.0f, 0.0f, 1.0f);
            } else {
                // 冷却中，绘制黄色条
                renderer.drawRect(cooldownBarX, cooldownBarY+10, filledWidth, cooldownBarHeight, 
                                1.0f, 0.8f, 0.0f, 1.0f);
            }
            
            // 绘制百分比文字
            String percentText = String.format("%.0f%%", cooldownPercentage * 100);
            renderer.drawText(percentText, barX + 165, barY + 35, 12, 1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void renderLevel() {
        // 在屏幕顶部中央绘制level数
        String levelText = "Level: " + level;
        // 屏幕宽度800，文本居中，假设字体大小为20，位置大约在x=400-50=350
        renderer.drawText(levelText, 350, 30, 20, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void createHulu() {
        /**
         * 创建葫芦娃实体，他会被系统当作主玩家
         * 可以通过GameLogic中的规则操控他
         */
        HuluPlayer hulu = new HuluPlayer(renderer, this);
        addGameObject(hulu);
    }

    private void createEnemySoldiers() {
        for (int i = 0; i <= 5; i++) {
            createEnemySoldier();
        }
    }
    
    // /**
    //  * 创建带透明度和旋转的蛇形敌人示例
    //  * 
    //  * @param imagePath 图片路径
    //  * @param imageSize 图片尺寸
    //  * @param alpha 透明度 (0.0-1.0)
    //  * @param rotation 旋转角度（弧度）
    //  */
    // @SuppressWarnings("unused")
    // private void createEnemySnakeWithEffects(String imagePath, Vector2 imageSize, float alpha, float rotation) {
    //     Vector2 position = new Vector2(
    //         random.nextFloat() * 700 + 50,
    //         random.nextFloat() * 500 + 50
    //     );
        
    //     EnemySnake enemySnake = new EnemySnake(renderer, position, imagePath, imageSize, alpha, rotation, random);
    //     addGameObject(enemySnake);
    // }

    private void createEnemySoldier() {
        // 生成远离玩家的位置（优先相对于当前玩家位置）
        // 确保敌人距离玩家至少 minDistance 像素
        Vector2 position;
        int minDistance = 200; // 最小距离
        int maxAttempts = 200; // 最大尝试次数（增大重试次数以提高成功率）
        int attempts = 0;

        // 使用当前玩家位置作为参考中心（如果不存在则回退到地图中心）
        Vector2 playerCenter = new Vector2(400, 300);
        if (gameLogic != null) {
            GameObject player = gameLogic.getPlayer();
            if (player != null) {
                TransformComponent pt = player.getComponent(TransformComponent.class);
                if (pt != null) {
                    playerCenter = pt.getPosition();
                }
            }
        }

        do {
            position = new Vector2(
                random.nextFloat() * 800,
                random.nextFloat() * 600
            );
            attempts++;

            // 计算与玩家中心的距离
            float dx = position.x - playerCenter.x;
            float dy = position.y - playerCenter.y;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);

            // 如果距离足够远，或者尝试次数过多，就使用这个位置
            if (distance >= minDistance || attempts >= maxAttempts) {
                break;
            }
        } while (true);
        
        EnemySoldier enemySoldier = new EnemySoldier(renderer, position, "resources/picture/bee.png", new Vector2(40, 60), random);
        addGameObject(enemySoldier);
    }

    /**
     * 创建国王怪
     */
    private void createEnemyKing() {
        // 生成远离玩家中心(400, 300)的随机位置
        // 确保敌人距离玩家至少200像素
        Vector2 position;
        int minDistance = 200; // 最小距离
        int maxAttempts = 50; // 最大尝试次数
        int attempts = 0;
        
        do {
            position = new Vector2(
                random.nextFloat() * 800,
                random.nextFloat() * 600
            );
            attempts++;
            
            // 计算与玩家中心的距离
            float dx = position.x - 400;
            float dy = position.y - 300;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            
            // 如果距离足够远，或者尝试次数过多，就使用这个位置
            if (distance >= minDistance || attempts >= maxAttempts) {
                break;
            }
        } while (true);
        
        EnemyKing enemyKing = new EnemyKing(renderer, this, position, "resources/picture/snake_queen.png", new Vector2(80, 80), random);
        addGameObject(enemyKing);
    }

    /**
     * 创建法师怪
     */
    private void createEnemyWizard() {
        // 生成远离玩家的位置（优先相对于当前玩家位置）
        // 确保敌人距离玩家至少 minDistance 像素
        Vector2 position;
        int minDistance = 200; // 最小距离
        int maxAttempts = 50; // 最大尝试次数
        int attempts = 0;

        // 使用当前玩家位置作为参考中心（如果不存在则回退到地图中心）
        Vector2 playerCenter = new Vector2(400, 300);
        if (gameLogic != null) {
            GameObject player = gameLogic.getPlayer();
            if (player != null) {
                TransformComponent pt = player.getComponent(TransformComponent.class);
                if (pt != null) {
                    playerCenter = pt.getPosition();
                }
            }
        }

        do {
            position = new Vector2(
                random.nextFloat() * 800,
                random.nextFloat() * 600
            );
            attempts++;

            // 计算与玩家中心的距离
            float dx = position.x - playerCenter.x;
            float dy = position.y - playerCenter.y;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);

            // 如果距离足够远，或者尝试次数过多，就使用这个位置
            if (distance >= minDistance || attempts >= maxAttempts) {
                break;
            }
        } while (true);
        
        EnemyWizard enemyWizard = new EnemyWizard(renderer, position, "resources/picture/wizard.png", new Vector2(50, 70), random);
        addGameObject(enemyWizard);
    }

    // private void createTree() {
    //     Vector2 position = new Vector2(
    //         random.nextFloat() * 800,
    //         random.nextFloat() * 600
    //     );
        
    //     Tree tree = new Tree(renderer, position);
    //     addGameObject(tree);
    // }

    // private void createTrees() {
    //     for (int i = 0; i < 3; i++) {
    //         createTree();
    //     }
    // }

    public void level1() {
        createEnemySoldiers();
        // createEnemySnake("resources/picture/snake_queen.png", new Vector2(40, 40));
    }

    public void level2() {
        createEnemyKing();
    }

    public void level3() {
        createEnemySoldiers();
        createEnemyKing();
    }

}
