package com.gameengine.dialogue;

import com.gameengine.dialogue.DialogueNode.SpeakerType;
import com.gameengine.graphics.IRenderer;

/**
 * 对话配置器
 * 负责注册游戏中的所有对话内容
 */
public class DialogueConfigurator {
    private final DialogueManager dialogueManager;
    
    // 角色头像路径
    private static final String[] HULUWA_PORTRAITS = {
        "resources/picture/huluBro1.png",  // 大娃
        "resources/picture/huluBro2.png",  // 二娃
        "resources/picture/huluBro3.png",  // 三娃
        "resources/picture/huluBro4.png",  // 四娃
        "resources/picture/huluBro5.png",  // 五娃
        "resources/picture/huluBro6.png",  // 六娃
        "resources/picture/huluBro7.png"   // 七娃
    };
    private static final String ENEMY_PORTRAIT = "resources/picture/snake_queen.png";
    private static final String WIZARD_PORTRAIT = "resources/picture/wizard.png";
    
    /**
     * 根据关卡号获取对应的葫芦娃头像
     * @param level 关卡号（1-7）
     * @return 对应的头像路径
     */
    private static String getHuluwaPortrait(int level) {
        int index = Math.max(0, Math.min(level - 1, HULUWA_PORTRAITS.length - 1));
        return HULUWA_PORTRAITS[index];
    }
    
    public DialogueConfigurator(IRenderer renderer) {
        this.dialogueManager = DialogueManager.getInstance();
        this.dialogueManager.reset();
        this.dialogueManager.initRenderer(renderer);
    }
    
    /**
     * 初始化所有对话
     */
    public void initializeAllDialogues() {
        registerGameStartDialogue();
        registerGameOverDialogues();
    }
    
    /**
     * 触发游戏开始对话
     */
    public void triggerGameStart() {
        dialogueManager.triggerEvent(DialogueTriggerType.GAME_START);
    }
    
    /**
     * 获取对话管理器实例
     */
    public DialogueManager getDialogueManager() {
        return dialogueManager;
    }
    
    /**
     * 触发关卡完成对话
     */
    public void triggerLevelComplete(int level) {
        DialogueNode levelDialogue = createLevelCompleteDialogue(level);
        if (levelDialogue != null) {
            dialogueManager.startDialogue(levelDialogue);
        }
    }
    
    /**
     * 触发无尽模式对话
     */
    public void triggerEndlessMode() {
        DialogueNode endlessDialogue = createEndlessModeDialogue();
        if (endlessDialogue != null) {
            dialogueManager.startDialogue(endlessDialogue);
        }
    }
    
    /**
     * 注册游戏开场对话（第一关背景介绍 - 大娃力大无穷）
     */
    private void registerGameStartDialogue() {
        // 游戏背景介绍
        DialogueNode intro1 = new DialogueNode("旁白", "很久很久以前，在一片神秘的东方大陆上，有一座被迷雾笼罩的葫芦山...", 
                                               SpeakerType.NARRATOR, null);
        
        DialogueNode intro2 = new DialogueNode("旁白", "葫芦山上生长着一株神奇的葫芦藤，七个彩色的葫芦在藤上闪闪发光。", 
                                               SpeakerType.NARRATOR, null);
        
        DialogueNode intro3 = new DialogueNode("旁白", "然而，邪恶的蛇精和蝎子精觊觎葫芦的神奇力量，派遣妖怪大军入侵葫芦山！", 
                                               SpeakerType.NARRATOR, null);
        
        DialogueNode intro4 = new DialogueNode("旁白", "在危急关头，红色的大葫芦率先成熟，大娃从葫芦中诞生！", 
                                               SpeakerType.NARRATOR, null);
        
        // 第一关剧情 - 大娃
        DialogueNode intro5 = new DialogueNode("大娃", "爷爷，放心吧！我力大无穷，一定会打败这些妖怪的！", 
                                               SpeakerType.PLAYER, getHuluwaPortrait(1));
        
        DialogueNode intro6 = new DialogueNode("旁白", "葫芦山脚下，一队妖怪士兵正在巡逻...", 
                                               SpeakerType.NARRATOR, null);
        
        DialogueNode intro7 = new DialogueNode("大娃", "妖怪们！休想踏入葫芦山一步！看我的铁拳！", 
                                               SpeakerType.PLAYER, getHuluwaPortrait(1));
        
        DialogueNode intro8 = new DialogueNode("旁白", "【第一关：山脚遭遇战】大娃力大无穷，消灭所有妖怪士兵！", 
                                               SpeakerType.NARRATOR, null);
        
        DialogueNode intro9 = new DialogueNode("旁白", "【操作提示】使用 WASD 移动，按 J 释放技能攻击敌人。", 
                                               SpeakerType.NARRATOR, null);

        intro1.then(intro2).then(intro3).then(intro4).then(intro5).then(intro6).then(intro7).then(intro8).then(intro9);
        dialogueManager.registerDialogue(DialogueTriggerType.GAME_START, intro1);
    }
    
    /**
     * 注册游戏结束对话
     */
    private void registerGameOverDialogues() {
        DialogueNode defeat1 = new DialogueNode("旁白", "葫芦娃倒下了...", 
                                                SpeakerType.NARRATOR, null);
        DialogueNode defeat2 = new DialogueNode("旁白", "邪恶的妖怪们发出了得意的笑声，森林陷入了黑暗...", 
                                                SpeakerType.NARRATOR, null);
        DialogueNode defeat3 = new DialogueNode("旁白", "【游戏结束】按 ESC 返回主菜单，再次挑战！", 
                                                SpeakerType.NARRATOR, null);
        defeat1.then(defeat2).then(defeat3);
        dialogueManager.registerDialogue(DialogueTriggerType.DEFEAT, defeat1);
    }
    
    /**
     * 创建关卡完成对话
     */
    private DialogueNode createLevelCompleteDialogue(int level) {
        switch (level) {
            case 2:
                // 第二关开始剧情：二娃 - 千里眼顺风耳
                DialogueNode l2d1 = new DialogueNode("大娃", "这些小喽啰不堪一击！但我感觉前方还有更多敌人...", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(1));
                DialogueNode l2d2 = new DialogueNode("旁白", "就在此时，橙色的葫芦闪耀光芒，二娃从葫芦中诞生！", 
                                                     SpeakerType.NARRATOR, null);
                DialogueNode l2d3 = new DialogueNode("二娃", "大哥，让我用千里眼和顺风耳帮你探路！", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(2));
                DialogueNode l2d4 = new DialogueNode("二娃", "不好！前面有妖怪的巫师设下了埋伏！", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(2));
                DialogueNode l2d5 = new DialogueNode("巫师", "呵呵呵...被你发现了也没用，你们逃不掉的！", 
                                                     SpeakerType.ENEMY, WIZARD_PORTRAIT);
                DialogueNode l2d6 = new DialogueNode("旁白", "【第二关：山道伏击】二娃千里眼顺风耳，击败巫师和士兵！", 
                                                     SpeakerType.NARRATOR, null);
                l2d1.then(l2d2).then(l2d3).then(l2d4).then(l2d5).then(l2d6);
                return l2d1;
            case 3:
                // 第三关开始剧情：三娃 - 铜头铁臂
                DialogueNode l3d1 = new DialogueNode("二娃", "巫师被打败了！但我听到更多妖怪在前方...", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(2));
                DialogueNode l3d2 = new DialogueNode("旁白", "黄色的葫芦绽放金光，三娃横空出世！", 
                                                     SpeakerType.NARRATOR, null);
                DialogueNode l3d3 = new DialogueNode("三娃", "哈哈！我铜头铁臂，刀枪不入！让我来对付他们！", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(3));
                DialogueNode l3d4 = new DialogueNode("蛇精女王", "呵呵呵...又来了一个送死的...", 
                                                     SpeakerType.ENEMY, ENEMY_PORTRAIT);
                DialogueNode l3d5 = new DialogueNode("三娃", "妖怪！你的法术对我没用！", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(3));
                DialogueNode l3d6 = new DialogueNode("旁白", "【第三关：铁壁防线】三娃铜头铁臂，抵挡妖法攻击！", 
                                                     SpeakerType.NARRATOR, null);
                l3d1.then(l3d2).then(l3d3).then(l3d4).then(l3d5).then(l3d6);
                return l3d1;
            case 4:
                // 第四关开始剧情：四娃 - 喷火
                DialogueNode l4d1 = new DialogueNode("三娃", "虽然我刀枪不入，但妖怪的数量太多了...", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(3));
                DialogueNode l4d2 = new DialogueNode("旁白", "绿色的葫芦燃起熊熊火焰，四娃破壳而出！", 
                                                     SpeakerType.NARRATOR, null);
                DialogueNode l4d3 = new DialogueNode("四娃", "哼！让我用火焰烧尽这些妖怪！", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(4));
                DialogueNode l4d4 = new DialogueNode("旁白", "妖怪们被四娃的火焰吓得四处逃窜...", 
                                                     SpeakerType.NARRATOR, null);
                DialogueNode l4d5 = new DialogueNode("蛇精女王", "可恶！别以为会喷火就了不起！给我上！", 
                                                     SpeakerType.ENEMY, ENEMY_PORTRAIT);
                DialogueNode l4d6 = new DialogueNode("旁白", "【第四关：火焰试炼】四娃喷火，烧尽妖巢！", 
                                                     SpeakerType.NARRATOR, null);
                l4d1.then(l4d2).then(l4d3).then(l4d4).then(l4d5).then(l4d6);
                return l4d1;
            case 5:
                // 第五关开始剧情：五娃 - 吐水
                DialogueNode l5d1 = new DialogueNode("四娃", "火焰虽然厉害，但妖怪越来越多了...", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(4));
                DialogueNode l5d2 = new DialogueNode("旁白", "青色的葫芦涌出滔滔流水，五娃从水中诞生！", 
                                                     SpeakerType.NARRATOR, null);
                DialogueNode l5d3 = new DialogueNode("五娃", "让我用水来配合四哥！水火相济，威力无穷！", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(5));
                DialogueNode l5d4 = new DialogueNode("蛇精女王", "哼！水火都来了又如何？", 
                                                     SpeakerType.ENEMY, ENEMY_PORTRAIT);
                DialogueNode l5d5 = new DialogueNode("五娃", "妖怪！今天就用洪水淹没你的妖洞！", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(5));
                DialogueNode l5d6 = new DialogueNode("旁白", "【第五关：水淹妖洞】五娃吐水，困住妖军！", 
                                                     SpeakerType.NARRATOR, null);
                l5d1.then(l5d2).then(l5d3).then(l5d4).then(l5d5).then(l5d6);
                return l5d1;
            case 6:
                // 第六关开始剧情：六娃 - 隐身
                DialogueNode l6d1 = new DialogueNode("五娃", "妖怪的防守越来越严密了...", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(5));
                DialogueNode l6d2 = new DialogueNode("旁白", "蓝色的葫芦闪烁神秘光芒，六娃悄然现身！", 
                                                     SpeakerType.NARRATOR, null);
                DialogueNode l6d3 = new DialogueNode("六娃", "嘿嘿，让我隐身潜入，出其不意！", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(6));
                DialogueNode l6d4 = new DialogueNode("巫师", "奇怪...怎么感觉有什么东西靠近...", 
                                                     SpeakerType.ENEMY, WIZARD_PORTRAIT);
                DialogueNode l6d5 = new DialogueNode("六娃", "哈哈！等你发现我的时候，已经太迟了！", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(6));
                DialogueNode l6d6 = new DialogueNode("旁白", "【第六关：隐身突袭】六娃隐身，潜入妖穴核心！", 
                                                     SpeakerType.NARRATOR, null);
                l6d1.then(l6d2).then(l6d3).then(l6d4).then(l6d5).then(l6d6);
                return l6d1;
            case 7:
                // 第七关最终决战：七娃 - 宝葫芦
                DialogueNode l7d1 = new DialogueNode("六娃", "我们已经来到蛇精的巢穴深处了...", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(6));
                DialogueNode l7d2 = new DialogueNode("旁白", "最后，紫色的葫芦散发出圣洁的光芒，七娃降临人间！", 
                                                     SpeakerType.NARRATOR, null);
                DialogueNode l7d3 = new DialogueNode("七娃", "我的宝葫芦可以收尽天下妖邪！兄弟们，最后一战！", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(7));
                DialogueNode l7d4 = new DialogueNode("蛇精女王", "七个葫芦娃都来了？！好，今天就一起消灭你们！", 
                                                     SpeakerType.ENEMY, ENEMY_PORTRAIT);
                DialogueNode l7d5 = new DialogueNode("七娃", "妖怪！今天就是你们的末日！宝葫芦，收！", 
                                                     SpeakerType.PLAYER, getHuluwaPortrait(7));
                DialogueNode l7d6 = new DialogueNode("旁白", "【最终关：收妖大战】七兄弟齐心协力，消灭所有妖怪！", 
                                                     SpeakerType.NARRATOR, null);
                l7d1.then(l7d2).then(l7d3).then(l7d4).then(l7d5).then(l7d6);
                return l7d1;
            case 8:
                return createEndlessModeDialogue();
            default:
                return null;
        }
    }
    
    /**
     * 创建无尽模式对话
     */
    private DialogueNode createEndlessModeDialogue() {
        DialogueNode end1 = new DialogueNode("旁白", "蛇精女王和她的妖怪大军被七兄弟彻底击溃！", 
                                             SpeakerType.NARRATOR, null);
        DialogueNode end2 = new DialogueNode("七娃", "兄弟们！我们胜利了！葫芦山终于和平了！", 
                                             SpeakerType.PLAYER, getHuluwaPortrait(7));
        DialogueNode end3 = new DialogueNode("旁白", "然而，就在胜利的喜悦中，远处传来了阵阵妖风...", 
                                             SpeakerType.NARRATOR, null);
        DialogueNode end4 = new DialogueNode("旁白", "蝎子精率领着更多的妖怪大军向葫芦山进发！", 
                                             SpeakerType.NARRATOR, null);
        DialogueNode end5 = new DialogueNode("七娃", "兄弟们，战斗还没有结束！来多少我们打多少！", 
                                             SpeakerType.PLAYER, getHuluwaPortrait(7));
        DialogueNode end6 = new DialogueNode("旁白", "【无尽模式】妖怪大军源源不断，七兄弟并肩作战，坚持到最后！", 
                                             SpeakerType.NARRATOR, null);
        end1.then(end2).then(end3).then(end4).then(end5).then(end6);
        return end1;
    }
}
