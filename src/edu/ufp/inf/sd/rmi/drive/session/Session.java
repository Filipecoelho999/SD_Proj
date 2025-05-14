package edu.ufp.inf.sd.rmi.drive.session;

import edu.ufp.inf.sd.rmi.drive.server.SubjectRI;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Session implements Serializable {
    private final String username;
    private final LocalDateTime loginTimestamp;
    private final SubjectRI subject;

    public Session(String username, SubjectRI subject) {
        this.username = username;
        this.subject = subject;
        this.loginTimestamp = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getLoginTimestamp() {
        return loginTimestamp;
    }

    public SubjectRI getSubject() {
        return subject;
    }
}
