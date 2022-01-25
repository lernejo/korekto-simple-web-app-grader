package com.github.lernejo.korekto.grader.simple_web_app.parts;

import com.github.lernejo.korekto.grader.simple_web_app.LaunchingContext;
import com.github.lernejo.korekto.grader.simple_web_app.TodoApiClient;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.PartGrader;
import com.github.lernejo.korekto.toolkit.misc.Ports;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenExecutionHandle;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenExecutor;
import org.jetbrains.annotations.NotNull;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Part4Grader implements PartGrader<LaunchingContext> {

    private final TodoApiClient client;

    public Part4Grader(TodoApiClient client) {
        this.client = client;
    }

    @Override
    public @NotNull String name() {
        return "Part 4 - Instance-Id header";
    }

    @Override
    public @NotNull Double maxGrade() {
        return 2.0D;
    }

    @Override
    public @NotNull GradePart grade(LaunchingContext context) {
        if (context.compilationFailed) {
            return result(List.of("Ignored due to previous compilation failure"), 0.0D);
        }
        if (context.instanceIds.size() < 2) {
            return result(List.of("Ignored due to previous API call failures"), 0.0D);
        }

        Set<String> instanceIdsWithoutDuplicates = new HashSet<>(context.instanceIds);

        if (instanceIdsWithoutDuplicates.size() != 1) {
            return result(List.of("Instance-Id header changes at every call, whereas it should be the same during the life of the server"), maxGrade() / 2);
        }

        try
            (MavenExecutionHandle ignored = MavenExecutor.executeGoalAsync(context.getExercise(), context.getConfiguration().getWorkspace(),
                "org.springframework.boot:spring-boot-maven-plugin:2.5.5:run -Dspring-boot.run.jvmArguments='-Dserver.port=8085 -Dspring.datasource.url=" + context.pgUrl() + "'")) {

            Ports.waitForPortToBeListenedTo(8085, TimeUnit.SECONDS, 20L);

            Response<List<Todo>> getResponse = client.getTodos().execute();

            String instanceId = getResponse.headers().get(Part3Grader.INSTANCE_ID_HEADER);

            String previousInstanceId = instanceIdsWithoutDuplicates.iterator().next();
            if (previousInstanceId.equals(instanceId)) {
                return result(List.of("Instance-Id header should be different from one server run to another"), maxGrade() / 2);
            }
            return result(List.of(), maxGrade());
        } catch (IOException e) {
            return result(List.of("Fail to call server: " + e.getMessage()), 0.0D);
        }
    }
}
