package com.gameengine.app;

import com.gameengine.app.testutils.FakeRenderer;
import com.gameengine.app.testutils.TestUtils;
import com.gameengine.input.InputManager;
import org.junit.Assert;
import org.junit.Test;

public class RecordingAndReplayTests {

    @Test
    public void testReplayLoadCallsRecordingScene() throws Exception {
        InputManager im = InputManager.getInstance();
        im.reset();
        FakeRenderer fr = new FakeRenderer(800,600);
        com.gameengine.core.GameEngine engine = TestUtils.createEngineWith(fr, im);

        ReplayScene replay = new ReplayScene(engine, null);
        // call loadRecording should replace engine scene with RecordingScene
        replay.loadRecording("nonexistent_path.json");

        Assert.assertNotNull(engine.getCurrentScene());
        Assert.assertTrue(engine.getCurrentScene() instanceof RecordingScene);
    }

    @Test
    public void testRecordingSceneHandlesMissingFileGracefully() throws Exception {
        InputManager im = InputManager.getInstance();
        im.reset();
        FakeRenderer fr = new FakeRenderer(800,600);
        com.gameengine.core.GameEngine engine = TestUtils.createEngineWith(fr, im);

        // construct RecordingScene with path that doesn't exist
        RecordingScene rs = new RecordingScene(engine, "__no_such_file__.json");
        // call update and render to ensure no exceptions
        rs.update(0.01f);
        rs.render();
    }
}
