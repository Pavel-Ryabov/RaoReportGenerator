package ru.kamikadze_zm.raoreportgenerator.settings;

import java.io.Serializable;

public class ServerSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_SERVER_PATH = "http://rao.kamikadze-zm.ru/";
    private static final String DEFAULT_SECRET_KEY = "secret-key";

    private String serverPath = DEFAULT_SERVER_PATH;
    private String secretKey = DEFAULT_SECRET_KEY;

    public ServerSettings() {
    }

    public ServerSettings(String serverPath, String secretKey) {
        this.serverPath = serverPath;
        this.secretKey = secretKey;
    }

    public String getServerPath() {
        return serverPath;
    }

    public void setServerPath(String serverPath) {
        if (!serverPath.endsWith("/")) {
            serverPath = serverPath + "/";
        }
        this.serverPath = serverPath;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
