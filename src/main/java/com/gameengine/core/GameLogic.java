package com.gameengine.core;

import com.gameengine.components.TransformComponent;
import com.gameengine.components.LifeFeatureComponent;
import com.gameengine.components.PhysicsComponent;
import com.gameengine.components.RenderComponent;
import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;
import com.gameengine.game.Record;
import com.gameengine.game.EnemyKing;
import com.gameengine.game.EnemySoldier;
import com.gameengine.game.EnemyWizard;
import com.gameengine.game.HuluPlayer;
import com.gameengine.graphics.IRenderer;
import com.gameengine.core.ParticleSystem;
import com.google.gson.Gson;

import java.util.List;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

// 并行处理类导入
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 游戏逻辑类，处理具体的游戏规则
 */
public class GameLogic implements AutoCloseable {
    // 世界地图尺寸常量
    public static final float WORLD_WIDTH = 2000f;
    public static final float WORLD_HEIGHT = 1500f;

    private Scene scene;
    private GameEngine engine;
    private InputManager inputManager;
    private Random random;
    private float enemyMoveTimer;
    private float enemyMoveInterval = 0.3f;
    private float EnemyAttackLifeTimer; // 攻击技能存活时间计时器
    private float EnemyAttackLifeDuration = 1.0f; // 攻击技能存活时间
    private float playerSkillCooldownTimer; // 玩家技能冷却计时器
    private float playerSkillCooldownDuration = 0.5f; // 玩家技能冷却时间（0.5秒）
    private float enemySkillCooldownTimer;
    private float enemySkillCooldownDuration = 2.0f;

    private boolean gameOver;

    // Multiplayer 相关字段
    private HuluPlayer[] multiplayerPlayers;
    private float[] playerSkillCooldownTimers;
    private static final float SKILL_COOLDOWN_DURATION = 0.5f;
    private IRenderer renderer;
    private Map<GameObject, ParticleSystem> playerParticles;
    private List<ParticleSystem> collisionParticles;
    private boolean multiplayerGameOver = false;
    private int winnerIndex = -1;

    // 多线程通用组件
    private final int PARALLEL_THRESHOLD = 20; // 组件数量超过此值才启用并行
    private final int threadCount;

    // 物理更新多线程赋值
    private ExecutorService physicsExecutor;
    // 碰撞检测多线程赋值
    private ExecutorService collisionExecutor;

    // 用于记录的组件
    private Gson gson;

    public GameLogic(Scene scene, GameEngine engine) {
        this.scene = scene;
        this.engine = engine;
        this.inputManager = InputManager.getInstance();
        this.random = new Random();
        this.enemyMoveTimer = 0;
        this.EnemyAttackLifeTimer = 0;
        this.playerSkillCooldownTimer = 0.5f; // 初始化为冷却完成状态
        this.enemySkillCooldownTimer = 0.5f;
        this.gameOver = false;
        this.gson = new Gson();

        // 多线程池通用threadCount赋值
        this.threadCount = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
        // 为避免重复创建线程池，在此初始化
        // 初始化物理更新多线程池
        physicsExecutor = Executors.newFixedThreadPool(threadCount);
        System.out.println("物理更新线程池已经被初始化完成");
        // 初始化碰撞检测多线程池
        this.collisionExecutor = Executors.newFixedThreadPool(threadCount);
        System.out.println("碰撞检测线程池已经被初始化完成");
    }

    /**
     * 关闭 GameLogic 持有的线程池资源（便于测试与正常退出）。
     */
    @Override
    public void close() {
        shutdownExecutor(physicsExecutor);
        shutdownExecutor(collisionExecutor);
    }

    private static void shutdownExecutor(ExecutorService executor) {
        if (executor == null) {
            return;
        }
        executor.shutdownNow();
    }

    /**
     * 初始化多人游戏模式
     */
    public void initializeMultiplayer(HuluPlayer[] players, IRenderer renderer) {
        this.multiplayerPlayers = players;
        this.renderer = renderer;
        this.playerSkillCooldownTimers = new float[players.length];

        // 初始化所有玩家的技能冷却为已完成状态
        for (int i = 0; i < players.length; i++) {
            playerSkillCooldownTimers[i] = SKILL_COOLDOWN_DURATION;
        }

        // 初始化粒子效果容器
        this.playerParticles = new HashMap<>();
        this.collisionParticles = new ArrayList<>();

        // 注释掉玩家粒子效果初始化，移除葫芦娃的粒子效果
        // initializeMultiplayerParticles();
    }

    /**
     * 初始化多人游戏粒子效果
     */
    private void initializeMultiplayerParticles() {
        for (int i = 0; i < multiplayerPlayers.length; i++) {
            if (multiplayerPlayers[i] != null) {
                TransformComponent transform = multiplayerPlayers[i].getComponent(TransformComponent.class);
                if (transform != null) {
                    ParticleSystem particles = new ParticleSystem(renderer, transform.getPosition());
                    particles.setActive(true);
                    playerParticles.put(multiplayerPlayers[i], particles);
                }
            }
        }
    }

    /**
     * 返回当前游戏状态
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * 获取当前游戏玩家
     */
    public GameObject getPlayer() {
        for (GameObject obj : scene.getGameObjects()) {
            if (obj.getidentity().equals("Player") && obj.hasComponent(PhysicsComponent.class)) {
                return obj;
            }
        }
        return null;
    }

    /**
     * 获取所有玩家
     * 适用于多人游戏
     */
    public List<GameObject> getPlayers() {
        return scene.getGameObjects().stream()
                .filter(obj -> obj.getidentity().equals("Player"))
                .filter(obj -> obj.isActive())
                .collect(Collectors.toList());
    }

    /**
     * 获取当前所有怪物
     */
    public List<GameObject> getEnemies() {
        return scene.getGameObjects().stream()
                .filter(obj -> obj.getidentity().equals("Enemy"))
                .filter(obj -> obj.isActive())
                .collect(Collectors.toList());
    }

    /**
     * 获取所有技能对象
     */
    public List<GameObject> getSkills() {
        return scene.getGameObjects().stream()
                .filter(obj -> obj.getidentity().equals("Player Skill"))
                .filter(obj -> obj.isActive())
                .collect(Collectors.toList());
    }

    /**
     * 记录游戏对象的当前状态
     */
    public void updateRecords(float keyTimer, FileWriter recordingWriter) {

        Record records = new Record();
        records.setKey(keyTimer);
        records.setRecordType("object_move");

        // 记录玩家血量
        GameObject player = getPlayer();
        if (player != null) {
            LifeFeatureComponent lifeFeature = player.getComponent(LifeFeatureComponent.class);
            if (lifeFeature != null) {
                records.setPlayerHealth(lifeFeature.getBlood());
                records.setPlayerMaxHealth(100);
            }
        }

        // 记录技能冷却百分比
        float skillCooldown = getSkillCooldownPercentage();
        records.setSkillCooldownPercent(skillCooldown);

        for (GameObject obj : scene.getGameObjects()) {

            if (!obj.isActive())
                continue;

            if (obj.getidentity().equals("None"))
                continue; // 对于无身份的对象不记录

            records.getGameObjectsMove().add(obj.getRecords());
        }

        String record_json = gson.toJson(records);

        try {
            if (!record_json.isEmpty()) {
                recordingWriter.write(record_json);
                recordingWriter.write("\n");
            }
        } catch (Exception e) {
            System.err.println("记录游戏对象状态时出错: " + e.getMessage());
        }
    }

    /**
     * 获取录制信息
     * 用于网络服务
     */
    public Record getRecord(float keyTimer) {
        Record records = new Record();
        records.setKey(keyTimer);
        records.setRecordType("object_move");

        // 记录玩家血量
        GameObject player = getPlayer();
        if (player != null) {
            LifeFeatureComponent lifeFeature = player.getComponent(LifeFeatureComponent.class);
            if (lifeFeature != null) {
                records.setPlayerHealth(lifeFeature.getBlood());
                records.setPlayerMaxHealth(100);
            }
        }

        // 记录技能冷却百分比
        float skillCooldown = getSkillCooldownPercentage();
        records.setSkillCooldownPercent(skillCooldown);

        for (GameObject obj : scene.getGameObjects()) {

            if (!obj.isActive())
                continue;

            if (obj.getidentity().equals("None"))
                continue; // 对于无身份的对象不记录

            records.getGameObjectsMove().add(obj.getRecords());
        }

        return records;
    }

    /**
     * 处理玩家输入
     */
    public void handlePlayerInput() {

        if (gameOver)
            return;

        GameObject player = getPlayer();
        TransformComponent transform = player.getComponent(TransformComponent.class);
        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);

        if (transform == null || physics == null)
            return;

        Vector2 movement = new Vector2();

        if (inputManager.isKeyPressed(87) || inputManager.isKeyPressed(38)) { // W或上箭头
            movement.y -= 1;
        }
        if (inputManager.isKeyPressed(83) || inputManager.isKeyPressed(40)) { // S或下箭头
            movement.y += 1;
        }
        if (inputManager.isKeyPressed(65) || inputManager.isKeyPressed(37)) { // A或左箭头
            movement.x -= 1;
        }
        if (inputManager.isKeyPressed(68) || inputManager.isKeyPressed(39)) { // D或右箭头
            movement.x += 1;
        }

        if (inputManager.isKeyPressed(74)) { // J按键

            handlePlayerAttackJ();

        }

        if (movement.magnitude() > 0) {
            movement = movement.normalize().multiply(200);
            physics.setVelocity(movement);
        }

        // 边界检查（使用世界地图尺寸）
        Vector2 pos = transform.getPosition();
        if (pos.x < 0)
            pos.x = 0;
        if (pos.y < 0)
            pos.y = 0;
        if (pos.x > WORLD_WIDTH - 20)
            pos.x = WORLD_WIDTH - 20;
        if (pos.y > WORLD_HEIGHT - 20)
            pos.y = WORLD_HEIGHT - 20;
        transform.setPosition(pos);
    }

    /**
     * 处理多人玩家输入
     */

    public void handleMultiplayerInput() {

        List<GameObject> players = getPlayers();

        if (gameOver)
            return;

        if (players.size() == 0) {
            return;
        } else if (players.size() > 2) {
            System.err
                    .println("当前玩家数量过多，无法处理输入，最多支持2个玩家同时游戏，当前玩家数量：" + players.size());
            return;
        } else if (players.size() == 1) {
            // 只有一个玩家，处理单人输入
            handlePlayerInput();
            return;
        }
        // 处理两个玩家的输入
        // 玩家1使用WASD控制，玩家2使用箭头键控制
        GameObject player1 = players.get(0);
        TransformComponent transform = player1.getComponent(TransformComponent.class);
        PhysicsComponent physics = player1.getComponent(PhysicsComponent.class);
        if (transform == null || physics == null)
            return;
        Vector2 movement = new Vector2();
        if (inputManager.isKeyPressed(87) || inputManager.isKeyPressed(38)) { // W或上箭头
            movement.y -= 1;
        }
        if (inputManager.isKeyPressed(83) || inputManager.isKeyPressed(40)) { // S或下箭头
            movement.y += 1;
        }
        if (inputManager.isKeyPressed(65) || inputManager.isKeyPressed(37)) { // A或左箭头
            movement.x -= 1;
        }
        if (inputManager.isKeyPressed(68) || inputManager.isKeyPressed(39)) { // D或右箭头
            movement.x += 1;
        }
        if (inputManager.isKeyPressed(74)) { // J按键
            handlePlayerAttackJ();
        }
        if (movement.magnitude() > 0) {
            movement = movement.normalize().multiply(200);
            physics.setVelocity(movement);
        }
        // 边界检查（使用世界地图尺寸）
        Vector2 pos = transform.getPosition();
        if (pos.x < 0)
            pos.x = 0;
        if (pos.y < 0)
            pos.y = 0;
        if (pos.x > WORLD_WIDTH - 20)
            pos.x = WORLD_WIDTH - 20;
        if (pos.y > WORLD_HEIGHT - 20)
            pos.y = WORLD_HEIGHT - 20;
        transform.setPosition(pos);
        // 玩家2
        GameObject player2 = players.get(1);
        TransformComponent transform2 = player2.getComponent(TransformComponent.class);
        PhysicsComponent physics2 = player2.getComponent(PhysicsComponent.class);
        if (transform2 == null || physics2 == null)
            return;
        Vector2 movement2 = new Vector2();
        if (inputManager.isKeyPressed(38) || inputManager.isKeyPressed(265)) { // 上箭头
            movement2.y -= 1;
        }
        if (inputManager.isKeyPressed(40) || inputManager.isKeyPressed(264)) { // 下箭头
            movement2.y += 1;
        }
        if (inputManager.isKeyPressed(37) || inputManager.isKeyPressed(263)) { // 左箭头
            movement2.x -= 1;
        }
        if (inputManager.isKeyPressed(39) || inputManager.isKeyPressed(262)) { // 右箭头
            movement2.x += 1;
        }
        if (movement2.magnitude() > 0) {
            movement2 = movement2.normalize().multiply(200);
            physics2.setVelocity(movement2);
        }
        // 边界检查（使用世界地图尺寸）
        Vector2 pos2 = transform2.getPosition();
        if (pos2.x < 0)
            pos2.x = 0;
        if (pos2.y < 0)
            pos2.y = 0;
        if (pos2.x > WORLD_WIDTH - 20)
            pos2.x = WORLD_WIDTH - 20;
        if (pos2.y > WORLD_HEIGHT - 20)
            pos2.y = WORLD_HEIGHT - 20;
        transform2.setPosition(pos2);
    }

    /**
     * 更新物理系统
     * 由组件数量决定并串行方式
     * 若组件过少，则串行，反之并行
     */
    public void updatePhysics() {
        List<PhysicsComponent> physicsComponents = scene.getComponents(PhysicsComponent.class);
        if (physicsComponents.isEmpty())
            return;

        // 根据组件数量决定串行还是并行
        if (physicsComponents.size() < PARALLEL_THRESHOLD) {
            updatePhysicsSerial(physicsComponents);
        } else {
            updatePhysicsParallel(physicsComponents);
        }

    }

    /**
     * 并行处理物理系统
     * 
     */
    private void updatePhysicsParallel(List<PhysicsComponent> physicsComponents) {

        // 初始化物理更新线程池（若为空或已关闭则重新创建）
        if (physicsExecutor == null || physicsExecutor.isShutdown() || physicsExecutor.isTerminated()) {
            physicsExecutor = Executors.newFixedThreadPool(threadCount);
            System.out.println("物理更新线程池已经被初始化完成");
        }

        // 分批次处理优化
        int batchSize = Math.max(1, physicsComponents.size() / threadCount + 1);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < physicsComponents.size(); i += batchSize) {
            final int start = i;
            final int end = Math.min(i + batchSize, physicsComponents.size());

            Future<?> future = physicsExecutor.submit(() -> {
                for (int j = start; j < end; j++) {
                    PhysicsComponent physics = physicsComponents.get(j);
                    updateSinglePhysics(physics);
                }
            });

            futures.add(future);
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 串行更新物理组件
     */
    private void updatePhysicsSerial(List<PhysicsComponent> physicsComponents) {
        for (PhysicsComponent physicsComponent : physicsComponents) {
            updateSinglePhysics(physicsComponent);
        }
    }

    /**
     * 对单个物理部件进行更新
     * 是决定具体更新方式的关键
     */
    private void updateSinglePhysics(PhysicsComponent physics) {
        TransformComponent transform = physics.getOwner().getComponent(TransformComponent.class);
        if (transform != null) {
            Vector2 pos = transform.getPosition();
            Vector2 velocity = physics.getVelocity();

            boolean velocityChanged = false;

            // 使用世界地图尺寸进行边界反弹
            if (pos.x <= 0 || pos.x >= WORLD_WIDTH - 15) {
                velocity.x = -velocity.x;
                velocityChanged = true;
            }
            if (pos.y <= 0 || pos.y >= WORLD_HEIGHT - 15) {
                velocity.y = -velocity.y;
                velocityChanged = true;
            }

            if (pos.x < 0)
                pos.x = 0;
            if (pos.y < 0)
                pos.y = 0;
            if (pos.x > WORLD_WIDTH - 15)
                pos.x = WORLD_WIDTH - 15;
            if (pos.y > WORLD_HEIGHT - 15)
                pos.y = WORLD_HEIGHT - 15;

            transform.setPosition(pos);

            if (velocityChanged) {
                physics.setVelocity(velocity);
            }
        }
    }

    /**
     * 对对象的碰撞进行检测
     * 
     * @param deltaTime
     */
    public void checkAiCollisions(float deltaTime) {

        if (gameOver)
            return;

        List<GameObject> players = getPlayers();
        List<GameObject> enemies = getEnemies();
        List<GameObject> playerSkills = getSkills();

        // 如果没有需要检测的对象，直接返回
        if (players.isEmpty() && enemies.isEmpty()) {
            return;
        }
        // 根据对象多少决定并行或串行的方式
        int totalCollisions = players.size() + enemies.size();
        if (totalCollisions < PARALLEL_THRESHOLD) {
            checkAiCollisionsSerial(players, enemies, playerSkills);
        } else {
            checkAiCollisionsParallel(players, enemies, playerSkills);
        }

    }

    /**
     * 串行处理组件碰撞
     * 
     */
    public void checkAiCollisionsSerial(List<GameObject> players,
            List<GameObject> enemies,
            List<GameObject> playerSkills) {
        // 检查玩家与敌人/敌人技能的碰撞
        for (GameObject player : players) {
            checkPlayerCollisions(player, enemies);
        }

        // 检查敌人与玩家技能的碰撞
        for (GameObject enemy : enemies) {
            checkEnemyCollisions(enemy, playerSkills);
        }
    }

    /**
     * 并行处理组件碰撞
     */
    private void checkAiCollisionsParallel(List<GameObject> players,
            List<GameObject> enemies,
            List<GameObject> playerSkills) {
        List<Future<?>> futures = new ArrayList<>();

        // 防止未初始化成果，这里检测一次
        if (collisionExecutor == null || collisionExecutor.isShutdown() || collisionExecutor.isTerminated()) {
            collisionExecutor = Executors.newFixedThreadPool(threadCount);
            System.out.println("碰撞检测线程池已经被初始化完成");
        }

        // 批量处理玩家碰撞
        int playerBatchSize = Math.max(1, players.size() / threadCount);
        for (int i = 0; i < players.size(); i += playerBatchSize) {
            final int start = i;
            final int end = Math.min(i + playerBatchSize, players.size());

            Future<?> future = collisionExecutor.submit(() -> {
                try {
                    for (int j = start; j < end; j++) {
                        GameObject player = players.get(j);
                        checkPlayerCollisions(player, enemies);
                    }
                } catch (Exception e) {
                    System.err.println("碰撞检测中出现错误" + e.getMessage());
                }
            });
            futures.add(future);
        }

        // 批量处理敌人碰撞
        int enemyBatchSize = Math.max(1, enemies.size() / threadCount);
        for (int i = 0; i < enemies.size(); i += enemyBatchSize) {
            final int start = i;
            final int end = Math.min(i + enemyBatchSize, enemies.size());

            Future<?> future = collisionExecutor.submit(() -> {
                try {
                    for (int j = start; j < end; j++) {
                        GameObject enemy = enemies.get(j);
                        checkEnemyCollisions(enemy, playerSkills);
                    }
                } catch (Exception e) {
                    System.err.println("碰撞检测中出现错误" + e.getMessage());
                }
            });
            futures.add(future);
        }
        // 等待所有任务完成
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                System.err.println("等待碰撞检测完成时出现错误" + e.getMessage());
            }
        }
    }

    /**
     * 检查玩家受到攻击
     */
    public void checkPlayerCollisions(GameObject player, List<GameObject> enemies) {

        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        if (playerTransform == null)
            return;

        LifeFeatureComponent playerLife = player.getComponent(LifeFeatureComponent.class);
        if (playerLife == null || playerLife.isunbeatable || playerLife.isInvincible())
            return;

        Vector2 playerPos = playerTransform.getPosition();

        // 获取玩家的碰撞半径
        RenderComponent playerRender = player.getComponent(RenderComponent.class);
        float playerCollisionRadius = 20; // 默认玩家碰撞半径
        if (playerRender != null) {
            Vector2 playerSize = playerRender.getSize();
            playerCollisionRadius = Math.max(playerSize.x, playerSize.y) / 2.0f;
        }

        // 检测与敌人的碰撞
        for (GameObject enemy : enemies) {
            if (!enemy.isActive())
                continue;

            TransformComponent enemyTransform = enemy.getComponent(TransformComponent.class);
            if (enemyTransform != null) {
                // 获取敌人的碰撞半径
                RenderComponent enemyRender = enemy.getComponent(RenderComponent.class);
                float enemyCollisionRadius = 25; // 默认敌人碰撞半径
                if (enemyRender != null) {
                    Vector2 enemySize = enemyRender.getSize();
                    enemyCollisionRadius = Math.max(enemySize.x, enemySize.y) / 2.0f;
                }

                float distance = playerPos.distance(enemyTransform.getPosition());
                float collisionDistance = playerCollisionRadius + enemyCollisionRadius;
                if (distance < collisionDistance) {
                    // 使用同步块保护共享状态的修改
                    synchronized (player) {
                        // 扣血并触发无敌帧，不再回到起点
                        playerLife.blood -= 10;
                        playerLife.triggerInvincibility(); // 触发1秒无敌帧
                    }
                    return; // 一次只处理一个碰撞
                }
            }
        }

        // 检测与敌人技能的碰撞
        for (GameObject obj : scene.getGameObjects()) {
            if ("Enemy Skill".equals(obj.getidentity()) && obj.isActive()) {
                TransformComponent skillTransform = obj.getComponent(TransformComponent.class);
                if (skillTransform != null) {
                    // 获取敌人技能的碰撞半径
                    RenderComponent skillRender = obj.getComponent(RenderComponent.class);
                    float skillCollisionRadius = 10; // 默认技能碰撞半径
                    if (skillRender != null) {
                        Vector2 skillSize = skillRender.getSize();
                        skillCollisionRadius = Math.max(skillSize.x, skillSize.y) / 2.0f;
                    }

                    float distance = playerPos.distance(skillTransform.getPosition());
                    float collisionDistance = playerCollisionRadius + skillCollisionRadius;
                    if (distance < collisionDistance) {
                        synchronized (player) {
                            // 扣血并触发无敌帧，不再回到起点
                            playerLife.blood -= 10;
                            playerLife.triggerInvincibility(); // 触发1秒无敌帧
                        }
                        return;
                    }
                }
            }
        }
    }

    /**
     * 检查怪物受到攻击
     */
    private void checkEnemyCollisions(GameObject enemy, List<GameObject> playerSkills) {
        TransformComponent enemyTransform = enemy.getComponent(TransformComponent.class);
        if (enemyTransform == null)
            return;

        LifeFeatureComponent enemyLife = enemy.getComponent(LifeFeatureComponent.class);
        if (enemyLife == null || enemyLife.isunbeatable)
            return;

        Vector2 enemyPos = enemyTransform.getPosition();

        // 获取敌人的渲染尺寸以计算碰撞半径
        RenderComponent enemyRender = enemy.getComponent(RenderComponent.class);
        float enemyCollisionRadius = 25; // 默认碰撞半径
        if (enemyRender != null) {
            Vector2 enemySize = enemyRender.getSize();
            // 使用敌人尺寸的较大值的一半作为碰撞半径
            enemyCollisionRadius = Math.max(enemySize.x, enemySize.y) / 2.0f;
        }

        // 检查玩家技能碰撞
        for (GameObject skill : playerSkills) {
            if (!skill.isActive())
                continue;

            TransformComponent skillTransform = skill.getComponent(TransformComponent.class);
            if (skillTransform == null)
                continue;

            Vector2 skillPos = skillTransform.getPosition();

            // 获取技能的碰撞半径
            RenderComponent skillRender = skill.getComponent(RenderComponent.class);
            float skillCollisionRadius = 10; // 默认技能碰撞半径
            if (skillRender != null) {
                Vector2 skillSize = skillRender.getSize();
                skillCollisionRadius = Math.max(skillSize.x, skillSize.y) / 2.0f;
            }

            // 检查碰撞（使用两个对象的碰撞半径之和）
            float distance = enemyPos.distance(skillPos);
            float collisionDistance = enemyCollisionRadius + skillCollisionRadius;
            if (distance < collisionDistance) {
                // 使用同步块保护共享状态
                synchronized (enemy) {
                    PhysicsComponent skillPhysics = skill.getComponent(PhysicsComponent.class);

                    // 移动技能到屏幕外
                    skillTransform.setPosition(new Vector2(-1000, -1000));
                    if (skillPhysics != null) {
                        skillPhysics.setVelocity(new Vector2(0, 0));
                    }

                    // 扣除生命值
                    enemyLife.blood -= 50;
                }
                return; // 一次只处理一个碰撞
            }
        }
    }

    public void handleEnemyMove() {

        if (gameOver)
            return;

        List<GameObject> enemies = scene.findGameObjectsByComponent(TransformComponent.class);
        if (enemies.isEmpty())
            return;

        // 筛选出所有敌人
        List<GameObject> enemyList = enemies.stream()
                .filter(obj -> obj.getidentity().equals("Enemy") || obj.getidentity().equals("EnemySanke"))
                .collect(Collectors.toList());

        if (enemyList.isEmpty())
            return;

        // 获取玩家位置
        GameObject player = enemies.stream()
                .filter(obj -> obj.getidentity().equals("Player"))
                .findFirst()
                .orElse(null);

        if (player == null)
            return;

        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        Vector2 playerPos = playerTransform.getPosition();

        // 根据敌人数量决定并行或串行
        if (enemyList.size() < PARALLEL_THRESHOLD) {
            enemyList.forEach(enemy -> updateSingleEnemyMove(enemy, playerPos));
        } else {
            // 这里采用并行流处理敌人移动，而不是executor方法，以简化代码
            System.out.println("敌人移动采用并行流处理");
            enemyList.parallelStream()
                    .forEach(enemy -> updateSingleEnemyMove(enemy, playerPos));
        }
    }

    private void updateSingleEnemyMove(GameObject enemy, Vector2 playerPos) {
        TransformComponent transform = enemy.getComponent(TransformComponent.class);
        PhysicsComponent physics = enemy.getComponent(PhysicsComponent.class);

        if (transform == null || physics == null)
            return;

        Random random = new Random();
        float PlayerAngle = playerPos.subtract(transform.getPosition()).angle();
        float randomAngle = (random.nextFloat() * 60 - 45) * (float) Math.PI / 180;
        float adjustedX = (float) Math.cos(PlayerAngle + randomAngle);
        float adjustedY = (float) Math.sin(PlayerAngle + randomAngle);

        Vector2 direction = new Vector2(adjustedX, adjustedY);
        if (direction.magnitude() > 0) {
            direction = direction.normalize();
        }

        // 国王速度更快
        float speed = enemy.getName().equals("EnemyKing") ? 200 : 100;
        Vector2 velocity = direction.multiply(speed);

        synchronized (enemy) {
            physics.setVelocity(velocity);

            // 边界检查（使用世界地图尺寸）
            Vector2 pos = transform.getPosition();
            pos.x = Math.max(0, Math.min(WORLD_WIDTH - 20, pos.x));
            pos.y = Math.max(0, Math.min(WORLD_HEIGHT - 20, pos.y));
            transform.setPosition(pos);
        }
    }

    public void updateEnemyMovement(float deltaTime) {
        enemyMoveTimer += deltaTime;

        if (enemyMoveTimer >= enemyMoveInterval) {
            enemyMoveTimer = 0;
            handleEnemyMove();
        }
    }

    public void updateEnemyAttack(float deltaTime) {
        EnemyAttackLifeTimer += deltaTime;

        // 每2秒重新发起攻击
        if (EnemyAttackLifeTimer >= EnemyAttackLifeDuration) {
            EnemyAttackLifeTimer = 0;
            handleEnemyAttack();
        }

    }

    public void updateAttack(float deltaTime) {
        // 更新技能冷却计时器
        if (playerSkillCooldownTimer < playerSkillCooldownDuration) {
            playerSkillCooldownTimer += deltaTime;
        }

        // 更新所有 PlayerAttack 的生命周期
        for (GameObject gameObject : scene.getGameObjects()) {
            if (gameObject.getidentity().equals("Player Skill")) {
                LifeFeatureComponent lifeFeature = gameObject.getComponent(LifeFeatureComponent.class);
                if (lifeFeature != null) {
                    lifeFeature.update(deltaTime);
                }
            }
            if (gameObject.getidentity().equals("Enemy Skill")) {
                LifeFeatureComponent lifeFeature = gameObject.getComponent(LifeFeatureComponent.class);
                if (lifeFeature != null) {
                    lifeFeature.update(deltaTime);
                }
            }
        }
    }

    /**
     * 获取技能冷却的百分比（0.0 到 1.0）
     * 
     * @return 冷却进度百分比，1.0 表示完全冷却
     */
    public float getSkillCooldownPercentage() {
        if (playerSkillCooldownDuration <= 0) {
            return 1.0f;
        }
        return Math.min(1.0f, playerSkillCooldownTimer / playerSkillCooldownDuration);
    }

    public void handlePlayerAttackJ() {
        // 检查技能是否在冷却中
        if (playerSkillCooldownTimer < playerSkillCooldownDuration) {
            return;
        }

        GameObject player = getPlayer();
        if (!(player instanceof HuluPlayer)) {
            return;
        }

        HuluPlayer huluPlayer = (HuluPlayer) player;
        if (huluPlayer.releaseSkillJ()) {
            playerSkillCooldownTimer = 0;
        }
    }

    public void handleEnemyAttack() {
        // 获取玩家位置
        GameObject player = getPlayer();
        Vector2 playerPosition = null;
        if (player != null) {
            TransformComponent playerTransform = player.getComponent(TransformComponent.class);
            if (playerTransform != null) {
                playerPosition = playerTransform.getPosition();
            }
        }
        
        // 处理 EnemyKing 的攻击
        for (GameObject obj : scene.getGameObjects()) {
            if (obj instanceof EnemyKing) {
                EnemyKing enemyKing = (EnemyKing) obj;
                enemyKing.releaseAttackSkills();
            }
        }
        
        // 处理 EnemySoldier 的攻击 - 向玩家方向发射
        if (playerPosition != null) {
            for (GameObject obj : scene.getGameObjects()) {
                if (obj instanceof EnemySoldier) {
                    EnemySoldier soldier = (EnemySoldier) obj;
                    soldier.releaseAttackSkills(playerPosition);
                }
            }
        }
    }
    
    /**
     * 更新 EnemyKing 的追踪火球技能
     * 在第二、三阶段，火球会追踪玩家
     */
    public void updateEnemyKingSkills(float deltaTime) {
        GameObject player = getPlayer();
        Vector2 playerPosition = null;
        if (player != null) {
            TransformComponent playerTransform = player.getComponent(TransformComponent.class);
            if (playerTransform != null) {
                playerPosition = playerTransform.getPosition();
            }
        }
        
        // 更新所有国王的追踪技能
        for (GameObject obj : scene.getGameObjects()) {
            if (obj instanceof EnemyKing) {
                EnemyKing king = (EnemyKing) obj;
                king.updateSkills(playerPosition, deltaTime);
            }
        }
    }
    
    /**
     * 更新 EnemyWizard 的陷阱球技能
     * 这个方法会在每帧调用，以更新陷阱球的状态
     */
    public void updateEnemyWizardSkills(float deltaTime) {
        GameObject player = getPlayer();
        Vector2 playerPosition = null;
        if (player != null) {
            TransformComponent playerTransform = player.getComponent(TransformComponent.class);
            if (playerTransform != null) {
                playerPosition = playerTransform.getPosition();
            }
        }
        
        // 更新所有法师的技能
        for (GameObject obj : scene.getGameObjects()) {
            if (obj instanceof EnemyWizard) {
                EnemyWizard wizard = (EnemyWizard) obj;
                wizard.updateSkills(playerPosition, deltaTime);
            }
        }
    }

    public void checkEntityAlive() {

        List<LifeFeatureComponent> lifeFeatureComponents = scene.getComponents(LifeFeatureComponent.class);

        for (LifeFeatureComponent lifeFeatureComponent : lifeFeatureComponents) {
            if (lifeFeatureComponent.getBlood() <= 0) {

                GameObject entity = lifeFeatureComponent.getOwner();

                if (entity.getidentity().equals("Enemy")) {
                    System.out.println(entity.getName() + " died");
                    entity.setActive(false);
                } else if (entity.getidentity().equals("Player")) {
                    System.out.println("Game Over");
                    this.gameOver = true;
                    engine.gameOver();
                }

                // 使实体消失（设置为不活跃状态）
                // 这会在下一次Scene.update()中被移除

            }
        }
    }

    public boolean checkEnemiesDied() {
        for (GameObject obj : scene.getGameObjects()) {
            if (obj.isActive() && obj.getidentity().equals("Enemy")) {
                return false;
            }
        }
        return true;
    }

    // ========== Multiplayer 相关方法 ==========

    /**
     * 处理多人游戏技能释放
     * 玩家1: J 键
     * 玩家2: Enter 键
     */
    public void handleMultiPlayerSkills() {
        if (multiplayerPlayers == null)
            return;

        // 玩家1 按 J 释放技能
        if (inputManager.isKeyJustPressed(74) && multiplayerPlayers[0] != null && multiplayerPlayers[0].isActive()) { // J
            tryReleaseMultiplayerSkill(0);
        }

        // 玩家2 按 Enter 释放技能
        if (multiplayerPlayers.length >= 2 && inputManager.isKeyJustPressed(257) && multiplayerPlayers[1] != null
                && multiplayerPlayers[1].isActive()) { // Enter
            tryReleaseMultiplayerSkill(1);
        }

        // 玩家3 按 Space 释放技能（如果有）
        if (multiplayerPlayers.length >= 3 && inputManager.isKeyJustPressed(32) && multiplayerPlayers[2] != null
                && multiplayerPlayers[2].isActive()) { // Space
            tryReleaseMultiplayerSkill(2);
        }

        // 玩家4 按 Numpad 0 释放技能（如果有）
        if (multiplayerPlayers.length >= 4 && inputManager.isKeyJustPressed(320) && multiplayerPlayers[3] != null
                && multiplayerPlayers[3].isActive()) { // Numpad 0
            tryReleaseMultiplayerSkill(3);
        }
    }

    /**
     * 尝试释放指定玩家的技能
     */
    private void tryReleaseMultiplayerSkill(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= multiplayerPlayers.length)
            return;
        if (multiplayerPlayers[playerIndex] == null || !multiplayerPlayers[playerIndex].isActive())
            return;

        // 检查冷却
        if (playerSkillCooldownTimers[playerIndex] < SKILL_COOLDOWN_DURATION) {
            return;
        }

        HuluPlayer player = multiplayerPlayers[playerIndex];
        if (player.releaseSkillJ()) {
            playerSkillCooldownTimers[playerIndex] = 0;
        }
    }

    /**
     * 更新所有玩家的技能冷却
     */
    public void updateMultiplayerSkillCooldowns(float deltaTime) {
        if (playerSkillCooldownTimers == null)
            return;

        for (int i = 0; i < playerSkillCooldownTimers.length; i++) {
            if (playerSkillCooldownTimers[i] < SKILL_COOLDOWN_DURATION) {
                playerSkillCooldownTimers[i] += deltaTime;
            }
        }
    }

    /**
     * 检查玩家之间的碰撞（PvP 近战伤害）
     */
    public void checkMultiplayerPlayerCollisions(float deltaTime) {
        if (multiplayerPlayers == null)
            return;

        for (int i = 0; i < multiplayerPlayers.length; i++) {
            if (multiplayerPlayers[i] == null || !multiplayerPlayers[i].isActive())
                continue;

            TransformComponent t1 = multiplayerPlayers[i].getComponent(TransformComponent.class);
            LifeFeatureComponent l1 = multiplayerPlayers[i].getComponent(LifeFeatureComponent.class);
            if (t1 == null || l1 == null)
                continue;

            Vector2 pos1 = t1.getPosition();

            for (int j = i + 1; j < multiplayerPlayers.length; j++) {
                if (multiplayerPlayers[j] == null || !multiplayerPlayers[j].isActive())
                    continue;

                TransformComponent t2 = multiplayerPlayers[j].getComponent(TransformComponent.class);
                LifeFeatureComponent l2 = multiplayerPlayers[j].getComponent(LifeFeatureComponent.class);
                if (t2 == null || l2 == null)
                    continue;

                Vector2 pos2 = t2.getPosition();
                float distance = pos1.distance(pos2);

                // 碰撞距离阈值
                if (distance < 30) {
                    // 检查是否处于无敌状态
                    boolean player1Invincible = l1.isInvincible();
                    boolean player2Invincible = l2.isInvincible();

                    // 双方都受到伤害（除非处于无敌状态）
                    if (!player1Invincible) {
                        l1.blood -= 5;
                        l1.triggerInvincibility(); // 触发无敌帧
                    }
                    if (!player2Invincible) {
                        l2.blood -= 5;
                        l2.triggerInvincibility(); // 触发无敌帧
                    }

                    // 产生碰撞粒子效果
                    Vector2 collisionPoint = new Vector2(
                            (pos1.x + pos2.x) / 2,
                            (pos1.y + pos2.y) / 2);
                    createMultiplayerCollisionParticles(collisionPoint);

                    // 推开两个玩家
                    Vector2 pushDir = pos1.subtract(pos2).normalize();
                    t1.setPosition(pos1.add(pushDir.multiply(20)));
                    t2.setPosition(pos2.subtract(pushDir.multiply(20)));
                }
            }
        }
    }

    /**
     * 检查技能与玩家的碰撞
     */
    public void checkMultiplayerSkillCollisions() {
        if (multiplayerPlayers == null)
            return;

        for (int attackerIndex = 0; attackerIndex < multiplayerPlayers.length; attackerIndex++) {
            if (multiplayerPlayers[attackerIndex] == null || !multiplayerPlayers[attackerIndex].isActive())
                continue;

            List<GameObject> skills = multiplayerPlayers[attackerIndex].getAttackingSkillsJ();
            if (skills == null)
                continue;

            for (GameObject skill : skills) {
                if (!skill.isActive())
                    continue;

                TransformComponent skillTransform = skill.getComponent(TransformComponent.class);
                if (skillTransform == null)
                    continue;

                Vector2 skillPos = skillTransform.getPosition();

                // 检查与其他玩家的碰撞（不需要屏幕边界检查）
                for (int targetIndex = 0; targetIndex < multiplayerPlayers.length; targetIndex++) {
                    if (targetIndex == attackerIndex)
                        continue; // 不能伤害自己
                    if (multiplayerPlayers[targetIndex] == null || !multiplayerPlayers[targetIndex].isActive())
                        continue;

                    TransformComponent targetTransform = multiplayerPlayers[targetIndex]
                            .getComponent(TransformComponent.class);
                    LifeFeatureComponent targetLife = multiplayerPlayers[targetIndex]
                            .getComponent(LifeFeatureComponent.class);
                    if (targetTransform == null || targetLife == null)
                        continue;

                    // 如果目标处于无敌状态，跳过
                    if (targetLife.isInvincible())
                        continue;

                    Vector2 targetPos = targetTransform.getPosition();
                    float distance = skillPos.distance(targetPos);

                    if (distance < 25) {
                        // 造成伤害并触发无敌帧
                        targetLife.blood -= 20;
                        targetLife.triggerInvincibility(); // 触发无敌帧

                        // 产生碰撞粒子
                        createMultiplayerCollisionParticles(targetPos);

                        // 移除技能
                        skillTransform.setPosition(new Vector2(-1000, -1000));
                        PhysicsComponent skillPhysics = skill.getComponent(PhysicsComponent.class);
                        if (skillPhysics != null) {
                            skillPhysics.setVelocity(new Vector2(0, 0));
                        }

                        break;
                    }
                }
            }
        }
    }

    /**
     * 创建碰撞粒子效果
     */
    private void createMultiplayerCollisionParticles(Vector2 position) {
        ParticleSystem.Config cfg = new ParticleSystem.Config();
        cfg.initialCount = 0;
        cfg.spawnRate = 9999f;
        cfg.opacityMultiplier = 1.0f;
        cfg.minRenderSize = 3.0f;
        cfg.burstSpeedMin = 150f;
        cfg.burstSpeedMax = 350f;
        cfg.burstLifeMin = 0.3f;
        cfg.burstLifeMax = 0.8f;
        cfg.burstSizeMin = 8f;
        cfg.burstSizeMax = 20f;
        cfg.burstR = 1.0f;
        cfg.burstGMin = 0.5f;
        cfg.burstGMax = 1.0f;
        cfg.burstB = 0.0f;

        ParticleSystem explosion = new ParticleSystem(renderer, position, cfg);
        explosion.burst(50);
        collisionParticles.add(explosion);
    }

    /**
     * 更新粒子效果
     */
    public void updateMultiplayerParticles(float deltaTime) {
        if (multiplayerPlayers == null || playerParticles == null)
            return;

        // 注释掉玩家粒子更新，移除葫芦娃的粒子效果
        // 更新玩家粒子
        // for (int i = 0; i < multiplayerPlayers.length; i++) {
        // if (multiplayerPlayers[i] != null && multiplayerPlayers[i].isActive()) {
        // ParticleSystem ps = playerParticles.get(multiplayerPlayers[i]);
        // if (ps != null) {
        // TransformComponent transform =
        // multiplayerPlayers[i].getComponent(TransformComponent.class);
        // if (transform != null) {
        // ps.setPosition(transform.getPosition());
        // }
        // ps.update(deltaTime);
        // }
        // }
        // }

        // 更新碰撞粒子
        for (int i = collisionParticles.size() - 1; i >= 0; i--) {
            ParticleSystem ps = collisionParticles.get(i);
            if (ps != null) {
                ps.update(deltaTime);
                if (ps.getParticleCount() == 0) {
                    collisionParticles.remove(i);
                }
            }
        }
    }

    /**
     * 渲染粒子效果
     */
    public void renderMultiplayerParticles() {
        if (playerParticles == null || collisionParticles == null)
            return;

        // 注释掉玩家粒子渲染，移除葫芦娃的粒子效果
        // 渲染玩家粒子
        // for (ParticleSystem ps : playerParticles.values()) {
        // if (ps != null && ps.getParticleCount() > 0) {
        // ps.render();
        // }
        // }

        // 渲染碰撞粒子
        for (ParticleSystem ps : collisionParticles) {
            if (ps != null && ps.getParticleCount() > 0) {
                ps.render();
            }
        }
    }

    /**
     * 检查游戏结束条件
     */
    public void checkMultiplayerGameOver() {
        if (multiplayerPlayers == null)
            return;

        int aliveCount = 0;
        int lastAliveIndex = -1;

        for (int i = 0; i < multiplayerPlayers.length; i++) {
            if (multiplayerPlayers[i] != null && multiplayerPlayers[i].isActive()) {
                LifeFeatureComponent life = multiplayerPlayers[i].getComponent(LifeFeatureComponent.class);
                if (life != null && life.getBlood() > 0) {
                    aliveCount++;
                    lastAliveIndex = i;
                } else {
                    // 玩家死亡，产生爆炸效果
                    if (multiplayerPlayers[i].isActive()) {
                        TransformComponent transform = multiplayerPlayers[i].getComponent(TransformComponent.class);
                        if (transform != null) {
                            createMultiplayerDeathExplosion(transform.getPosition());
                        }
                        multiplayerPlayers[i].setActive(false);
                    }
                }
            }
        }

        // 只剩一个玩家存活，游戏结束
        if (aliveCount <= 1 && multiplayerPlayers.length > 1) {
            multiplayerGameOver = true;
            winnerIndex = lastAliveIndex;
        }
    }

    /**
     * 创建死亡爆炸效果
     */
    private void createMultiplayerDeathExplosion(Vector2 position) {
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

        ParticleSystem explosion = new ParticleSystem(renderer, position, cfg);
        explosion.burst(180);
        collisionParticles.add(explosion);
    }

    // ========== Multiplayer Getter 方法 ==========

    public boolean isMultiplayerGameOver() {
        return multiplayerGameOver;
    }

    public int getWinnerIndex() {
        return winnerIndex;
    }

    public float[] getPlayerSkillCooldownTimers() {
        return playerSkillCooldownTimers;
    }
}
