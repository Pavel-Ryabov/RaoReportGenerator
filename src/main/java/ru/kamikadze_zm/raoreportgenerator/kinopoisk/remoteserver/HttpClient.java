package ru.kamikadze_zm.raoreportgenerator.kinopoisk.remoteserver;

import com.google.gson.Gson;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.kamikadze_zm.raoreportgenerator.MainApp;

public class HttpClient {

    private static final Logger LOG = LogManager.getLogger(HttpClient.class);

    private static final HttpClient INSTANCE = new HttpClient();

    private static final int CONNECT_TIMEOUT = 15000;

    private final String fullUrl;
    private final Gson gson;

    private HttpClient() {
        String serverPath = MainApp.SETTINGS.getServerSettings().getServerPath();
        String secretKey = MainApp.SETTINGS.getServerSettings().getSecretKey();
        this.fullUrl = serverPath + "movie-info/" + secretKey;
        this.gson = new Gson();
    }

    void sendPost(RemoteMovieInfo movieInfo) throws IOException {
        String json = gson.toJson(movieInfo);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        URL url = new URL(this.fullUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            out.write(data);
            out.flush();
            int code = connection.getResponseCode();
            if (HttpURLConnection.HTTP_CREATED != code) {
                LOG.warn("Sending movie info error. Http code: {}", code);
            }
        }
    }

    public static void post(RemoteMovieInfo movieInfo) {
        if (movieInfo.getId() == null) {
            return;
        }
        LOG.info("Sending movie info, id: {}", movieInfo.getId());
        CompletableFuture.runAsync(() -> {
            try {
                INSTANCE.sendPost(movieInfo);
                LOG.info("Movie info sended, id: {}", movieInfo.getId());
            } catch (IOException e) {
                LOG.warn("Sending movie info exception: ", e);
            }
        });
    }
}
