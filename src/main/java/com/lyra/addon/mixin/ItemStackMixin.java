package com.lyra.addon.mixin;

import com.lyra.addon.modules.DisableTooltips;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Mixin(ItemStack.class)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ItemStackMixin {
    @Inject(at = @At("HEAD"), method = "getTooltipData()Ljava/util/Optional;", cancellable = true)
    private void onGetTooltipData(CallbackInfoReturnable<Optional<TooltipData>> ci) {

    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/item/ItemStack;getTooltip"
        + "(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/item/TooltipContext;)Ljava/util/List;", cancellable = true)
    private void onGetTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> ci) {
        DisableTooltips module = Modules.get().get(DisableTooltips.class);
        if(module.isActive()) {
            Text text = Text.of(module.getTooltipText().get());
            List<Text> textList = Arrays.asList(text);
            ci.setReturnValue(textList);
        }
        return;
    }
}
