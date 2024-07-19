// Decompiled with: CFR 0.152
// Class Version: 17
package dev.realme.ash.api.account.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.realme.ash.Ash;
import dev.realme.ash.api.account.type.MinecraftAccount;
import dev.realme.ash.api.account.type.impl.CrackedAccount;
import dev.realme.ash.api.account.type.impl.MicrosoftAccount;
import dev.realme.ash.api.file.ConfigFile;
import dev.realme.ash.init.Managers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

public class AccountFile
        extends ConfigFile {
    public AccountFile(Path dir, String path) {
        super(dir, path);
    }

    public AccountFile(Path dir) {
        this(dir, "accounts");
    }

    @Override
    public void save() {
        try {
            Path filepath = this.getFilepath();
            if (!Files.exists(filepath, new LinkOption[0])) {
                Files.createFile(filepath, new FileAttribute[0]);
            }
            this.write(filepath, this.saveAs());
        } catch (IOException e) {
            Ash.error("Could not save file for accounts.json!");
            e.printStackTrace();
        }
    }

    @Override
    public void load() {
        try {
            Path filepath = this.getFilepath();
            if (Files.exists(filepath, new LinkOption[0])) {
                this.readFrom(this.read(filepath));
            }
        } catch (IOException e) {
            Ash.error("Could not read file for accounts.json!");
            e.printStackTrace();
        }
    }

    protected String saveAs() {
        JsonArray array = new JsonArray();
        for (MinecraftAccount account : Managers.ACCOUNT.getAccounts()) {
            try {
                array.add(account.toJSON());
            } catch (RuntimeException e) {
                Ash.error(e.getMessage());
            }
        }
        return this.serialize(array);
    }

    protected void readFrom(String content) {
        JsonArray json = this.parseArray(content);
        if (json == null) {
            return;
        }
        for (JsonElement element : json.asList()) {
            if (!(element instanceof JsonObject object)) continue;
            MinecraftAccount account = null;
            if (object.has("email") && object.has("password")) {
                account = new MicrosoftAccount(object.get("email").getAsString(), object.get("password").getAsString());
                if (object.has("username")) {
                    ((MicrosoftAccount) account).setUsername(object.get("username").getAsString());
                }
            } else if (object.has("token")) {
                if (!object.has("username")) {
                    Ash.error("Browser account does not have a username set?");
                    continue;
                }
                account = new MicrosoftAccount(object.get("token").getAsString());
                ((MicrosoftAccount) account).setUsername(object.get("username").getAsString());
            } else if (object.has("username")) {
                account = new CrackedAccount(object.get("username").getAsString());
            }
            if (account != null) {
                Managers.ACCOUNT.register(account);
                continue;
            }
            Ash.error("Could not parse account JSON.\nRaw: {}", object.toString());
        }
    }
}
