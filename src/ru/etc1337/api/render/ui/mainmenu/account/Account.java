package ru.etc1337.api.render.ui.mainmenu.account;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraft.util.math.vector.Vector2f;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.interfaces.IScreen;
import ru.etc1337.api.interfaces.QuickImports;
import ru.etc1337.api.render.Hover;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Glow;
import ru.etc1337.api.render.shaders.impl.Round;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Data
@Accessors(fluent = true)
public class Account implements IScreen, QuickImports {
    private final LocalDateTime creationDate;
    private final String name;
    private boolean favorite;
    private final Vector2f position = new Vector2f(0, 0);
    private final Vector2f size = new Vector2f(0, 0);

    public Account(LocalDateTime creationDate, String name) {
        this.creationDate = Objects.requireNonNull(creationDate, "Creation date cannot be null");
        if (creationDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Creation date cannot be in the future.");
        }
        this.name = Objects.requireNonNull(name, "Name cannot be null").trim();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
    }

    public Account toggleFavorite() {
        this.favorite = !this.favorite;
        return this;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        // No specific resize logic needed for individual account rendering
    }

    @Override
    public void init() {
        // No specific initialization logic needed for individual account rendering
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        boolean selected = mc.session.getProfile().getName().equals(name);

        if (Client.getInstance().getAccountGui().getSelected() != null &&
                Client.getInstance().getAccountGui().getSelected().equals(this)) {
            float glowSize = 10f;
            Glow.draw(matrix, new Rect(position.x, position.y, size.x, size.y), glowSize, 1.0f, glowSize / 2,
                    TempColor.getBackgroundColor(),
                    TempColor.getBackgroundColor(),
                    TempColor.getBackgroundColor(),
                    TempColor.getBackgroundColor());
        }

        Round.draw(matrix, new Rect(position.x, position.y, size.x, size.y), 4, TempColor.getBackgroundColor());

        // Render account name
        Fonts.SEMIBOLD_14.draw(matrix, name, position.x + 5F, position.y + size.y / 2F - 4F,
                TempColor.getFontColor().getRGB());

        // Render favorite star
        if (favorite) {
            Fonts.SEMIBOLD_14.draw(matrix, "★",
                    position.x + size.x - 15F,
                    position.y + size.y / 2F - 4F,
                    new Color(255, 215, 0).getRGB());
        }

        // Render creation date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        String formattedDate = creationDate.format(formatter);
        Fonts.SEMIBOLD_14.draw(matrix, formattedDate,
                position.x + size.x - Fonts.SEMIBOLD_14.width(formattedDate) - 5F,
                position.y + size.y / 2F - 4F,
                new Color(150, 150, 150).getRGB());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered(mouseX, mouseY)) {
            AccountGuiScreen accountGui = Client.getInstance().getAccountGui();
            if (accountGui.getSelected() == null || !accountGui.getSelected().equals(this)) {
                accountGui.setSelected(this);
                return true; // Indicate that the click was handled
            }
            if (accountGui.getSelected().equals(this) && !mc.session.getProfile().getName().equals(name)) {
                if (Hlp.isLClick(button)) {
                    mc.session = new Session(name, "", "", "");
                    return true;
                }
                if (Hlp.isRClick(button)) {
                    Client.getInstance().getAccountManager().removeAccount(name);
                    accountGui.setSelected(
                            Client.getInstance().getAccountManager().stream().findFirst().orElse(null));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    @Override
    public void onClose() {
        // No specific close logic needed for individual account rendering
    }

    public boolean hovered(double mouseX, double mouseY) {
        return Hover.isHovered(position.x, position.y, size.x, size.y, mouseX, mouseY);
    }
}