package com.example.examplemod.common.items;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class Importer extends Item {
    public Importer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        if(!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            MinecraftServer server = serverLevel.getServer();
            server.getFile();
        }
        return super.useOn(pContext);
    }
}
