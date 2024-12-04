package com.github.lernejo.korekto.grader.simple_web_app;

import com.github.lernejo.korekto.grader.simple_web_app.parts.Part3Grader;
import com.github.lernejo.korekto.grader.simple_web_app.parts.Part4Grader;
import com.github.lernejo.korekto.grader.simple_web_app.parts.Part7Grader;
import com.github.lernejo.korekto.toolkit.Grader;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.PartGrader;
import com.github.lernejo.korekto.toolkit.misc.SubjectForToolkitInclusion;
import com.github.lernejo.korekto.toolkit.partgrader.GitHubActionsPartGrader;
import com.github.lernejo.korekto.toolkit.partgrader.MavenCompileAndTestPartGrader;
import com.github.lernejo.korekto.toolkit.thirdparty.docker.MappedPortsContainer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Collection;
import java.util.List;

@SubjectForToolkitInclusion
public class WebAppSpringGrader implements Grader<LaunchingContext> {

    private final Logger logger = LoggerFactory.getLogger(WebAppSpringGrader.class);

    private final Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://localhost:8085/")
        .addConverterFactory(JacksonConverterFactory.create())
        .build();

    public final TodoApiClient client = retrofit.create(TodoApiClient.class);

    private final MappedPortsContainer postgresContainer;

    @NotNull
    @Override
    public String name() {
        return "Simple web app with Spring";
    }

    public WebAppSpringGrader() {
        logger.info("Waiting for PG to start");
        postgresContainer = new MappedPortsContainer(
            "postgres:14.0-alpine",
            5432,
            (sp, sps) -> "PG up on :" + sp)
            .withEnv("POSTGRES_PASSWORD", "example")
            .startAndWaitForServiceToBeUp();
    }

    @Override
    public void close() {
        postgresContainer.stop();
    }

    @Override
    @NotNull
    public String slugToRepoUrl(@NotNull String slug) {
        return "https://github.com/" + slug + "/web_app_spring_training";
    }

    @Override
    public boolean needsWorkspaceReset() {
        return true;
    }

    @Override
    @NotNull
    public LaunchingContext gradingContext(@NotNull GradingConfiguration configuration) {
        return new LaunchingContext(configuration, postgresContainer.getServicePort(), client);
    }

    public Collection<PartGrader<LaunchingContext>> graders() {
        return List.of(
            new MavenCompileAndTestPartGrader<>("Part 1 - Compilation & Tests", 2.0D),
            new GitHubActionsPartGrader<>("Part 2 - CI", 1.0D),
            new Part3Grader("Part 3 - HTTP server API", 4.0D),
            new Part4Grader("Part 4 - Instance-Id header", 2.0D),
            new Part7Grader("Part 5 - Database persistence", 4.0D)
        );
    }
}
