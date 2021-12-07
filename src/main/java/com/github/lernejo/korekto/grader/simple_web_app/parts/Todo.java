package com.github.lernejo.korekto.grader.simple_web_app.parts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Todo(String message, String author) {
}
