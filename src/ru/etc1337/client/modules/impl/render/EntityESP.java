package ru.etc1337.client.modules.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import lombok.Getter;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import ru.etc1337.Client;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.PrefixHelper;
import ru.etc1337.api.game.Translator;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.fonts.impl.TextBatcher;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.combat.AntiBot;
import ru.etc1337.client.modules.impl.misc.NameProtect;

import java.awt.Color;
import java.util.*;
import java.util.List;

@ModuleInfo(name = "Tags", description = "Отрисовка информации о игроках", category = ModuleCategory.RENDER)
public class EntityESP extends Module {

    @Getter
    private final MultiModeSetting checks = new MultiModeSetting("Элементы", this,
            "Эффекты зелий",
            "Зачарования",
            "Броня",
            "Никнейм",
            "Предметы",
            "Вторая Рука"
    );
    private final BooleanSetting onlyCustom = new BooleanSetting("Только Custom", this).setVisible(() -> checks.get("Вторая Рука").isEnabled());

    private final TextBatcher textBatcher = new TextBatcher(Fonts.MEDIUM_14);

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D eventRender2D) {
            if (mc.world == null || mc.player == null) return;

            MatrixStack matrix = eventRender2D.getMatrixStack();
            matrix.push();

            for (Entity entity : mc.world.getAllEntities()) {
                if (!isValid(entity)) continue;

                Vector3d interpolatedVector = Render.interpolate(entity, mc.getRenderPartialTicks());
                double posX = interpolatedVector.x;
                double posY = interpolatedVector.y;
                double posZ = interpolatedVector.z;

                renderEntity(matrix, entity, posX, posY, posZ);
            }

            textBatcher.setFont(Fonts.MEDIUM_14).drawAll();
            matrix.pop();
        }
    }

    private void renderEntity(MatrixStack matrixStack, Entity entity, double posX, double posY, double posZ) {
        double nametagWidth = entity.getWidth() / 1.5, nametagHeight = entity.getHeight() + 0.1f - (entity.isSneaking() ? 0.2f : 0.0f);

        AxisAlignedBB aabb = new AxisAlignedBB(posX - nametagWidth, posY, posZ - nametagWidth, posX + nametagWidth, posY + nametagHeight, posZ + nametagWidth);

        Vector2f min = null;
        Vector2f max = null;

        for (Vector3d vector : getVectors(aabb)) {
            Vector2f vec = Render.project(vector.x, vector.y, vector.z);

            if (min == null) {
                min = new Vector2f(vec.x, vec.y);
                max = new Vector2f(vec.x, vec.y);
            } else {
                min.x = Math.min(min.x, vec.x);
                min.y = Math.min(min.y, vec.y);
                max.x = Math.max(max.x, vec.x);
                max.y = Math.max(max.y, vec.y);
            }
        }

        if (max != null) {
            float minX = min.x;
            float minY = min.y;
            float maxX = max.x;
            float maxY = max.y;

            FixColor black = TempColor.getBackgroundColor().alpha(178);
            FixColor friendsColor = new FixColor(0, 66, 0, 155);

            if (entity instanceof PlayerEntity player) {
                boolean isFriend = Client.getInstance().getFriendManager().isFriend(player);
                FixColor color = isFriend ? friendsColor : black;
                if (checks.get("Никнейм").isEnabled()) {
                    ITextComponent displayName = Translator.translate(player.getDisplayName());

                    NameProtect nameProtect = Client.getInstance().getModuleManager().get(NameProtect.class);
                    boolean shouldProtect = isFriend && nameProtect.isEnabled() && nameProtect.friends.isEnabled();
                    if (shouldProtect) {
                        IFormattableTextComponent iformattabletextcomponent = ScorePlayerTeam.func_237500_a_(player.getTeam(), new StringTextComponent("stradix"));
                        displayName = player.addTellEvent(iformattabletextcomponent);
                    }
                    String title = displayName.getString();
                    if (title.isEmpty()) return;

                    if (PrefixHelper.isServerForFix() && !shouldProtect) {
                        List<ITextComponent> siblings = player.getDisplayName().getSiblings();

                        if (siblings.isEmpty()) {
                            return;
                        }

                        ITextComponent firstComponent = siblings.get(0);
                        String textContent = TextFormatting.getTextWithoutFormattingCodes(
                                firstComponent.getUnformattedComponentText().replace("●", "").trim()
                        );

                        IFormattableTextComponent newComponent = new StringTextComponent(PrefixHelper.translate(Objects.requireNonNull(textContent)));
                        IFormattableTextComponent newDisplayName = new StringTextComponent("");
                        newDisplayName.append(newComponent);

                        for (int i = 1; i < siblings.size(); i++) {
                            newDisplayName.append(siblings.get(i).deepCopy());
                        }

                        displayName = newDisplayName;
                    }
                    float nameX = minX + ((maxX - minX) / 2F);
                    // Фиксируем Y через интерполированную позицию — убирает тряску при ходьбе
                    float partialTicks = mc.getRenderPartialTicks();
                    double interpY = player.lastTickPosY + (player.getPosY() - player.lastTickPosY) * partialTicks;
                    double interpX = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * partialTicks;
                    double interpZ = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * partialTicks;
                    Vector2f topPos = Render.project(interpX, interpY + player.getHeight() + 0.25, interpZ);
                    float nameY = (topPos.x != Float.MAX_VALUE) ? topPos.y - Fonts.MEDIUM_14.height() - 1f : minY - Fonts.MEDIUM_14.height() * 1.5F;
                    float nameWidth = Fonts.MEDIUM_14.width(displayName);

                    int health = (int) player.getRealHealth();
                    String healthText = String.format("%sHP", health);

                    float healthWidth = Fonts.MEDIUM_14.width(healthText) + 2f;
                    float x = nameX - (nameWidth / 2F) - (healthWidth / 2F) - 1;
                    float height = 3;
                    float y = nameY - height/2f;

                    nametagWidth = (nameWidth + healthWidth) + 1;
                    nametagHeight = Fonts.MEDIUM_14.height();

                    double rightX = nametagWidth - 0.5f;
                    double bottomY = nametagHeight + height;
                    Round.draw(matrixStack, new Rect(x, y + 1.5f, (float) rightX, (float) bottomY),
                            3, 3, 3, 3, color, color, color, color);
                    ClientVoicechat client = ClientManager.getClient();
                    if (client != null) {
                        ClientPlayerStateManager manager = ClientManager.getPlayerStateManager();
                        boolean isVoiceClient = !manager.isPlayerDisconnected(player);
                        if (isVoiceClient) {
                            boolean isSpeak = client.getTalkCache().isWhispering(player) || client.getTalkCache().isTalking(player);
                            Render.drawRect(matrixStack, x - 1, y + 1.5f, (float) 1, (float) bottomY, manager.isPlayerDisabled(player) ? FixColor.ORANGE : (isSpeak ? FixColor.GREEN : FixColor.RED));
                            Render.drawRect(matrixStack, (float) (x + rightX), y + 1.5f, (float) 1, (float) bottomY, manager.isPlayerDisabled(player) ? FixColor.ORANGE : (isSpeak ? FixColor.GREEN : FixColor.RED));
                        }
                    }
                    textBatcher.add(displayName, matrixStack, nameX - (nameWidth / 2F) - (healthWidth / 2F) - 0.5f, nameY, 255);
                    textBatcher.add(healthText, matrixStack, nameX + (nameWidth / 2F) - (healthWidth / 2F) - 0.5f, nameY, getHealthColor(health, player.getMaxHealth()).getRGB(), false);
                    if (checks.get("Вторая Рука").isEnabled()) {
                        ITextComponent iTextComponent = player.getHeldItemOffhand().getDisplayName();
                        if (!(iTextComponent instanceof TranslationTextComponent && onlyCustom.isEnabled())) {

                            float strWidth = Fonts.MEDIUM_14.width(iTextComponent) + 2f;

                            x = maxX - ((maxX - minX) / 2F);
                            y = maxY + 5;

                            if (iTextComponent instanceof TranslationTextComponent textComponent) {
                                textBatcher.add(textComponent.getString(), matrixStack, x - (strWidth / 2), y, -1, false);
                            } else {
                                textBatcher.add(iTextComponent, matrixStack, x - (strWidth / 2), y, 255);
                            }
                            float rectX = x - (strWidth / 2);
                            Round.draw(matrixStack, new Rect(rectX - 1.5F, y, strWidth + 1, Fonts.MEDIUM_14.height() + height),
                                    3, 3, 3, 3, color, color, color, color);
                        }
                    }
                }

                if (checks.get("Эффекты зелий").isEnabled()) {
                    renderEffects(matrixStack, player, maxX, minY + 5);
                }

                if (checks.get("Броня").isEnabled()) {
                    List<ItemStack> items = new ArrayList<>();

                    ItemStack mainStack = player.getHeldItemMainhand();
                    if (!mainStack.isEmpty()) {
                        items.add(mainStack);
                    }

                    for (ItemStack itemStack : entity.getArmorInventoryList()) {
                        if (itemStack.isEmpty()) continue;
                        items.add(itemStack);
                    }

                    ItemStack offStack = player.getHeldItemOffhand();
                    if (!offStack.isEmpty()) {
                        items.add(offStack);
                    }

                    int x = (int) (minX + ((maxX - minX) / 2F) + (-items.size() * 8));
                    float nameTagY = minY - Fonts.MEDIUM_14.height() * 2;
                    nametagHeight = Fonts.MEDIUM_14.height() - 0.5F;

                    int y = (int) (nameTagY - (nametagHeight + 6));

                    float stackSize = 16;
                    for (ItemStack item : items) {
                        if (item.isEmpty()) continue;

                        Render.drawItemStack(matrixStack, item, x, y, 1, -1);

                        float enchWidth = (float) EnchantmentHelper.getEnchantments(item).entrySet().stream()
                                .mapToDouble(enchant -> Fonts.MEDIUM_14.width(getShortEnchantment(enchant)))
                                .max()
                                .orElse(0);
                        if (checks.get("Зачарования").isEnabled()) {
                            float yOffset = 0;
                            for (Map.Entry<Enchantment, Integer> enchant : EnchantmentHelper.getEnchantments(item).entrySet()) {
                                if (!getShortEnchantment(enchant).isEmpty()) {
                                    textBatcher.add(getShortEnchantment(enchant), matrixStack, x + (stackSize / 2F) - 5F, y - (stackSize / 2F) + yOffset, -1, false);
                                    yOffset -= Fonts.MEDIUM_14.height() + 2;
                                }
                            }
                        }
                        x += Math.max(enchWidth, stackSize);
                    }
                }
            } else if (entity instanceof ItemEntity item) {
                if (checks.get("Предметы").isEnabled()) {
                    ItemStack itemStack = item.getItem();
                    ITextComponent iTextComponent = itemStack.getDisplayName();
                    iTextComponent = iTextComponent.deepCopy().append(new StringTextComponent(String.format(" %sx", itemStack.getCount())));

                    float nameX = (minX + ((maxX - minX) / 2F));
                    float nameY = (minY - Fonts.MEDIUM_14.height());
                    float nameWidth = Fonts.MEDIUM_14.width(iTextComponent.getString()) + 2f;
                    nametagHeight = Fonts.MEDIUM_14.height();
                    Round.draw(matrixStack, new Rect(nameX - nameWidth / 2F - 1, nameY,
                            nameWidth,
                            (float) (nametagHeight + 3)), 4, black);

                    if (iTextComponent instanceof TranslationTextComponent textComponent) {
                        TextFormatting color = itemStack.getRarity().color;
                        FixColor rarity = TempColor.getFontColor();
                        if (color == TextFormatting.YELLOW || color == TextFormatting.LIGHT_PURPLE) {
                            rarity = FixColor.YELLOW;
                        }
                        textBatcher.add(textComponent.getString(), matrixStack, nameX - nameWidth / 2F, nameY, rarity.getRGB(), false);
                    } else {
                        textBatcher.add(iTextComponent, matrixStack, nameX - nameWidth / 2F, nameY, 255);
                    }

                    //textBatcher.add(title, matrixStack, nameX - nameWidth / 2F, nameY - 0.5f, TempColor.getFontColor().getRGB(), false);
                }
            }
        }
    }

    public Color getHealthColor(float hp, float maxHp) {
        float health = (hp == 1000) ? 20 : hp;
        float healthPercentage = health / maxHp;

        if (healthPercentage <= 0.25f) {
            return new Color(0xAA0000);
        } else if (healthPercentage <= 0.5f) {
            return new Color(0xFFAA00);
        } else if (healthPercentage <= 0.75f) {
            return new Color(0xFFFF55);
        } else {
            return new Color(0x55FF55);
        }
    }

    private void renderEffects(MatrixStack matrix, PlayerEntity player, float x, float y) {
        EffectInstance[] effects = player.getActivePotionEffects().toArray(new EffectInstance[0]);
        for (int index = 0; index < effects.length; index++) {
            EffectInstance effect = effects[index];
            if (effect == null) continue;
            String name = I18n.format(effect.getEffectName());
            String amplifier = I18n.format("enchantment.level." + (effect.getAmplifier() + 1)).replaceAll("enchantment.level.0", "");
            String duration = EffectUtils.getPotionDurationString(effect, 1);

            String effectText = (name + " " + amplifier + TextFormatting.RED + " (" + duration + ")" + TextFormatting.RESET).replace("**:**", "беск");

            textBatcher.add(effectText, matrix, x, y + (index * Fonts.MEDIUM_14.height()), -1, false);
        }
    }

    private String getShortEnchantment(Map.Entry<Enchantment, Integer> nbt) {
        if (nbt.getValue() < 0) return "";
        String output = nbt.getKey().getDisplayName(0).getString().substring(0, 2);

        output += " ";

        if (nbt.getValue() != 1 || nbt.getKey().getMaxLevel() != 1) {
            if (nbt.getValue() == Short.MAX_VALUE) {
                output += "∞";
            } else if (nbt.getValue() > 10) {
                output += nbt.getValue().toString();
            } else {
                output += new TranslationTextComponent("enchantment.level." + nbt.getValue()).getString();
            }
        }

        return output.replaceAll("enchantment.level.", "");
    }

    private Vector3d[] getVectors(AxisAlignedBB boundingBox) {
        return new Vector3d[]{
                new Vector3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                new Vector3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ),
                new Vector3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                new Vector3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ),
                new Vector3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),
                new Vector3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ),
                new Vector3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                new Vector3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ)
        };
    }

    private boolean isValid(Entity entity) {
        if (!Render.isInView(entity)) return false;
        if (entity == mc.player && mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) return false;
        if (!entity.isAlive()) return false;
        if (entity instanceof PlayerEntity player && AntiBot.isBot(player)) return false;

        return entity instanceof PlayerEntity || entity instanceof ItemEntity;
    }
}