package com.gameengine.app;

import com.gameengine.app.testutils.FakeRenderer;
import com.gameengine.app.testutils.TestUtils;
import com.gameengine.input.InputManager;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;

public class GameSceneTest {

    @Test
    public void testSetRecordingFlag() throws Exception {
        InputManager im = InputManager.getInstance();
        im.reset();
        FakeRenderer fr = new FakeRenderer(800,600);
        com.gameengine.core.GameEngine engine = TestUtils.createEngineWith(fr, im);

        GameScene gs = new GameScene("test", engine);

        File tmp = File.createTempFile("rec_test", ".json");
        FileWriter fw = new FileWriter(tmp, true);
        gs.setRecording(fw);

        // check recording flag via reflection
        java.lang.reflect.Field f = GameScene.class.getDeclaredField("isRecording");
        f.setAccessible(true);
        boolean isRec = f.getBoolean(gs);
        Assert.assertTrue(isRec);

        fw.close();
        tmp.delete();
    }
}

