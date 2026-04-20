package ru.etc1337.client.modules.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.math.vector.Vector2f;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.draggable.Draggable;
import ru.etc1337.api.events.impl.game.EventTick;
import ru.etc1337.api.events.impl.game.EventUpdate;
import ru.etc1337.api.events.impl.input.EventKeyboardClick;
import ru.etc1337.api.events.impl.input.EventMouseClick;
import ru.etc1337.api.events.impl.render.EventRender2D;
import ru.etc1337.api.game.Move;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.render.Hover;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.fonts.Fonts;
import ru.etc1337.api.render.shaders.impl.Round;
import ru.etc1337.api.render.ui.dropui.elements.extended.*;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.settings.Setting;
import ru.etc1337.api.settings.impl.*;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;
import ru.etc1337.client.modules.impl.render.ui.*;
import ru.etc1337.client.modules.impl.render.ui.NotificationRenderer;
import ru.etc1337.client.modules.impl.render.ui.NurikWatermarkRenderer;
import ru.etc1337.client.modules.impl.render.ui.ModuleNotifyRenderer;
import ru.etc1337.client.modules.impl.render.ui.api.Header;
import ru.etc1337.client.modules.impl.render.ui.api.UIElement;
import ru.etc1337.client.modules.impl.render.ui.info.*;
import ru.etc1337.client.modules.impl.render.ui.settings.Settings;

import java.util.*;

@ModuleInfo(name = "Interface", description = "Интерфейс клиента", category = ModuleCategory.RENDER)
public class Interface extends Module {
    @Getter
    private final List<UIElement> elements = new ArrayList<>();
    private Vector2f mousePosition = Vector2f.ZERO;

    public Interface() {
        System.out.println("Init Interface");

        List<UIElement> elements = Arrays.asList(
                new WatermarkRenderer(),
                new KeybindsRenderer(),
                new PotionsRenderer(),
                new TpsRenderer(),
                new TotemRenderer(),
                new ServerRenderer(),
                new TimeRenderer(),
                new ArmorRenderer(),
                new PingRenderer(),
                new FpsRenderer(),
                new BpsRenderer(),
                new CoordsRenderer(),
                new StaffRenderer(),
                new TargetRenderer(),
                new NotificationRenderer(),
                new ru.etc1337.client.modules.impl.render.ui.PigTargetRenderer(),
                new NurikWatermarkRenderer(),
                new ModuleNotifyRenderer()
        );
        elements.forEach(this::addElement);
        this.elements.add(new Settings()); // добавляем после инициализации

        String[] elementNames = elements.stream()
                .map(UIElement::getName)
                .map(String::trim)
                .toArray(String[]::new);

        // Ищем существующий MultiModeSetting "UI Elements"
        MultiModeSetting existing = null;
        for (Setting s : getSettings()) {
            if (s instanceof MultiModeSetting ms && ms.getName().equals("UI Elements")) {
                existing = ms;
                break;
            }
        }

        if (existing == null) {
            // Первый запуск — создаём новый, все включены по умолчанию
            MultiModeSetting uiElementsSetting = new MultiModeSetting("UI Elements", this, elementNames);
            // включаем все по умолчанию
            for (String name : elementNames) {
                BooleanSetting bs = uiElementsSetting.get(name);
                if (bs != null) bs.setEnabled(true);
            }
            getSettings().add(uiElementsSetting);
        } else {
            // Добавляем новые элементы которых ещё нет
            for (String name : elementNames) {
                if (existing.get(name) == null) {
                    BooleanSetting bs = new BooleanSetting(name, (ru.etc1337.api.settings.api.Parent) null);
                    bs.setEnabled(true);
                    existing.getBoolSettings().add(bs);
                }
            }
        }
    }

    @Override
    public void onEvent(Event event) {
        for (UIElement element : elements) {
            if (element instanceof Settings) {
                element.onEvent(event);
                continue;
            }

            for (Setting setting : getSettings()) {
                if (setting instanceof MultiModeSetting multiSetting) {
                    BooleanSetting elementSetting = multiSetting.get(element.getName().trim());
                    if (elementSetting != null && elementSetting.isEnabled()) {
                        element.onEvent(event);
                    }
                }
            }
        }

        if (!(mc.currentScreen instanceof ChatScreen)) return;

        if (event instanceof EventTick) {
            if (mc.mouseHelper == null) return;
            double mouseX = mc.mouseHelper.getMouseX() * window.getScaledWidth() / (double) window.getWidth();
            double mouseY = mc.mouseHelper.getMouseY() * window.getScaledHeight() / (double) window.getHeight();
            mousePosition = new Vector2f((float) mouseX, (float) mouseY);
        }

        if (event instanceof EventMouseClick eventMouseClick) {
            for (UIElement element : elements) {
                boolean isEnabled = element instanceof Settings;
                if (!isEnabled) {
                    for (Setting setting : getSettings()) {
                        if (setting.getHideCondition() == null || setting.getHideCondition().get()) {
                            if (setting instanceof MultiModeSetting multiSetting) {
                                BooleanSetting elementSetting = multiSetting.get(element.getName().trim());
                                if (elementSetting != null) {
                                    isEnabled = elementSetting.isEnabled();
                                    break;
                                }
                            }
                        }
                    }
                }

                if (!isEnabled) {
                    Draggable draggable = element.getDraggable();
                    if (draggable != null) {
                        draggable.setWidth(0); // 'выключаем драг'
                    }
                    continue;
                }

                Draggable draggable = element.getDraggable();
                if (draggable == null || element.getSettings().isEmpty()) continue;

                if (Hover.isHovered(draggable, mousePosition.x, mousePosition.y)) {
                    if (eventMouseClick.getKey() == 1 && !eventMouseClick.isReleased()) {
                        //комментим т.к ниже в коде есть постоянная привязка
                        //element.setClickedMousePosition(new Vector2f(draggable.getX(), draggable.getY() + draggable.getHeight() + 5F));
                        element.setSettingsWindow(!element.isSettingsWindow());
                    }
                }

                if (element.isSettingsWindow()) {
                    float yOffset = element.getClickedMousePosition().y + 2.5F;
                    float xOffset = element.getClickedMousePosition().x + 2.5F;


                    for (Setting setting : element.getSettings()) {
                        if (setting.getHideCondition() == null || setting.getHideCondition().get()) {
                            if (setting instanceof BooleanSetting booleanSetting) {
                                element.getCheckboxes().add(new BooleanRenderer(booleanSetting));
                            } else if (setting instanceof SliderSetting sliderSetting) {
                                element.getSliders().add(new SliderRenderer(sliderSetting));
                            } else if (setting instanceof BindSetting bindSetting) {
                                element.getBinds().add(new BindRenderer(bindSetting));
                            } else if (setting instanceof ColorSetting colorSetting) {
                                element.getColors().add(new ColorRenderer(colorSetting));
                            } else if (setting instanceof ModeSetting modeSetting) {
                                element.getModes().add(new ModeRenderer(modeSetting));
                            } else if (setting instanceof MultiModeSetting multiModeSetting) {
                                element.getMultiModes().add(new MultiModeRenderer(multiModeSetting));
                            }
                        }
                    }

                    for (Setting setting : element.getSettings()) {
                        if (setting.getHideCondition() == null || setting.getHideCondition().get()) {
                            if (setting instanceof BooleanSetting) {
                                for (BooleanRenderer renderer : element.getCheckboxes()) {
                                    if (renderer.getSetting() == setting) {
                                        if (!eventMouseClick.isReleased() && eventMouseClick.getKey() == 0) {
                                            renderer.mouseClicked(0, xOffset, yOffset, mousePosition.x, mousePosition.y);
                                        }
                                        yOffset += 12;
                                        break;
                                    }
                                }
                            } else if (setting instanceof SliderSetting) {
                                for (SliderRenderer renderer : element.getSliders()) {
                                    if (renderer.getSetting() == setting) {
                                        if (!eventMouseClick.isReleased() && eventMouseClick.getKey() == 0) {
                                            if (SliderRenderer.isHovered(xOffset, yOffset, 110, mousePosition.x, mousePosition.y)) {
                                                renderer.mouseClicked(xOffset, yOffset, 110, mousePosition.x, mousePosition.y, eventMouseClick.getKey());
                                            }
                                        } else if (eventMouseClick.isReleased()) {
                                            renderer.mouseReleased(mousePosition.x, mousePosition.y, eventMouseClick.getKey());
                                        }
                                        yOffset += renderer.getNextHeight() + 12;
                                        break;
                                    }
                                }
                            } else if (setting instanceof BindSetting) {
                                for (BindRenderer renderer : element.getBinds()) {
                                    if (renderer.getSetting() == setting) {
                                        if (!eventMouseClick.isReleased() && eventMouseClick.getKey() == 0) {
                                            renderer.mouseClicked(xOffset, yOffset, 110, mousePosition.x, mousePosition.y, eventMouseClick.getKey());
                                        }
                                        yOffset += 12;
                                        break;
                                    }
                                }
                            } else if (setting instanceof ColorSetting) {
                                for (ColorRenderer renderer : element.getColors()) {
                                    if (renderer.getSetting() == setting) {
                                        if (!eventMouseClick.isReleased() && eventMouseClick.getKey() == 0) {
                                            renderer.mouseClicked(xOffset, yOffset, 110, mousePosition.x, mousePosition.y, eventMouseClick.getKey());
                                        } else if (eventMouseClick.isReleased()) {
                                            renderer.mouseReleased(mousePosition.x, mousePosition.y, eventMouseClick.getKey());
                                        }
                                        yOffset += renderer.getHeight();
                                        break;
                                    }
                                }
                            } else if (setting instanceof ModeSetting) {
                                for (ModeRenderer renderer : element.getModes()) {
                                    if (renderer.getSetting() == setting) {
                                        if (!eventMouseClick.isReleased() && eventMouseClick.getKey() == 0) {
                                            renderer.mouseClicked(mousePosition.x, mousePosition.y, eventMouseClick.getKey());
                                        }
                                        yOffset += renderer.getNextHeight(0) + 12;
                                        break;
                                    }
                                }
                            } else if (setting instanceof MultiModeSetting) {
                                for (MultiModeRenderer renderer : element.getMultiModes()) {
                                    if (renderer.getSetting() == setting) {
                                        if (!eventMouseClick.isReleased() && eventMouseClick.getKey() == 0) {
                                            renderer.mouseClicked(mousePosition.x, mousePosition.y, eventMouseClick.getKey());
                                        }
                                        yOffset += renderer.getNextHeight(0) + 12;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (event instanceof EventKeyboardClick eventKeyboardClick) {
            for (UIElement element : elements) {
                if (element.isSettingsWindow()) {
                    for (BindRenderer renderer : element.getBinds()) {
                        if (!eventKeyboardClick.isReleased()) {
                            renderer.keyPressed(eventKeyboardClick.getKey(), eventKeyboardClick.getScancode(), 0);
                        }
                    }
                }
            }
        }

        if (event instanceof EventRender2D eventRender2D) {
            MatrixStack matrixStack = eventRender2D.getMatrixStack();

            for (UIElement element : elements) {
                Draggable draggable = element.getDraggable();
                if (draggable != null) {
                    boolean shouldRenderHeader = false;
                    for (Setting setting : element.getSettings()) {
                        if (setting instanceof BooleanSetting booleanSetting &&
                                booleanSetting.getName().equalsIgnoreCase("Убрать заголовок") &&
                                booleanSetting.isEnabled()) {
                            shouldRenderHeader = true;
                            break;
                        }
                    }

                    if (shouldRenderHeader) {
                        Rect rect = new Rect(draggable.getX(), draggable.getY(), draggable.getWidth(), draggable.getHeight());

                        Header.drawModernHeader(matrixStack, draggable, rect.getX(), rect.getY(), element.getIcon(), element.getName());
                    }
                }

                if (element.isSettingsWindow()) {
                    if (draggable != null) {
                        element.setClickedMousePosition(new Vector2f(draggable.getX(), draggable.getY() + draggable.getHeight() + 2.5F));
                    }

                    float maxTextWidth = 0;
                    float baseSettingWidth = 110;
                    float minSettingWidth = 65;
                    float maxSettingWidth = 85f;

                    int settingCount = element.getSettings().size();
                    for (Setting setting : element.getSettings()) {
                        if (setting.getHideCondition() == null || setting.getHideCondition().get()) {
                            maxTextWidth = Math.max(maxTextWidth, Fonts.SEMIBOLD_13.width(setting.getName()));
                            if (setting instanceof BooleanSetting) {
                                maxSettingWidth = Math.max(maxSettingWidth, settingCount == 1 ? minSettingWidth : baseSettingWidth);
                            } else if (setting instanceof SliderSetting) {
                                maxSettingWidth = Math.max(maxSettingWidth, settingCount == 1 ? minSettingWidth : baseSettingWidth);
                            } else if (setting instanceof BindSetting) {
                                maxSettingWidth = Math.max(maxSettingWidth, settingCount == 1 ? minSettingWidth : baseSettingWidth);
                            } else if (setting instanceof ColorSetting) {
                                maxSettingWidth = Math.max(maxSettingWidth, settingCount == 1 ? minSettingWidth : baseSettingWidth);
                            } else if (setting instanceof ModeSetting || setting instanceof MultiModeSetting) {
                                maxSettingWidth = Math.max(maxSettingWidth, settingCount == 1 ? minSettingWidth : baseSettingWidth);
                            }
                        }
                    }

                    float totalHeight = 0; // начальный отступ
                    for (Setting setting : element.getSettings()) {
                        if (setting.getHideCondition() == null || setting.getHideCondition().get()) {
                            if (setting instanceof BooleanSetting) {
                                totalHeight += 12;
                            } else if (setting instanceof SliderSetting) {
                                totalHeight += 14;
                            } else if (setting instanceof BindSetting) {
                                totalHeight += 12;
                            } else if (setting instanceof ColorSetting) {
                                for (ColorRenderer renderer : element.getColors()) {
                                    if (renderer.getSetting() == setting) {
                                        totalHeight += renderer.getHeight();
                                        break;
                                    }
                                }
                            } else if (setting instanceof ModeSetting) {
                                for (ModeRenderer renderer : element.getModes()) {
                                    if (renderer.getSetting() == setting) {
                                        totalHeight += renderer.getNextHeight(0) + 12;
                                        break;
                                    }
                                }
                            } else if (setting instanceof MultiModeSetting) {
                                for (MultiModeRenderer renderer : element.getMultiModes()) {
                                    if (renderer.getSetting() == setting) {
                                        totalHeight += renderer.getNextHeight(0) + 12;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    Rect rect = new Rect(element.getClickedMousePosition().x, element.getClickedMousePosition().y,
                            maxSettingWidth + 2.5f,
                            totalHeight + 0.5f
                    );

                    Round.draw(matrixStack, rect, 6, TempColor.getBackgroundColor());

                    float yOffset = element.getClickedMousePosition().y + 2.5F;
                    float xOffset = element.getClickedMousePosition().x + 2.5F;

                    for (Setting setting : element.getSettings()) {
                        if (setting.getHideCondition() == null || setting.getHideCondition().get()) {
                            if (setting instanceof BooleanSetting booleanSetting) {
                                for (BooleanRenderer renderer : element.getCheckboxes()) {
                                    if (renderer.getSetting() == setting) {
                                        renderer.render(matrixStack, xOffset, yOffset, mousePosition.x, mousePosition.y);
                                        Fonts.SEMIBOLD_13.draw(matrixStack, booleanSetting.getName(), xOffset + 10.5F, yOffset, TempColor.getFontColor().getRGB());
                                        yOffset += 12;
                                        break;
                                    }
                                }
                            } else if (setting instanceof SliderSetting sliderSetting) {
                                for (SliderRenderer renderer : element.getSliders()) {
                                    if (renderer.getSetting() == setting) {
                                        renderer.render(matrixStack, xOffset, yOffset, maxSettingWidth, mousePosition.x, mousePosition.y);
                                        yOffset += renderer.getNextHeight() + 12;
                                        break;
                                    }
                                }
                            } else if (setting instanceof BindSetting bindSetting) {
                                for (BindRenderer renderer : element.getBinds()) {
                                    if (renderer.getSetting() == setting) {
                                        renderer.render(matrixStack, xOffset, yOffset, maxSettingWidth + 2F, mousePosition.x, mousePosition.y);
                                        yOffset += 12;
                                        break;
                                    }
                                }
                            } else if (setting instanceof ColorSetting colorSetting) {
                                for (ColorRenderer renderer : element.getColors()) {
                                    if (renderer.getSetting() == setting) {
                                        renderer.render(matrixStack, xOffset, yOffset, maxSettingWidth, mousePosition.x, mousePosition.y);
                                        yOffset += renderer.getHeight();
                                        break;
                                    }
                                }
                            } else if (setting instanceof ModeSetting modeSetting) {
                                for (ModeRenderer renderer : element.getModes()) {
                                    if (renderer.getSetting() == setting) {
                                        renderer.render(matrixStack, xOffset, yOffset, maxSettingWidth, 0, mousePosition.x, mousePosition.y);
                                        yOffset += renderer.getNextHeight(0) + 12;
                                        break;
                                    }
                                }
                            } else if (setting instanceof MultiModeSetting multiModeSetting) {
                                for (MultiModeRenderer renderer : element.getMultiModes()) {
                                    if (renderer.getSetting() == setting) {
                                        renderer.render(matrixStack, xOffset, yOffset, maxSettingWidth, 0, mousePosition.x, mousePosition.y);
                                        yOffset += renderer.getNextHeight(0) + 12;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public <T extends UIElement> T find(final Class<T> clazz) {
        return this.elements.stream()
                .filter(module -> clazz.isAssignableFrom(module.getClass()))
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }
    public void addElement(UIElement element) {
        elements.add(element);
    }
}