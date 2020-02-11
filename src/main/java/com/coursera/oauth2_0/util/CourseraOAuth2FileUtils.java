package com.coursera.oauth2_0.util;

import com.coursera.oauth2_0.exception.CreateClientAppException;
import com.coursera.oauth2_0.model.CourseraOAuth2Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

public class CourseraOAuth2FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(CourseraOAuth2FileUtils.class);

    private static final String TOKEN_CACHE_DIR = System.getProperty("user.home") + File.separator + ".coursera";
    private static final String CONFIG_FILE ="coaclient.csv";
    private static final String TOKEN_FILE_SUFFIX = "_aout2.csv";
    private static final String SEPARATOR = ",";

    public static void writeClientConfigToFile(String clientName,
                                               String clientId,
                                               String secretKey,
                                               String scopes) throws CreateClientAppException {
        File cacheDir = new File(TOKEN_CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }

        boolean isNewFile = false;
        File config = new File(TOKEN_CACHE_DIR + File.separator + CONFIG_FILE);
        if (!config.exists()) {
            isNewFile = true;
        }

        try (FileWriter csvWriter = new FileWriter(config, true)) {
            if (isNewFile) {
                csvWriter.append(CourseraOAuth2Constants.CLIENT_APP_NAME);
                csvWriter.append(SEPARATOR);
                csvWriter.append(CourseraOAuth2Constants.CLIENT_ID_KEY);
                csvWriter.append(SEPARATOR);
                csvWriter.append(CourseraOAuth2Constants.CLIENT_SECRET_KEY);
                csvWriter.append(SEPARATOR);
                csvWriter.append(CourseraOAuth2Constants.SCOPE_PROFILE);
                csvWriter.append("\n");
            }

            csvWriter.append(clientName);
            csvWriter.append(SEPARATOR);
            csvWriter.append(clientId);
            csvWriter.append(SEPARATOR);
            csvWriter.append(secretKey);
            csvWriter.append(SEPARATOR);
            csvWriter.append(scopes);
            csvWriter.append("\n");

            csvWriter.flush();
        } catch (IOException e) {
            throw new CreateClientAppException("Error write new client config to file: " + e.getMessage());
        }
    }

    public static void saveAuthTokens(String clientName,
                                      String refreshToken,
                                      String accessToken,
                                      String expiredTime) {
        File tokensFile = new File(TOKEN_CACHE_DIR + File.separator + clientName + TOKEN_FILE_SUFFIX);
        if (tokensFile.exists()) {
            tokensFile.delete();
        }

        try (FileWriter csvWriter = new FileWriter(tokensFile)) {
            csvWriter.append(CourseraOAuth2Constants.REFRESH_TOKEN_KEY);
            csvWriter.append(SEPARATOR);
            csvWriter.append(CourseraOAuth2Constants.ACCESS_TOKEN_KEY);
            csvWriter.append(SEPARATOR);
            csvWriter.append(CourseraOAuth2Constants.EXPIRES_IN);
            csvWriter.append("\n");
            csvWriter.append(Base64Utils.encodeToString(refreshToken.getBytes()));
            csvWriter.append(SEPARATOR);
            csvWriter.append(Base64Utils.encodeToString(accessToken.getBytes()));
            csvWriter.append(SEPARATOR);
            csvWriter.append(expiredTime);
            csvWriter.append("\n");

            csvWriter.flush();
        } catch (IOException e) {
            logger.error("Error while saving authentication tokens to file: {}", e.getMessage());
        }
    }

    public static Map<String, String> getAuthTokensFromFile(String clientAppName) {
        Map<String, String> authTokens = new HashMap<>();
        File tokensFile = new File(TOKEN_CACHE_DIR + File.separator + clientAppName + TOKEN_FILE_SUFFIX);
        if (!tokensFile.exists()) {
            logger.error("File with {} tokens not found in path: {}. Please try to generate auth tokens.",
                    clientAppName,
                    tokensFile.toPath());
            return authTokens;
        }

        try (BufferedReader csvReader = new BufferedReader(new FileReader(tokensFile.getPath()))) {
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] splitRow = row.split(SEPARATOR);
                if (!splitRow[0].equals(CourseraOAuth2Constants.REFRESH_TOKEN_KEY)) {
                    byte[] decodedRefreshToken = Base64Utils.decode(splitRow[0].getBytes());
                    byte[] decodedAccessToken = Base64Utils.decode(splitRow[1].getBytes());

                    authTokens.put(CourseraOAuth2Constants.REFRESH_TOKEN_KEY, new String(decodedRefreshToken));
                    authTokens.put(CourseraOAuth2Constants.ACCESS_TOKEN_KEY, new String(decodedAccessToken));
                    authTokens.put(CourseraOAuth2Constants.EXPIRES_IN, splitRow[2]);
                    return authTokens;
                }
            }
        } catch (IOException e) {
            logger.error("Error while read tokens file: {}", e.getMessage());
        }
        return authTokens;
    }

    public static void deleteClientConfig(String clientName) {
        try {
            File file = new File(TOKEN_CACHE_DIR + File.separator + CONFIG_FILE);
            List<String> out;
            try (Stream<String> lines = Files.lines(file.toPath())) {
                out = lines.filter(line -> !line.contains(clientName))
                        .collect(Collectors.toList());
            }

            Files.write(file.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            File tokensFile = new File(TOKEN_CACHE_DIR + File.separator + clientName + TOKEN_FILE_SUFFIX);
            if (tokensFile.exists()) {
                Files.delete(tokensFile.toPath());
            }
            logger.info("Client {} successfully deleted.", clientName);
        } catch (IOException e) {
            logger.error(
                    "Error while delete {} config in file: {}", clientName, e.getMessage());
        }
    }

    public static CourseraOAuth2Config getClientConfigByNameOrId(String clientIdentifier) {
        try (BufferedReader csvReader = new BufferedReader(new FileReader(
                TOKEN_CACHE_DIR + File.separator + CONFIG_FILE))) {
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] configRow = row.split(SEPARATOR);
                if (!configRow[0].equals(CourseraOAuth2Constants.CLIENT_APP_NAME) &&
                        (configRow[0].equals(clientIdentifier) || configRow[1].equals(clientIdentifier))) {
                    return createConfig(configRow);
                }
            }
        } catch (IOException e) {
            logger.error(
                    "Error while read config file in path: " + TOKEN_CACHE_DIR + File.separator + CONFIG_FILE +
                            ". Please add application before start generating tokens");
        }
        return null;
    }

    public static List<CourseraOAuth2Config> getClientsFromConfigFile() {
        List<CourseraOAuth2Config> clientConfigs = new ArrayList<>();
        try (BufferedReader csvReader = new BufferedReader(new FileReader(
                TOKEN_CACHE_DIR + File.separator + CONFIG_FILE))) {
            String row;
            while ((row = csvReader.readLine()) != null) {
                String[] configRow = row.split(SEPARATOR);
                if (!configRow[0].equals(CourseraOAuth2Constants.CLIENT_APP_NAME)) {
                    clientConfigs.add(createConfig(configRow));
                }
            }
        } catch (IOException e) {
            logger.error(
                    "Error while read config file in path: " + TOKEN_CACHE_DIR + File.separator + CONFIG_FILE +
                            ". Please add application before start generating tokens");
        }

        return clientConfigs;
    }

    private static CourseraOAuth2Config createConfig(String[] configRow) {
        return new CourseraOAuth2Config(
                configRow[0],
                configRow[1],
                configRow[2],
                configRow[3]);
    }
}
