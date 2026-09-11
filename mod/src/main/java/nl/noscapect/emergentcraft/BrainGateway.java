package nl.noscapect.emergentcraft;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Only transport: no game objects cross this thread boundary. */
public final class BrainGateway {
    private static final Pattern ACTION = Pattern.compile("\\\"actionId\\\"\\s*:\\s*\\\"([^\\\"]{1,128})\\\"");
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final URI endpoint = URI.create(System.getProperty("emergentcraft.brainUrl", "http://127.0.0.1:3847") + "/v1/decide");
    public CompletableFuture<Optional<String>> decide(String compactSnapshot) {
        HttpRequest request = HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(125)).header("content-type", "application/json").POST(HttpRequest.BodyPublishers.ofString(compactSnapshot)).build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> { if (response.statusCode()!=200) return Optional.<String>empty(); Matcher matcher=ACTION.matcher(response.body()); return matcher.find()?Optional.of(matcher.group(1)):Optional.<String>empty(); }).exceptionally(error -> { EmergentCraftMod.LOGGER.debug("Brain request unavailable: {}", error.toString()); return Optional.empty(); });
    }
}
