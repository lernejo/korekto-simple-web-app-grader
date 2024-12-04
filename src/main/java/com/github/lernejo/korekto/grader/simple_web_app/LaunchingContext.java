package com.github.lernejo.korekto.grader.simple_web_app;

import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.GradingContext;
import com.github.lernejo.korekto.toolkit.partgrader.MavenContext;

import java.util.ArrayList;
import java.util.List;

public class LaunchingContext extends GradingContext implements MavenContext {
    public final Integer pgPort;
    public final TodoApiClient client;
    public final List<String> instanceIds = new ArrayList<>();
    private boolean compilationFailed;
    private boolean testFailed;
    public Integer postedTodosNbr = null;

    public LaunchingContext(GradingConfiguration configuration, Integer pgPort, TodoApiClient client) {
        super(configuration);
        this.pgPort = pgPort;
        this.client = client;
    }

    public static long serverStartTime() {
        return Long.parseLong(System.getProperty("server_start_timeout", "20"));
    }

    public String pgUrl() {
        return "jdbc:postgresql://localhost:" + pgPort + "/postgres";
    }

    @Override
    public boolean hasCompilationFailed() {
        return compilationFailed;
    }

    @Override
    public boolean hasTestFailed() {
        return testFailed;
    }

    @Override
    public void markAsCompilationFailed() {
        compilationFailed = true;
    }

    @Override
    public void markAsTestFailed() {
        testFailed = true;
    }
}
