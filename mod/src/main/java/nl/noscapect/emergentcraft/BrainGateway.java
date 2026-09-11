package nl.noscapect.emergentcraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.noscapect.emergentcraft.brain.BrainDecision;
import nl.noscapect.emergentcraft.perception.PerceptionModels.AgentPerception;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Only transport: no game objects cross this thread boundary. */
public final class BrainGateway {
    private static final Gson GSON=new GsonBuilder().serializeNulls().create();
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final URI endpoint = URI.create(System.getProperty("emergentcraft.brainUrl", "http://127.0.0.1:3847") + "/v1/decide");
    public CompletableFuture<Optional<BrainDecision>> decide(AgentPerception perception) {
        HttpRequest request = HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(125)).header("content-type", "application/json").POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(perception))).build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> { if (response.statusCode()!=200) return Optional.<BrainDecision>empty(); BrainDecision decision=GSON.fromJson(response.body(),BrainDecision.class); return decision==null||decision.actionId()==null?Optional.<BrainDecision>empty():Optional.of(decision); }).exceptionally(error -> { EmergentCraftMod.LOGGER.debug("Brain request unavailable: {}", error.toString()); return Optional.empty(); });
    }
    public CompletableFuture<Optional<String>> diagnostic(String route,Object payload) { HttpRequest request=HttpRequest.newBuilder(URI.create(endpoint.toString().replace("/v1/decide",route))).timeout(Duration.ofSeconds(15)).header("content-type","application/json").POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(payload))).build(); return client.sendAsync(request,HttpResponse.BodyHandlers.ofString()).thenApply(response->response.statusCode()==200?Optional.of(response.body()):Optional.<String>empty()).exceptionally(error->{EmergentCraftMod.LOGGER.debug("Brain diagnostic unavailable: {}",error.toString());return Optional.<String>empty();}); }
    public void appendFactualEvent(UUID agentId, long epoch, String kind, String text) {
        appendFactualEvent(agentId,epoch,kind,text,java.util.Map.of());
    }
    public void appendFactualEvent(UUID agentId, long epoch, String kind, String text,java.util.Map<String,Object> metadata) {
        java.util.Map<String,Object> payload=new java.util.LinkedHashMap<>(); payload.put("agentId",agentId.toString()); payload.put("epoch",epoch); payload.put("kind",kind); payload.put("text",text); if(!metadata.isEmpty()) payload.put("metadata",metadata); String json = GSON.toJson(payload);
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint.toString().replace("/v1/decide", "/v1/event"))).timeout(Duration.ofSeconds(10)).header("content-type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
        client.sendAsync(request, HttpResponse.BodyHandlers.discarding()).thenAccept(response -> { if (response.statusCode() != 204) EmergentCraftMod.LOGGER.warn("Brain rejected factual event for {}: HTTP {}", agentId, response.statusCode()); }).exceptionally(error -> { EmergentCraftMod.LOGGER.warn("Could not append factual memory for {}: {}", agentId, error.toString()); return null; });
    }
}
