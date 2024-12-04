package com.github.lernejo.korekto.grader.simple_web_app.parts;

import com.github.lernejo.korekto.grader.simple_web_app.LaunchingContext;
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

public record Part4Grader(String name, Double maxGrade) implements PartGrader<LaunchingContext> {

    @Override
    public @NotNull GradePart grade(LaunchingContext context) {
        if (context.hasCompilationFailed()) {
            return result(List.of("Ignored due to previous compilation failure"), 0.0D);
        }
        if (context.instanceIds.size() < 2) {
            return result(List.of("Ignored due to previous API call failures"), 0.0D);
        }

        Set<String> instanceIdsWithoutDuplicates = new HashSet<>(context.instanceIds);

        if (instanceIdsWithoutDuplicates.contains("<none>")) {
            return result(List.of("No header `" + Part3Grader.INSTANCE_ID_HEADER + "` found in request response"), 0.0D);
        }
        if (instanceIdsWithoutDuplicates.size() != 1) {
            return result(List.of("Instance-Id header changes at every call, whereas it should be the same during the life of the server"), maxGrade() / 2);
        }

        try
            (MavenExecutionHandle ignored = MavenExecutor.executeGoalAsync(context.getExercise(), context.getConfiguration().getWorkspace(),
                "org.springframework.boot:spring-boot-maven-plugin:3.4.0:run -Dspring-boot.run.jvmArguments='-Dserver.port=8085 -Dspring.datasource.url=" + context.pgUrl() + "'")) {

            Ports.waitForPortToBeListenedTo(8085, TimeUnit.SECONDS, LaunchingContext.serverStartTime());

            Response<List<Todo>> getResponse = context.client.getTodos().execute();

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
