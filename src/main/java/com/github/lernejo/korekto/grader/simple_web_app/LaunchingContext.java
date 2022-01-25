package com.github.lernejo.korekto.grader.simple_web_app;

import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.GradingContext;

import java.util.ArrayList;
import java.util.List;

public class LaunchingContext extends GradingContext {
    public final Integer pgPort;
    public final List<String> instanceIds = new ArrayList<>();
    public boolean compilationFailed;
    public boolean testFailed;
    public Integer postedTodosNbr = null;

    public LaunchingContext(GradingConfiguration configuration, Integer pgPort) {
        super(configuration);
        this.pgPort = pgPort;
    }

    public String pgUrl() {
        return "jdbc:postgresql://localhost:" + pgPort + "/postgres";
    }
}
