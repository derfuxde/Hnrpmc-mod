package org.emil.hnrpmc.doc.managers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.emil.hnrpmc.doc.HNDoc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.*;

import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

@OnlyIn(Dist.CLIENT)
public class Loader {
    Minecraft mc = Minecraft.getInstance();
    Path localPath = mc.gameDirectory.toPath().resolve("HNDoc");

    public Map<String, List<String>> Paths = new HashMap<>();
    public Map<String, String> Files = new HashMap<>();
    public HttpClient client = HttpClient.newHttpClient();

    public Loader() {
        loadFromGithub();
    }

    public Map<String, ?> getDoc() {
        return new HashMap<>();
    }

    public void loadFromGithub() {
        fetchDirectory("");
    }

    private void fetchDirectory(String path) {
        try {

            String fullUrl = "https://api.github.com/repositories/1136755582/contents/src/Doc" + path;
            HttpRequest currentRequest = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("Authorization", "Bearer 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") // Dein Token
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(currentRequest, HttpResponse.BodyHandlers.ofString());

            try {
                if (response.statusCode() == 200) {
                    String jsonResponse = response.body();
                    Gson gson = HNDoc.getInstance().getGson();
                    Type listType = new TypeToken<Collection<Map<String, ?>>>() {
                    }.getType();
                    Collection<Map<String, ?>> items = gson.fromJson(jsonResponse, listType);

                    List<String> filesInThisFolder = new ArrayList<>();

                    for (Map<String, ?> item : items) {
                        String itemPath = ((String) item.get("path")).replace("src/Doc", "");
                        String type = (String) item.get("type");

                        if ("dir".equals(type)) {
                            fetchDirectory(itemPath);
                        } else {
                            filesInThisFolder.add(itemPath);

                            String content = fetchFileContent(itemPath);
                            Files.put(itemPath, content);
                        }
                    }

                    Paths.put(path.replace("src/Doc", ""), filesInThisFolder);
                }
            } catch (JsonSyntaxException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            System.out.println("Fehler bei: " + path + " -> " + e);
        }
    }

    private String fetchFileContent(String path) {
        try {
            // Der Pfad muss an die Basis-URL angehängt werden
            String fileUrl = "https://api.github.com/repositories/1136755582/contents/src/Doc" + path;

            HttpRequest contentRequest = HttpRequest.newBuilder()
                    .uri(URI.create(fileUrl))
                    .header("Authorization", "Bearer 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") // Hier dein Token einsetzen
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(contentRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Gson gson = HNDoc.getInstance().getGson();
                Map<String, Object> jsonMap = gson.fromJson(response.body(), new TypeToken<Map<String, Object>>(){}.getType());

                String base64Content = (String) jsonMap.get("content");

                if (base64Content != null) {
                    base64Content = base64Content.replace("\n", "").replace("\r", "");
                    byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
                    return new String(decodedBytes, StandardCharsets.UTF_8);
                }
            }
            return "Fehler: HTTP " + response.statusCode();
        } catch (Exception e) {
            e.printStackTrace();
            return "Fehler: " + e.getMessage();
        }
    }
}
