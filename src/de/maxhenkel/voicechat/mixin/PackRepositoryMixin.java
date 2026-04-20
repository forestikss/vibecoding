
package de.maxhenkel.voicechat.mixin;

import de.maxhenkel.voicechat.resourcepacks.IPackRepository;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.resources.IPackFinder;
import ru.etc1337.api.interfaces.QuickImports;

import java.util.HashSet;
import java.util.Set;

public class PackRepositoryMixin implements IPackRepository, QuickImports {

    @Override
    public void addSource(IPackFinder source) {
        final Set<IPackFinder> sources = mc.getResourcePackList().packFinders;
        Set<IPackFinder> set = new HashSet<>(sources);
        set.add(source);
        mc.getResourcePackList().packFinders = set;
    }
}

