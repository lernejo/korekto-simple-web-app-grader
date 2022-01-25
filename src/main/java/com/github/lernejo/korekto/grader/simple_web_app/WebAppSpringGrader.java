package com.github.lernejo.korekto.grader.simple_web_app;

import com.github.lernejo.korekto.grader.simple_web_app.parts.*;
import com.github.lernejo.korekto.toolkit.*;
import com.github.lernejo.korekto.toolkit.misc.HumanReadableDuration;
import com.github.lernejo.korekto.toolkit.misc.Ports;
import com.github.lernejo.korekto.toolkit.misc.SubjectForToolkitInclusion;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitNature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SubjectForToolkitInclusion
public class WebAppSpringGrader implements Grader<LaunchingContext> {

    private final Logger logger = LoggerFactory.getLogger(WebAppSpringGrader.class);

    private final Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://localhost:8085/")
        .addConverterFactory(JacksonConverterFactory.create())
        .build();

    public final TodoApiClient client = retrofit.create(TodoApiClient.class);

    private final GenericContainer genericContainer;

    public WebAppSpringGrader() {
        this.genericContainer = new GenericContainer("postgres:14.0-alpine");
        genericContainer.addEnv("POSTGRES_PASSWORD", "example");
        genericContainer.addExposedPort(5432);
        try {
            genericContainer.start();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Unable to use Docker, make sure the Docker engine is started", e);
        }
        logger.info("Waiting for PG to boot");
        Ports.waitForPortToBeListenedTo(genericContainer.getMappedPort(5432), TimeUnit.SECONDS, 20L);
        logger.info("PG up");
    }

    @Override
    public void close() {
        genericContainer.stop();
    }

    @Override
    public String slugToRepoUrl(String slug) {
        return "https://github.com/" + slug + "/web_app_spring_training";
    }

    @Override
    public boolean needsWorkspaceReset() {
        return true;
    }

    @Override
    public LaunchingContext gradingContext(GradingConfiguration configuration) {
        return new LaunchingContext(configuration, genericContainer.getMappedPort(5432));
    }

    @Override
    public void run(LaunchingContext context) {
        context.getGradeDetails().getParts().addAll(grade(context));
    }

    private Collection<? extends GradePart> grade(LaunchingContext context) {
        return graders().stream()
            .map(g -> applyPartGrader(context, g))
            .collect(Collectors.toList());
    }

    private GradePart applyPartGrader(LaunchingContext context, PartGrader g) {
        long startTime = System.currentTimeMillis();
        try {
            return g.grade(context);
        } finally {
            logger.debug(g.name() + " in " + HumanReadableDuration.toString(System.currentTimeMillis() - startTime));
        }
    }

    private Collection<? extends PartGrader> graders() {
        return List.of(
            new Part1Grader(),
            new Part2Grader(),
            new Part3Grader(client),
            new Part4Grader(client),
            new Part5Grader()
        );
    }
}
