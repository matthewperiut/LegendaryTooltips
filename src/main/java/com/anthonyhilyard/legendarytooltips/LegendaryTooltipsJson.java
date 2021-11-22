package com.anthonyhilyard.legendarytooltips;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public class LegendaryTooltipsJson implements SimpleResourceReloadListener<LegendaryToolTipsStorage>
{
    public static final LegendaryTooltipsJson INSTANCE = new LegendaryTooltipsJson();
    private static final JsonParser JSON_PARSER = new JsonParser();

    private LegendaryTooltipsJson()
    {

    }

    @Override
    public CompletableFuture<LegendaryToolTipsStorage> load(ResourceManager resourceManager, ProfilerFiller profilerFiller, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadTooltips(resourceManager);
            } catch (IOException e) {
                Loader.LOGGER.error("Failed to load colored light mappings", e);
                return new LegendaryToolTipsStorage();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(LegendaryToolTipsStorage storage, ResourceManager resourceManager, ProfilerFiller profilerFiller, Executor executor) {
        LegendaryToolTipsStorage.instance = storage;
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(Loader.MODID, "tooltips");
    }

    private LegendaryToolTipsStorage loadTooltips(ResourceManager manager) throws IOException {
        {
            LegendaryToolTipsStorage storage = new LegendaryToolTipsStorage();
            for (var resource : manager.getResources(new ResourceLocation(Loader.MODID, "tooltips.json"))) {
                try (var input = resource.getInputStream()) {
                    var root = JSON_PARSER.parse(new InputStreamReader(input)).getAsJsonObject();

                    var mappings = GsonHelper.getAsJsonObject(root, "tooltips");

                    for (var entry : mappings.entrySet()) {
                        storage.names.add(entry.getKey());
                        storage.values.add(entry.getValue().getAsInt());
                    }

                } catch (JsonSyntaxException e) {
                    Loader.LOGGER.error("Failed to parse mappings at {}", resource.getLocation(), e);
                }
            }
            return storage;
        }
    }
}
