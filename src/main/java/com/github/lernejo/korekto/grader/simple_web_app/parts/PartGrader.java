package com.github.lernejo.korekto.grader.simple_web_app.parts;

import java.util.List;

import com.github.lernejo.korekto.grader.simple_web_app.LaunchingContext;
import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.misc.SubjectForToolkitInclusion;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;

@SubjectForToolkitInclusion
public interface PartGrader {

    String name();

    default Double maxGrade() {
        return null;
    }

    default double minGrade() {
        return 0.0D;
    }

    GradePart grade(GradingConfiguration configuration, Exercise exercise, LaunchingContext context, GitContext gitContext);

    default GradePart result(List<String> explanations, double grade) {
        return new GradePart(name(), Math.min(Math.max(minGrade(), grade), maxGrade()), maxGrade(), explanations);
    }
}
