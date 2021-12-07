package com.github.lernejo.korekto.grader.simple_web_app.parts;

import com.github.lernejo.korekto.grader.simple_web_app.LaunchingContext;
import com.github.lernejo.korekto.grader.simple_web_app.TodoApiClient;
import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.misc.Ports;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenExecutionHandle;
import com.github.lernejo.korekto.toolkit.thirdparty.maven.MavenExecutor;
import okhttp3.ResponseBody;
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

public class Part3Grader implements PartGrader {

    private final TodoApiClient client;
    private final Random random = new Random();

    public Part3Grader(TodoApiClient client) {
        this.client = client;
    }

    @Override
    public String name() {
        return "Part 3 - HTTP server API";
    }

    @Override
    public Double maxGrade() {
        return 4.0D;
    }

    @Override
    public GradePart grade(GradingConfiguration configuration, Exercise exercise, LaunchingContext context, GitContext gitContext) {
        if (context.compilationFailed) {
            return result(List.of("Not trying to start server as compilation failed"), 0.0D);
        }

        String pgUrl = context.pgUrl();
        initdb(pgUrl);
        try
            (MavenExecutionHandle handle = MavenExecutor.executeGoalAsync(exercise, configuration.getWorkspace(),
                "org.springframework.boot:spring-boot-maven-plugin:2.5.5:run -Dspring-boot.run.jvmArguments='-Dserver.port=8085 -Dspring.datasource.url=" + pgUrl + "'")) {

            Ports.waitForPortToBeListenedTo(8085, TimeUnit.SECONDS, 20L);

            double grade = maxGrade();
            List<String> errors = new ArrayList<>();
            Response<ResponseBody> postResponse = client.addTodo(new Todo("message1", "author1")).execute();
            int callNbr = random.nextInt(6) + 2;

            if (!postResponse.isSuccessful()) {
                grade -= maxGrade() / 2;
                errors.add("Unsuccessful response of POST /api/todo: " + postResponse.code());
            } else {
                for (int i = 0; i < callNbr; i++) {
                    client.addTodo(new Todo("message" + i, "author2")).execute();
                }
                context.postedTodosNbr = callNbr + 1;
            }

            Response<List<Todo>> getResponse = client.getTodos().execute();

            if (!getResponse.isSuccessful()) {
                grade -= maxGrade() / 2;
                errors.add("Unsuccessful response of GET /api/todo: " + postResponse.code());
            } else {
                List<Todo> todos = getResponse.body();
                if (todos.size() != context.postedTodosNbr) {
                    grade -= maxGrade() / 3;
                    errors.add("Expecting of GET /api/todo to return a list of size " + context.postedTodosNbr + " but was: " + todos.size());
                }
            }

            return result(errors, grade);
        } catch (CancellationException e) {
            return result(List.of("Server failed to start within 20 sec."), 0.0D);
        } catch (RuntimeException e) {
            return result(List.of("Unwanted error during API invocation: " + e.getMessage()), 0.0D);
        } catch (IOException e) {
            return result(List.of("Fail to call server: " + e.getMessage()), 0.0D);
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
