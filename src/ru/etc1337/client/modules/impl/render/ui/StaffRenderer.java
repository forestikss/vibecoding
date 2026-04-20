package ru.etc1337.client.modules.impl.render.ui;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.world.GameType;
import ru.etc1337.Client;
import ru.etc1337.api.animations.simple.Animation;
import ru.etc1337.api.animations.simple.Easing;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.game.PrefixHelper;
import ru.etc1337.api.game.Translator;
import ru.etc1337.api.other.AsyncManager;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.fonts.impl.TextBatcher;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.impl.render.ui.api.ElementInfo;
import ru.etc1337.client.modules.impl.render.ui.api.Header;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ElementInfo(name = "Staff", icon = "q", initX = 60.0F, initY = 28.0F, initHeight = 17.0F)
public class StaffRenderer extends UIElement {
    private final BooleanSetting removeHeader = new BooleanSetting("Убрать заголовок", this);
    private final BooleanSetting fullSize = new BooleanSetting("Полный размер", this);

    private final BooleanSetting showPrefix = new BooleanSetting("Показывать префикс", this);

    private final BooleanSetting showSuffix = new BooleanSetting("Показывать статус", this)/*.setVisible(() -> !getShowHead().isEnabled())*/;
    @Getter
    //private final BooleanSetting showHead = new BooleanSetting("Показывать голову", this).setVisible(() -> !showSuffix.isEnabled());
    private final BooleanSetting ignoreOnline = new BooleanSetting("Игнорировать онлайн staff", this);
    private final BooleanSetting showVanished = new BooleanSetting("Показывать скрытых", this);
    private final BooleanSetting autoAdd = new BooleanSetting("Авто добавление", this);
    private final BooleanSetting visibleOnly = new BooleanSetting("Скрывать если пусто", this).setVisible(() -> !removeHeader.isEnabled());

    private final TextBatcher textBatcher = new TextBatcher(Fonts.SEMIBOLD_14);
    private final Animation animation = new Animation(Easing.SINE_IN_OUT, 150);
    // Инициализируем анимацию сразу в 1 чтобы не было моргания при старте
    {
        animation.update(1f);
        for (int i = 0; i < 20; i++) animation.update(1f);
    }

    private static final Pattern VALID_USER_PATTERN = Pattern.compile("^\\w{3,16}$");
    private static final Set<String> STAFF_PREFIX_KEYWORDS = Set.of(
            "developer", "moder", "helper", "admin", "owner",
            "хелпер", "модер", "админ", "kurator", "сотрудник",
            "стажер", "мл.сотрудник", "поддержка", "модератор", "J.MODER"
    );
    private static final Set<String> EXCLUDED_PREFIX_KEYWORDS = Set.of(
            "d.moder", "d.helper", "d.admin"
    );

    private final List<StaffMember> staffList = new ArrayList<>();
    private long lastUpdateTime = 0;
    private boolean isUpdating = false;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mc.player == null || mc.isSingleplayer()) {
                staffList.clear();
                return;
            }

            if (PrefixHelper.isServerForFix() && showVanished.isEnabled()) {
                ITextComponent header = mc.ingameGUI.getTabList().getHeader();
                if (header == null) return;
                String textContent = TextFormatting.getTextWithoutFormattingCodes(
                        header.getUnformattedComponentText()
                );
                if (textContent == null || textContent.contains("Вы находитесь в: Lobby")) {
                    staffList.clear();
                    return;
                }
            }

            long now = System.currentTimeMillis();
            if (now - lastUpdateTime >= 2000) { // раз в 2 секунды, в главном потоке
                lastUpdateTime = now;
                try {
                    List<StaffMember> updated = collectStaffMembers();
                    staffList.clear();
                    staffList.addAll(updated);
                } catch (Exception ignored) {}
            }
        }

        if (event instanceof EventRender2D eventRender2D) {
            MatrixStack matrixStack = eventRender2D.getMatrixStack();
            float x = getDraggable().getX();
            float y = getDraggable().getY();

                // Не скрываем пока идёт обновление — предотвращает моргание
                boolean isEmpty = staffList.isEmpty() && !isUpdating;
                // Дополнительно: не скрываем если список только что был непустым
                float target = (!(mc.currentScreen instanceof ChatScreen) && visibleOnly.isEnabled() && isEmpty) ? 0f : 1f;
                // Если анимация уже показывает элемент — не скрываем резко
                if (animation.getValue() > 0.5f && staffList.isEmpty() && isUpdating) target = 1f;
                animation.update(target);
                float value = animation.getValue();
                if (value < 0.05F) return;

                if (!removeHeader.isEnabled()) {
                    Header.drawModernHeader(matrixStack, getDraggable(), x, y, getIcon(), getName(), value * 255);
                }

                if (staffList.isEmpty()) return;
                renderStaffList(matrixStack, value);
        }
    }

    private void renderStaffList(MatrixStack matrixStack, float value) {
        float x = getDraggable().getX();
        float y = getDraggable().getY();
        float hotkeyOffsetY = y;
        hotkeyOffsetY += 19.5f;

        boolean isIgnore = fullSize.isEnabled();
        float scale = isIgnore ? 1 : 0.8f;
        float offset = isIgnore ? 18 : 15;
        for (StaffMember staffMember : staffList) {
            String name = staffMember.name;

            ITextComponent prefix = staffMember.prefix;
            boolean containsAllowed = prefix.getString().matches(".*[\\p{IsCyrillic}A-Za-z0-9.!№?*;()].*");
            if (!containsAllowed) {
                prefix = null;
            }

            boolean showPref = showPrefix.isEnabled();
            boolean showSuff = showSuffix.isEnabled();

            String icon = staffMember.vanished ? "s" : "r";
            Header.drawModernHeader(matrixStack, getDraggable(), x, hotkeyOffsetY, icon, name, scale, value * 255, staffMember.color.alpha(125),
                    showSuff ? staffMember.suffix : null, showPref ? prefix : null);


            hotkeyOffsetY += offset;
        }
    }

    private List<StaffMember> collectStaffMembers() {
        Set<String> onlinePlayers = new HashSet<>(getOnlinePlayers());
        List<StaffMember> onlineStaff = !ignoreOnline.isEnabled() ? getOnlineStaff() : Collections.emptyList();
        List<StaffMember> vanishedStaff = getVanishedStaff(onlinePlayers);

        Map<String, StaffMember> result = new HashMap<>();
        for (StaffMember sm : onlineStaff) result.put(sm.getName(), sm);
        for (StaffMember sm : vanishedStaff) result.put(sm.getName(), sm);

        return new ArrayList<>(result.values());
    }

    public static List<String> getOnlinePlayers() {
        if (mc.player == null) return Collections.emptyList();
        return mc.player.connection.getPlayerInfoMap().stream()
                .map(NetworkPlayerInfo::getGameProfile)
                .map(GameProfile::getName)
                .filter(StaffRenderer::isValidName)
                .collect(Collectors.toList());
    }

    private List<StaffMember> getOnlineStaff() {
        if (mc.player == null) return Collections.emptyList();
        List<StaffMember> result = new ArrayList<>();

        for (NetworkPlayerInfo info : mc.player.connection.getPlayerInfoMap()) {
            if (info.getPlayerTeam() == null) continue;

            String name = info.getGameProfile().getName();
            if (!isValidName(name)) continue;

            if (info.getGameType() == GameType.SPECTATOR) {
                handleSpectator(result, info, name);
                continue;
            }

            String prefix = TextFormatting.getTextWithoutFormattingCodes(
                    getPrefix(info.getPlayerTeam().getPrefix()).getString().trim()
            );

            if (isStaffPrefix(prefix) || isInStaffManager(name)) {
                handleAutoAdd(name);
                result.add(new StaffMember(
                        name,
                        getPrefix(info.getPlayerTeam().getPrefix()),
                        TextFormatting.GREEN + "Active",
                        info.getLocationSkin(),
                        FixColor.GREEN, false));
            }
        }

        return result;
    }

    private String getCompiledPrefix(String prefix, List<ITextComponent> siblings) {
        StringBuilder prefixBuilder = new StringBuilder();
        if (prefix != null && prefix.trim().isEmpty()) {
            for (ITextComponent i : siblings) {
                prefixBuilder.append(i.getString());
            }
            prefix = prefixBuilder.toString().trim();
        }
        return prefix;
    }

    private List<StaffMember> getVanishedStaff(Set<String> onlinePlayers) {
        if (mc.world == null) return Collections.emptyList();
        List<StaffMember> result = new ArrayList<>();

        for (ScorePlayerTeam team : mc.world.getScoreboard().getTeams()) {
            for (String name : team.getMembershipCollection()) {
                if (!isValidName(name) || onlinePlayers.contains(name)) continue;

                NetworkPlayerInfo info = findPlayerInfoByName(name);
                ITextComponent prefixComponent = getPrefix(team.getPrefix());
                String prefix = prefixComponent.getString();

                if (isStaffPrefix(prefix) || isInStaffManager(name) || showVanished.isEnabled()) {
                    String displayName = team.getDisplayName().getString().trim();
                  //  TextFormatting teamColor = team.getColor();
                    if (displayName.toLowerCase().startsWith("cit-")) continue; // bots
                    if (displayName.startsWith("модер") || displayName.startsWith("FS_") || displayName.startsWith("npc") || (!name.trim().contains(displayName) && displayName.startsWith("J") && displayName.endsWith("A"))) continue;


                    result.add(new StaffMember(
                            name,
                            prefixComponent,
                            TextFormatting.GRAY + "Vanished",
                            info != null ? info.getLocationSkin() : null,
                            FixColor.GRAY, true));
                }
            }
        }

        return result;
    }

    private void handleSpectator(List<StaffMember> list, NetworkPlayerInfo info, String name) {
        handleAutoAdd(name);
        list.add(new StaffMember(
                name,
                getPrefix(info.getPlayerTeam().getPrefix()),
                TextFormatting.GOLD + "Gm3",
                info.getLocationSkin(),
                new FixColor(255, 215, 0), true
        ));
    }


    private void handleAutoAdd(String name) {
        if (autoAdd.isEnabled() && !isInStaffManager(name)) {
            Chat.send(TextFormatting.GREEN + name + TextFormatting.RESET + " был добавлен в staff");
            Client.getInstance().getStaffManager().getStaff().add(name);
        }
    }

    private boolean isInStaffManager(String name) {
        return Client.getInstance().getStaffManager().getStaff().contains(name);
    }

    private NetworkPlayerInfo findPlayerInfoByName(String name) {
        return mc.player.connection.getPlayerInfoMap().stream()
                .filter(info -> info.getGameProfile().getName().equals(name))
                .findFirst().orElse(null);
    }

    public ITextComponent getPrefix(ITextComponent prefix) {
        if (prefix == null) return new StringTextComponent("");
        if (PrefixHelper.isServerForFix()) {
            String textContent = TextFormatting.getTextWithoutFormattingCodes(
                    prefix.getUnformattedComponentText().replace("●", "").trim()
            );

            IFormattableTextComponent newComponent = new StringTextComponent(PrefixHelper.translate(Objects.requireNonNull(textContent)).trim());
            newComponent.setStyle(prefix.deepCopy().getStyle());

            IFormattableTextComponent newDisplayName = new StringTextComponent("");
            newDisplayName.append(newComponent);

            return newDisplayName;
        } else {
            return Translator.translate(prefix);
        }
    }

    private boolean isStaffPrefix(String prefix) {
        if (prefix == null) return false;

        String normalized = Translator.translate(
                TextFormatting.getTextWithoutFormattingCodes(prefix).toLowerCase()).toLowerCase();


        for (String excluded : EXCLUDED_PREFIX_KEYWORDS) {
            if (normalized.contains(excluded)) return false;
        }

        for (String keyword : STAFF_PREFIX_KEYWORDS) {
            if (normalized.contains(keyword)) return true;
        }

        return false;
    }

    private static boolean isValidName(String name) {
        return !name.isEmpty() && !containsRussianChars(name)
                && (VALID_USER_PATTERN.matcher(name).matches() || name.contains(","));
    }

    private static boolean containsRussianChars(String input) {
        return input.matches(".*[А-Яа-яЁё].*");
    }

    @AllArgsConstructor @Getter @Setter
    public static class StaffMember {
        private final String name;
        private final ITextComponent prefix;
        private final String suffix;
        private final ResourceLocation skin;
        private FixColor color;
        private boolean vanished;
    }
}