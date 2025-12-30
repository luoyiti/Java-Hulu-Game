package com.gameengine.app;

import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.*;

public class ClientGameLauncherTest {

    @Test
    public void main_doesNotHang_quicklyReturnsOnInitFailure() throws Exception {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<?> f = ex.submit(() -> {
            ClientGameLauncher.main(new String[]{});
        });

        try {
            f.get(5, TimeUnit.SECONDS); // expect main to return within 5s (init fails fast)
        } catch (TimeoutException te) {
            f.cancel(true);
            fail("ClientGameLauncher.main hung or blocked initialization");
        } finally {
            ex.shutdownNow();
        }
    }
}
