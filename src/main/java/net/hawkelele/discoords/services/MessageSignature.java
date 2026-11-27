package net.hawkelele.discoords.services;

// Who posted, written in the text when the message author can't say it
public final class MessageSignature {
    private MessageSignature() {
    }

    public static String of(String text, String player) {
        return text + " ~ *[" + player + "]*";
    }
}
