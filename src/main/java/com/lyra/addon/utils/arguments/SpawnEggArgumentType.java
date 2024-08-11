package com.lyra.addon.utils.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SpawnEggArgumentType implements ArgumentType<ItemStackArgument> {
    private static final SpawnEggArgumentType INSTANCE = new SpawnEggArgumentType();

    private static final Collection<String> EXAMPLES = Registries.ITEM.stream()
        .map(Item::toString)
        .filter(name -> name.endsWith("spawn_egg"))
        .map(name -> name.replace("minecraft:", ""))
        .map(name -> name.replace("_spawn_egg", ""))
        .toList();
    private static final DynamicCommandExceptionType NO_SUCH_ENTITY = new DynamicCommandExceptionType(name -> Text.literal("Entity with name " + name + " doesn't exist."));

    public static SpawnEggArgumentType get() {
        return INSTANCE;
    }
    private SpawnEggArgumentType() {}



    @Override
    public ItemStackArgument parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();

        Identifier id;
        id = Identifier.ofVanilla( argument + "_spawn_egg");

        Registry<Item> itemRegistry = mc.world.getRegistryManager().get(RegistryKeys.ITEM);

        Item item = Objects.requireNonNull(itemRegistry.get(id));

        ItemStack stack = new ItemStack(item);


        if (!EXAMPLES.contains(argument)) throw NO_SUCH_ENTITY.create(argument);

        return new ItemStackArgument(stack.getRegistryEntry(), stack.getComponentChanges());
    }
    public static <S> ItemStackArgument getItemStack(CommandContext<S> context, String name) {
        return (ItemStackArgument)context.getArgument(name, ItemStackArgument.class);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(getExamples(), builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
