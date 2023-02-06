package com.github.lernejo.korekto.grader.simple_web_app.parts;

import com.github.lernejo.korekto.grader.simple_web_app.LaunchingContext;
import com.github.lernejo.korekto.grader.simple_web_app.TodoApiClient;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.PartGrader;
import com.github.lernejo.korekto.toolkit.misc.Ports;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenExecutionHandle;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenExecutor;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import retrofit2.Response;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

public class Part3Grader implements PartGrader<LaunchingContext> {

    public static final String INSTANCE_ID_HEADER = "Instance-Id";

    private final TodoApiClient client;
    private final Random random = new Random();

    public Part3Grader(TodoApiClient client) {
        this.client = client;
    }

    @Override
    public @NotNull String name() {
        return "Part 3 - HTTP server API";
    }

    @Override
    public @NotNull Double maxGrade() {
        return 4.0D;
    }

    @Override
    public @NotNull GradePart grade(LaunchingContext context) {
        if (context.hasCompilationFailed()) {
            return result(List.of("Not trying to start server as compilation failed"), 0.0D);
        }

        String pgUrl = context.pgUrl();
        initdb(pgUrl);
        try
            (MavenExecutionHandle ignored = MavenExecutor.executeGoalAsync(context.getExercise(), context.getConfiguration().getWorkspace(),
                "org.springframework.boot:spring-boot-maven-plugin:2.5.5:run -Dspring-boot.run.jvmArguments='-Dserver.port=8085 -Dspring.datasource.url=" + pgUrl + "'")) {

            Ports.waitForPortToBeListenedTo(8085, TimeUnit.SECONDS, 20L);

            double grade = maxGrade();
            List<String> errors = new ArrayList<>();
            Response<ResponseBody> postResponse = client.addTodo(new Todo("message1", "author1")).execute();
            storeInstanceIdHeader(context, postResponse);

            int callNbr = random.nextInt(6) + 2;

            try {
                if (!postResponse.isSuccessful()) {
                    grade -= maxGrade() / 2;
                    errors.add("Unsuccessful response of POST /api/todo: " + postResponse.code());
                } else {
                    for (int i = 0; i < callNbr; i++) {
                        Response<ResponseBody> otherResponse = client.addTodo(new Todo("message" + i, "author2")).execute();
                        storeInstanceIdHeader(context, otherResponse);
                    }
                    context.postedTodosNbr = callNbr + 1;
                }
            } catch (RuntimeException e) {
                grade -= maxGrade() / 2;
                errors.add("Unsuccessful response of POST /api/todo: " + e.getMessage());
            }

            try {
                Response<List<Todo>> getResponse = client.getTodos().execute();
                storeInstanceIdHeader(context, getResponse);

                if (!getResponse.isSuccessful()) {
                    grade -= maxGrade() / 2;
                    errors.add("Unsuccessful response of GET /api/todo: " + postResponse.code());
                } else {
                    List<Todo> todos = getResponse.body();
                    if (todos == null) {
                        grade -= maxGrade() / 3;
                        errors.add("Expecting of GET /api/todo to return a list of size " + context.postedTodosNbr + " but was an empty response");
                    } else if (todos.size() != context.postedTodosNbr) {
                        grade -= maxGrade() / 3;
                        errors.add("Expecting of GET /api/todo to return a list of size " + context.postedTodosNbr + " but was: " + todos.size());
                    }
                }
            } catch (RuntimeException e) {
                grade -= maxGrade() / 2;
                errors.add("Unsuccessful response of GET /api/todo: " + e.getMessage());
            }

            return result(errors, grade);
        } catch (CancellationException e) {
            return result(List.of("Server failed to start within 20 sec."), 0.0D);
        } catch (RuntimeException e) {
            return result(List.of("Unwanted error during API invocation: " + e.getMessage()), 0.0D);
        } catch (IOException e) {
            return result(List.of("Fail to call server: " + e.getMessage()), 0.0D);
        } finally {
            Ports.waitForPortToBeFreed(8085, TimeUnit.SECONDS, 5L);
        }
    }

    private void storeInstanceIdHeader(LaunchingContext context, Response<?> response) {
        String instanceId = response.headers().get(INSTANCE_ID_HEADER);
        if (instanceId != null) {
            context.instanceIds.add(instanceId);
        }
    }

    private void initdb(String pgUrl) {
        try (Connection connection = DriverManager.getConnection(pgUrl, "postgres", "example");
             PreparedStatement dropStm = connection.prepareStatement("DROP SCHEMA public CASCADE");
             PreparedStatement createStm = connection.prepareStatement("CREATE SCHEMA public")) {
            dropStm.execute();
            createStm.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to reset db");
        }
    }
}
