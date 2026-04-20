package ru.etc1337.client.modules.impl.render;

import lombok.Getter;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

@Getter
@ModuleInfo(name = "View Model", description = "Положение рук на экране", category = ModuleCategory.RENDER)
public class ViewModel extends Module {
    public final SliderSetting rightX = new SliderSetting("RightX",this,0.0f, -2.0f, 2.0f, 0.1f);
    public final SliderSetting rightY = new SliderSetting("RightY",this,0.0f, -2.0f, 2.0f, 0.1f);
    public final SliderSetting rightZ = new SliderSetting("RightZ",this,0.0f, -2.0f, 2.0f, 0.1f);
    public final SliderSetting leftX = new SliderSetting("LeftX",this,0.0f, -2.0f, 2.0f, 0.1f);
    public final SliderSetting leftY = new SliderSetting("LeftY",this,0.0f, -2.0f, 2.0f, 0.1f);
    public final SliderSetting leftZ = new SliderSetting("LeftZ",this,0.0f, -2.0f, 2.0f, 0.1f);
}

