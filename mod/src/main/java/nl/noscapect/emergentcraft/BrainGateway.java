package nl.noscapect.emergentcraft;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
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
    public void appendFactualEvent(UUID agentId, long epoch, String kind, String text) {
        String json = "{\"agentId\":\"" + agentId + "\",\"epoch\":" + epoch + ",\"kind\":\"" + json(kind) + "\",\"text\":\"" + json(text) + "\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint.toString().replace("/v1/decide", "/v1/event"))).timeout(Duration.ofSeconds(10)).header("content-type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
        client.sendAsync(request, HttpResponse.BodyHandlers.discarding()).thenAccept(response -> { if (response.statusCode() != 204) EmergentCraftMod.LOGGER.warn("Brain rejected factual event for {}: HTTP {}", agentId, response.statusCode()); }).exceptionally(error -> { EmergentCraftMod.LOGGER.warn("Could not append factual memory for {}: {}", agentId, error.toString()); return null; });
    }
    private static String json(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " "); }
}
