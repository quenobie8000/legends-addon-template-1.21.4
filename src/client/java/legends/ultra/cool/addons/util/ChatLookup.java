package legends.ultra.cool.addons.util;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatLookup {
    private ChatLookup() {
    }

    private static boolean initialized = false;
    public static String result = "";


    public static String getResult() {
        boolean completed = ChatLookup.consumeExact("completed");
        boolean failed = ChatLookup.consumeExact("failed");

        if (completed) {
            result = "completed";
        } else if (failed) {
            result = "failed";
        }

        return result;
    }

    private static final java.util.Set<String> triggers = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private static final java.util.Set<String> fired = java.util.concurrent.ConcurrentHashMap.newKeySet();


    /**
     * Call this before init() to add what you want to detect.
     */
    public static void watchExact(String message) {
        triggers.add(message.toLowerCase());
    }

    /**
     * Register the chat listener ONCE.
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            String plain = message.getString().toLowerCase();

            // exact-match triggers
            if (triggers.contains(plain)) {
                fired.add(plain);
                System.out.println(getResult());
            }
        });
    }

    public static boolean consumeExact(String message) {
        if (message == null) return false;
        return fired.remove(message.toLowerCase());
    }

}
