package com.viaversion.viafabricplus.features.emulation.recipe;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class RecipeManager1_11_2 {

    private final Multimap<RecipeType<?>, RecipeEntry<?>> recipesByType;
    private final Map<RegistryKey<Recipe<?>>, RecipeEntry<?>> recipesById;

    public RecipeManager1_11_2(final Iterable<RecipeEntry<?>> recipes) {
        final ImmutableMultimap.Builder<RecipeType<?>, RecipeEntry<?>> recipesByTypeBuilder = ImmutableMultimap.builder();
        final ImmutableMap.Builder<RegistryKey<Recipe<?>>, RecipeEntry<?>> recipesByIdBuilder = ImmutableMap.builder();

        for (RecipeEntry<?> recipeEntry : recipes) {
            final RecipeType<?> recipeType = recipeEntry.value().getType();
            recipesByTypeBuilder.put(recipeType, recipeEntry);
            recipesByIdBuilder.put(recipeEntry.id(), recipeEntry);
        }

        this.recipesByType = recipesByTypeBuilder.build();
        this.recipesById = recipesByIdBuilder.build();
    }

    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> getFirstMatch(final RecipeType<T> type, final I input, final World world) {
        if (input.isEmpty()) {
            return Optional.empty();
        } else {
            return this.recipesByType.get(type).stream().map(e -> (RecipeEntry<T>) e).filter(recipe -> recipe.value().matches(input, world)).findFirst();
        }
    }

    public Optional<RecipeEntry<?>> get(final RegistryKey<Recipe<?>> id) {
        return Optional.ofNullable(this.recipesById.get(id));
    }

    public Collection<RecipeEntry<?>> values() {
        return this.recipesById.values();
    }

    public Stream<RegistryKey<Recipe<?>>> keys() {
        return this.recipesById.keySet().stream();
    }

}
