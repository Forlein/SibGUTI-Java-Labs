package common;

/**
 * Общая модель уведомления для заданий 2, 4, 7, 13.
 */
public class Notification {
    private final String recipient;
    private final String subject;
    private final String body;

    public Notification(String recipient, String subject, String body) {
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "\"" + subject + "\" -> " + recipient;
    }
}
