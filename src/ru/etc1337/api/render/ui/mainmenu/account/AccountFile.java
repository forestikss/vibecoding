package ru.etc1337.api.render.ui.mainmenu.account;

import com.google.gson.*;
import lombok.NonNull;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import static ru.etc1337.Client.GSON;

public record AccountFile(File file) {
    public AccountFile(@NonNull File file) {
        this.file = Objects.requireNonNull(file, "File cannot be null");
    }

    public boolean read(AccountManager accounts) {
        if (!file.exists()) {
            return false;
        }

        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            JsonObject jsonObject = GSON.fromJson(bufferedReader, JsonObject.class);
            if (jsonObject == null) {
                System.err.println("Account file is empty or malformed: " + file.getAbsolutePath());
                return false;
            }

            JsonArray accountsArray = jsonObject.getAsJsonArray("accounts");
            if (accountsArray != null) {
                for (JsonElement accountElement : accountsArray) {
                    try {
                        JsonObject accountObj = accountElement.getAsJsonObject();
                        String name = accountObj.get("name").getAsString();
                        LocalDateTime date = LocalDateTime.parse(accountObj.get("creationDate").getAsString());
                        boolean favorite = accountObj.get("favorite").getAsBoolean();

                        Account account = new Account(date, name);
                        account.favorite(favorite);
                        accounts.addAccount(account);
                    } catch (JsonParseException | IllegalStateException | DateTimeParseException e) {
                        System.err.println("Error parsing account entry in " + file.getAbsolutePath() + ": " + e.getMessage());
                        // Continue to the next account even if one entry is malformed
                    }
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error reading account file: " + file.getAbsolutePath() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) { // Catch any other unexpected exceptions
            System.err.println("An unexpected error occurred while reading account file: " + file.getAbsolutePath() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean write(AccountManager accounts) {
        try {
            // Ensure parent directories exist
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                System.err.println("Failed to create parent directories for account file: " + file.getAbsolutePath());
                return false;
            }

            if (!file.exists() && !file.createNewFile()) {
                System.err.println("Failed to create account file: " + file.getAbsolutePath());
                return false;
            }

            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(getJsonObject(accounts), writer);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error writing account file: " + file.getAbsolutePath() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) { // Catch any other unexpected exceptions
            System.err.println("An unexpected error occurred while writing account file: " + file.getAbsolutePath() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @NonNull
    private JsonObject getJsonObject(AccountManager getAccountManager) {
        JsonObject json = new JsonObject();
        JsonArray accounts = new JsonArray();

        getAccountManager.forEach(account -> {
            JsonObject accObj = new JsonObject();
            accObj.addProperty("name", account.name());
            accObj.addProperty("creationDate", account.creationDate().toString());
            accObj.addProperty("favorite", account.favorite());
            accounts.add(accObj);
        });

        json.add("accounts", accounts);
        return json;
    }
}