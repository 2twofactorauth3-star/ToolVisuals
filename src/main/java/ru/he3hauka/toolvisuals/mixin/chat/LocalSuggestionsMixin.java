package ru.he3hauka.toolvisuals.mixin.chat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.ParseResults;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.he3hauka.toolvisuals.client.command.LocalCommandLine;
import ru.he3hauka.toolvisuals.client.command.LocalSuggestions;

@Mixin(CommandSuggestions.class)
public abstract class LocalSuggestionsMixin {
    @Shadow @Final private EditBox input;
    @Shadow @Final private boolean commandsOnly;
    @Shadow private ParseResults<?> currentParse;
    @Unique private long toolvisuals$policy = Long.MIN_VALUE;

    @Inject(method="updateCommandInfo",at=@At("HEAD"))
    private void toolvisuals$refreshPolicy(CallbackInfo callback){
        if(commandsOnly||!LocalCommandLine.suggests(input.getValue()))return;
        long next=LocalSuggestions.policyRevision();
        if(toolvisuals$policy!=next){currentParse=null;toolvisuals$policy=next;}
    }

    @ModifyArg(method = "updateCommandInfo", at = @At(value = "INVOKE", target =
            "Lcom/mojang/brigadier/StringReader;<init>(Ljava/lang/String;)V"), index = 0)
    private String toolvisuals$normalizeCommand(String text) {
        return commandsOnly ? text : LocalCommandLine.suggestionInput(text);
    }

    @WrapOperation(method = "updateCommandInfo", at = @At(value = "INVOKE", target =
            "Lcom/mojang/brigadier/StringReader;peek()C"))
    private char toolvisuals$dotPrefix(StringReader reader, Operation<Character> original) {
        return !commandsOnly && LocalCommandLine.suggests(reader.getString()) ? '/' : original.call(reader);
    }

    @WrapOperation(method = {"updateCommandInfo", "fillNodeUsage"}, at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/multiplayer/ClientPacketListener;getCommands()Lcom/mojang/brigadier/CommandDispatcher;"))
    private CommandDispatcher<?> toolvisuals$localDispatcher(ClientPacketListener connection, Operation<CommandDispatcher<?>> original) {
        return !commandsOnly && LocalCommandLine.suggests(input.getValue()) ? LocalSuggestions.dispatcher() : original.call(connection);
    }
}
