package com.github.lernejo.korekto.grader.simple_web_app.parts;

import com.github.lernejo.korekto.grader.simple_web_app.LaunchingContext;
import com.github.lernejo.korekto.toolkit.Exercise;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.thirdparty.git.GitContext;

import java.sql.*;
import java.util.List;

public class Part5Grader implements PartGrader {

    @Override
    public String name() {
        return "Part 5 - Database persistence";
    }

    @Override
    public Double maxGrade() {
        return 4.0D;
    }

    @Override
    public GradePart grade(GradingConfiguration configuration, Exercise exercise, LaunchingContext context, GitContext gitContext) {
        if (context.compilationFailed) {
            return result(List.of("Ignored due to previous compilation failure"), 0.0D);
        }
        if (context.postedTodosNbr == null) {
            return result(List.of("Ignored due to previous API call failures"), 0.0D);
        }

        int lineCount = 0;

        try (Connection connection = DriverManager.getConnection(context.pgUrl(), "postgres", "example");
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM public.todo")) {
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                lineCount++;
            }
        } catch (SQLException e) {
            return result(List.of("Unable to SELECT from `public.todo` table: " + e.getMessage()), 0.0D);
        }
        if (lineCount != context.postedTodosNbr) {
            return result(List.of("Expected to find " + context.postedTodosNbr + " todos inserted in table `public.todo`, but found: " + lineCount), 0.0D);
        }
        return result(List.of(), maxGrade());
    }
}
