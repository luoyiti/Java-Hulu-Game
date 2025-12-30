package com.gameengine.app;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.core.GameLogic;
import com.gameengine.core.ParticleSystem;
import com.gameengine.dialogue.DialogueConfigurator;
import com.gameengine.dialogue.DialogueManager;
import com.gameengine.dialogue.DialogueTriggerType;
import com.gameengine.game.*;
import com.gameengine.graphics.IRenderer;
import com.gameengine.level.EnemyFactory;
import com.gameengine.level.LevelManager;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;
import com.gameengine.ui.GameUIManager;

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
    private GameEngine engine;
    
    // 管理器
    private LevelManager levelManager;
    private GameUIManager uiManager;
    private DialogueConfigurator dialogueConfigurator;

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
    // 粒子效果开关（默认关闭）
    private boolean particlesEnabled = false;

    // 时间系统
    private boolean waitingReturn;
    private float waitInputTimer;
    private float freezeTimer;
    private final float inputCooldown = 0.25f;
    private final float freezeDelay = 0.20f;

    // 对话系统
    private DialogueManager dialogueManager;
    private boolean gameStartDialogueTriggered = false;
    private boolean gameOverDialogueTriggered = false;
    private int lastLevel = 0;
    private boolean isEndlessMode = false;

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
        // Only create missing dependencies so tests can inject fakes
        if (this.gameLogic == null) {
            this.gameLogic = new GameLogic(this, engine);
        }

        if (this.renderer == null) {
            this.renderer = engine.getRenderer();
        }

        // 创建相机（视口800x600，世界地图2000x1500）
        this.camera = new com.gameengine.core.Camera(
                800, 600,
                GameLogic.WORLD_WIDTH,
                GameLogic.WORLD_HEIGHT);
        this.camera.setSmoothSpeed(0.15f); // 设置平滑跟随速度

        // 创建初始游戏对象（只创建玩家，怪物在对话结束后生成）
        createHulu();

        // 初始化相机位置到玩家位置
        GameObject player = gameLogic.getPlayer();
        if (player != null) {
            TransformComponent playerTransform = player.getComponent(TransformComponent.class);
            if (playerTransform != null) {
                camera.setPosition(playerTransform.getPosition());
            }
        }

        // 初始化管理器
        if (this.levelManager == null) {
            EnemyFactory enemyFactory = new EnemyFactory(renderer, this, gameLogic);
            this.levelManager = new LevelManager(enemyFactory);
        }
        if (this.uiManager == null) {
            this.uiManager = new GameUIManager(renderer, gameLogic);
        }
        if (this.dialogueConfigurator == null) {
            this.dialogueConfigurator = new DialogueConfigurator(renderer);
        }
        if (this.dialogueManager == null && this.dialogueConfigurator != null) {
            this.dialogueManager = dialogueConfigurator.getDialogueManager();
        }

        // 初始化时间系统
        this.waitingReturn = false;
        this.waitInputTimer = 0f;
        this.freezeTimer = 0f;

        // 初始化对话系统
        dialogueConfigurator.initializeAllDialogues();
        dialogueConfigurator.triggerGameStart();
        gameStartDialogueTriggered = true;
        lastLevel = 0; // 从0开始，表示还没有完成任何关卡
    }

    // 标记是否已经生成了第一关怪物
    private boolean enemiesSpawned = false;
    // 标记当前关卡的敌人是否已经全部生成完毕（用于检测关卡完成）
    private boolean levelEnemiesActive = false;
    // 等待敌人添加到场景的计数器（等待1帧后再激活关卡完成检测）
    private int enemySpawnDelayFrames = 0;

    public float getTime() {
        return this.time;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        time += deltaTime;

        // 更新对话系统
        if (dialogueManager != null) {
            dialogueManager.update(deltaTime, engine.getInputManager());

            // 如果对话正在进行，只更新相机，暂停其他游戏逻辑
            if (dialogueManager.isDialogueActive()) {
                // 更新相机位置跟随玩家（即使在对话中也要更新）
                if (camera != null && gameLogic != null) {
                    GameObject player = gameLogic.getPlayer();
                    if (player != null) {
                        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
                        if (playerTransform != null) {
                            camera.follow(playerTransform.getPosition(), deltaTime);
                        }
                    }
                }
                return; // 跳过游戏战斗逻辑更新
            } else {
                // 对话结束后，检查是否需要生成第一关怪物
                if (!enemiesSpawned && gameStartDialogueTriggered) {
                    levelManager.spawnCurrentLevel();
                    enemiesSpawned = true;
                    enemySpawnDelayFrames = 2; // 等待2帧让敌人添加到场景
                }
            }
        }
        
        // 处理敌人生成延迟
        if (enemySpawnDelayFrames > 0) {
            enemySpawnDelayFrames--;
            if (enemySpawnDelayFrames == 0) {
                levelEnemiesActive = true; // 延迟后才激活关卡完成检测
            }
        }

        // 更新相机位置跟随玩家
        if (camera != null && gameLogic != null) {
            GameObject player = gameLogic.getPlayer();
            if (player != null) {
                TransformComponent playerTransform = player.getComponent(TransformComponent.class);
                if (playerTransform != null) {
                    camera.follow(playerTransform.getPosition(), deltaTime);
                }
            }
        }

        // 检查ESC键退回主界面
        if (engine.getInputManager().isKeyJustPressed(256)) { // GLFW_KEY_ESCAPE（Esc 键）
            // 创建主菜单场景
            if (dialogueManager != null) {
                dialogueManager.reset(); // 重置对话状态
            }
            MenuScene menuScene = new MenuScene(engine, "MainMenu");
            engine.setScene(menuScene);
        }

        // 录制按钮
        if (engine.getInputManager().isKeyJustPressed(82)) { // GLFW_KEY_ENTER（回车键）
            if (!isRecording) {
                System.out.println("开始录制游戏...");
                try {
                    this.recordingWriter = new FileWriter(
                            "recordings/recording_" + System.currentTimeMillis() + ".json", true);
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
        gameLogic.updateEnemyKingSkills(deltaTime); // 更新国王追踪火球技能
        gameLogic.updateEnemyWizardSkills(deltaTime); // 更新法师陷阱球技能
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

        // 检测游戏结束并触发失败对话
        if (gameLogic.isGameOver() && !wasGameOver && !gameOverDialogueTriggered) {
            gameOverDialogueTriggered = true;
            dialogueManager.triggerEvent(DialogueTriggerType.DEFEAT);
            
            GameObject player = gameLogic.getPlayer();

            // if (player != null) {
            //     TransformComponent transform = player.getComponent(TransformComponent.class);
            //     if (transform != null && particlesEnabled) {
            //         ParticleSystem.Config cfg = new ParticleSystem.Config();
            //         cfg.initialCount = 0;
            //         cfg.spawnRate = 9999f;
            //         cfg.opacityMultiplier = 1.0f;
            //         cfg.minRenderSize = 3.0f;
            //         cfg.burstSpeedMin = 250f;
            //         cfg.burstSpeedMax = 520f;
            //         cfg.burstLifeMin = 0.5f;
            //         cfg.burstLifeMax = 1.2f;
            //         cfg.burstSizeMin = 18f;
            //         cfg.burstSizeMax = 42f;
            //         cfg.burstR = 1.0f;
            //         cfg.burstGMin = 0.0f;
            //         cfg.burstGMax = 0.05f;
            //         cfg.burstB = 0.0f;
            //         ParticleSystem explosion = new ParticleSystem(renderer, transform.getPosition(), cfg);
            //         explosion.burst(180);
            //         if (collisionParticles != null) {
            //             collisionParticles.add(explosion);
            //         }
            //     }
            // }

            // 即使关闭粒子效果，也保留等待返回的流程
            waitingReturn = true;
            waitInputTimer = 0f;
            freezeTimer = 0f;

            if (waitingReturn) {
                waitInputTimer += deltaTime;
                freezeTimer += deltaTime;
            }
        }

        // 游戏所处的关卡 - 只有当敌人已生成且全部死亡时才触发下一关
        if (levelEnemiesActive && gameLogic.checkEnemiesDied()) {
            levelEnemiesActive = false; // 重置标志，等待下一关敌人生成
            int currentLevel = levelManager.getCurrentLevel();

            // 触发关卡完成对话（使用刚完成的关卡号）
            if (currentLevel != lastLevel) {
                // 先触发刚完成关卡的对话
                if (currentLevel <= 7) {
                    dialogueConfigurator.triggerLevelComplete(currentLevel + 1); // 触发下一关的开始对话
                }
                lastLevel = currentLevel;
            }

            // 进入下一关
            levelManager.nextLevel();
            currentLevel = levelManager.getCurrentLevel();

            // 检查是否进入无尽模式（第七关之后）
            if (currentLevel > 7) {
                if (!isEndlessMode) {
                    isEndlessMode = true;
                    dialogueConfigurator.triggerEndlessMode();
                }
                // 无尽模式：生成随机敌人组合
                levelManager.spawnEndlessLevel();
                enemySpawnDelayFrames = 2; // 等待2帧让敌人添加到场景
            } else {
                // 更新玩家形象为当前关卡对应的葫芦娃
                updatePlayerImageForLevel(currentLevel);
                // 生成下一关怪物
                levelManager.spawnCurrentLevel();
                enemySpawnDelayFrames = 2; // 等待2帧让敌人添加到场景
            }
        }

        // 游戏粒子效果（关闭时不更新）
        if (particlesEnabled) {
            updateParticles(deltaTime);
        }

        if (waitingReturn) {
            waitInputTimer += deltaTime;
            freezeTimer += deltaTime;
        }
    }

    private void updateParticles(float deltaTime) {
        if (!particlesEnabled) {
            return;
        }

        if (EnemyParticles == null || collisionParticles == null) {
            return;
        }
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
                            particles = new ParticleSystem((IRenderer) renderer, transform.getPosition(),
                                    ParticleSystem.Config.light());
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
        // 绘制背景（基于图片，根据相机位置滚动）
        if (camera != null) {
            Vector2 camPos = camera.getPosition();
            float bgOffsetX = -(camPos.x - 400) * 0.5f; // 视差滚动效果（背景移动速度减半）
            float bgOffsetY = -(camPos.y - 300) * 0.5f;

            // 绘制平铺背景以覆盖整个视口
            renderer.drawImage(
                    "resources/picture/game_scene.png",
                    bgOffsetX, bgOffsetY,
                    2000.0f, 1500.0f,
                    1.0f);
        } else {
            // 如果没有相机，使用原来的固定背景
            renderer.drawImage(
                    "resources/picture/game_scene.png",
                    0, 0,
                    800, 600,
                    1.0f);
        }

        super.render();

        // 粒子效果渲染（关闭时不渲染）
        if (particlesEnabled) {
            renderParticles();
        }

        // 使用 UIManager 渲染所有UI元素
        if (uiManager != null) {
            uiManager.renderAll(levelManager.getCurrentLevel(), isRecording, gameLogic.isGameOver());
        }

        // 渲染对话框（最后渲染，确保在最上层）
        if (dialogueManager != null) {
            dialogueManager.render();
        }
    }

    private void renderParticles() {
        if (!particlesEnabled) {
            return;
        }

        if (EnemyParticles == null || collisionParticles == null) {
            return;
        }
        if (playerParticles != null) {
            int count = playerParticles.getParticleCount();
            if (count > 0) {
                playerParticles.render(camera);
            }
        }

        for (ParticleSystem ps : EnemyParticles.values()) {
            if (ps != null && ps.getParticleCount() > 0) {
                ps.render(camera);
            }
        }

        for (ParticleSystem ps : collisionParticles) {
            if (ps != null && ps.getParticleCount() > 0) {
                ps.render(camera);
            }
        }
    }

    private void createHulu() {
        /**
         * 创建葫芦娃实体，他会被系统当作主玩家
         * 可以通过GameLogic中的规则操控他
         */
        HuluPlayer hulu = new HuluPlayer(renderer, this, "Hulu Player");
        // 第一关使用大娃形象
        hulu.setImageForLevel(1);
        addGameObject(hulu);
    }
    
    /**
     * 根据关卡更新玩家形象和技能
     * 每一关对应一个葫芦娃兄弟，并随机分配一种五行技能
     * @param level 关卡号（1-7）
     */
    private void updatePlayerImageForLevel(int level) {
        if (gameLogic != null) {
            GameObject player = gameLogic.getPlayer();
            if (player instanceof HuluPlayer) {
                HuluPlayer huluPlayer = (HuluPlayer) player;
                huluPlayer.setImageForLevel(level);
                // 每关重新初始化技能，随机分配五行技能之一
                huluPlayer.initAttackSkillJ();
            }
        }
    }

    /**
     * Overloaded constructor to allow dependency injection for testing.
     * Any injected dependency that is null will be created in initialize().
     */
    public GameScene(String name,
                     GameEngine engine,
                     GameLogic gameLogic,
                     IRenderer renderer,
                     LevelManager levelManager,
                     GameUIManager uiManager,
                     DialogueConfigurator dialogueConfigurator,
                     DialogueManager dialogueManager) {
        super(name);
        this.random = new Random();
        this.time = 0;
        this.engine = engine;
        this.isRecording = false;
        this.recordingWriter = null;

        this.gameLogic = gameLogic;
        this.renderer = renderer;
        this.levelManager = levelManager;
        this.uiManager = uiManager;
        this.dialogueConfigurator = dialogueConfigurator;
        this.dialogueManager = dialogueManager;
    }

}
