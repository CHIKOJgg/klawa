package org.example.aiassistantklawa.task.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import org.springframework.util.Assert;

@Entity
public class Request {
    @EmbeddedId
    private RequestId requestId;
    private String title;
    private String body;
    private String topic;

    public Request(String title, String body, String topic) {
        Assert.notNull(title, "title must be non empty");
        Assert.notNull(body, "body must be non empty");
        Assert.notNull(topic, "topic must be non empty");
        this.requestId = new RequestId();
        this.title = title;
        this.body = body;
        this.topic = topic;
    }

    public Request() {

    }
}
