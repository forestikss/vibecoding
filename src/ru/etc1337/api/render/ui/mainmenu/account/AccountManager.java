package ru.etc1337.api.render.ui.mainmenu.account;

import lombok.extern.log4j.Log4j2;
import ru.etc1337.api.config.Directory;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Log4j2
public class AccountManager extends CopyOnWriteArrayList<Account> {
    public static File ACCOUNT_DIRECTORY;

    public AccountManager() {
        init();
    }

    public void init() {
        ACCOUNT_DIRECTORY = Directory.DIRECTORY;
        if (!ACCOUNT_DIRECTORY.exists() && !ACCOUNT_DIRECTORY.mkdirs()) {
            log.error("Не удалось создать папку для аккаунтов");
            System.exit(1);
        }
        get().read(this);
    }

    public AccountFile get() {
        return new AccountFile(new File(ACCOUNT_DIRECTORY, "accounts.json"));
    }

    public void save() {
        get().write(this);
    }

    public void addAccount(Account account) {
        if (isAccount(account.name())) return;
        super.add(account);
        save();
    }

    public Optional<Account> getAccount(String name) {
        return stream().filter(account -> account.name().equalsIgnoreCase(name)).findFirst();
    }

    public boolean isAccount(String name) {
        return stream().anyMatch(account -> account.name().equalsIgnoreCase(name));
    }

    public void removeAccount(String name) {
        removeIf(account -> account.name().equalsIgnoreCase(name));
        save();
    }

    public void clearAccounts() {
        clear();
        save();
    }

    public List<Account> getFavoriteAccountsSorted() {
        return stream()
                .filter(Account::favorite)
                .collect(Collectors.toList());
    }
}