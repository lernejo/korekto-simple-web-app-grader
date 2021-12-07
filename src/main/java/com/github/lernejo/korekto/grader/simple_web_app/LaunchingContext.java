package com.github.lernejo.korekto.grader.simple_web_app;

import com.github.lernejo.korekto.toolkit.misc.SubjectForToolkitInclusion;

@SubjectForToolkitInclusion
public class LaunchingContext {
    public final Integer pgPort;
    public boolean compilationFailed;
    public boolean testFailed;

    public LaunchingContext(Integer pgPort) {
        this.pgPort = pgPort;
    }
}
