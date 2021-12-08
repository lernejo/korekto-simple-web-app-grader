package com.github.lernejo.korekto.grader.simple_web_app;

import java.util.ArrayList;
import java.util.List;

import com.github.lernejo.korekto.toolkit.misc.SubjectForToolkitInclusion;

@SubjectForToolkitInclusion
public class LaunchingContext {
    public final Integer pgPort;
    public final List<String> instanceIds = new ArrayList<>();
    public boolean compilationFailed;
    public boolean testFailed;
    public Integer postedTodosNbr = null;

    public LaunchingContext(Integer pgPort) {
        this.pgPort = pgPort;
    }

    public String pgUrl() {
        return "jdbc:postgresql://localhost:" + pgPort + "/postgres";
    }
}
