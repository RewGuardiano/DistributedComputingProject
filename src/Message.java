/**
 * Represents a message with a unique ID and content.
 */
class Message {
    private final String id;
    private final String content;

    public Message(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Content: " + content;
    }
}