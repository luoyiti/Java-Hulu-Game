package com.gameengine.app;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.*;

public class GameTest {

    @Test
    public void testMainPrintsStartupAndShutdown() throws Exception {
        // capture stdout/stderr
        PrintStream origOut = System.out;
        PrintStream origErr = System.err;
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outBuf));
        System.setErr(new PrintStream(errBuf));

        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<?> f = ex.submit(() -> {
            try {
                Game.main(new String[0]);
            } catch (Throwable t) {
                // swallow to let test inspect outputs
                t.printStackTrace(System.err);
            }
        });

        try {
            // wait up to 5 seconds for main to complete (should exit quickly on failure)
            f.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            // cancel if stuck
            f.cancel(true);
        } finally {
            ex.shutdownNow();
            System.setOut(origOut);
            System.setErr(origErr);
        }

        String out = outBuf.toString("UTF-8");
        String err = errBuf.toString("UTF-8");

        // main should at least print startup and shutdown lines
        Assert.assertTrue("stdout should contain startup message", out.contains("启动游戏引擎"));
        Assert.assertTrue("stdout should contain game end message", out.contains("游戏结束"));

        // if there was an error during engine init, it should be printed to stderr
        // but we don't require a specific error; ensure test did not hang
    }
}
