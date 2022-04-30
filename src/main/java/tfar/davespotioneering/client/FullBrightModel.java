package tfar.davespotioneering.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;

public class FullBrightModel extends BakedModelWrapper<BakedModel> {

    private static final LoadingCache<CacheKey, List<BakedQuad>> CACHE = CacheBuilder.newBuilder().build(new CacheLoader<CacheKey, List<BakedQuad>>() {
        @Override
        public List<BakedQuad> load(@Nonnull CacheKey key) {
            return transformQuads(key.base.getQuads(key.state, key.side, key.random, EmptyModelData.INSTANCE), key.textures);
        }
    });

    public static void invalidateCache() {
        CACHE.invalidateAll();
    }

    private final Set<ResourceLocation> textures;
    private final boolean doCaching;
    private Predicate<BlockState> state = null;

    public FullBrightModel(BakedModel base, boolean doCaching, ResourceLocation... textures) {
        super(base);

        this.textures = new HashSet<>(Arrays.asList(textures));
        this.doCaching = doCaching;
    }

    public FullBrightModel(BakedModel base, boolean doCaching, Predicate<BlockState> state, ResourceLocation... textures) {
        super(base);

        this.textures = new HashSet<>(Arrays.asList(textures));
        this.doCaching = doCaching;
        this.state = state;
    }

    @Override
    public boolean doesHandlePerspectives() {
        return false;
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack mat) {
        return net.minecraftforge.client.ForgeHooksClient.handlePerspective(getBakedModel(), cameraTransformType, mat);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        if (state == null) {
            return transformQuads(originalModel.getQuads(state,null, rand), textures);
        }

        if (this.state != null && !this.state.test(state)) {
            return originalModel.getQuads(state, side, rand);
        }

        if (!doCaching) {
            return transformQuads(originalModel.getQuads(state, side, rand), textures);
        }

        return CACHE.getUnchecked(new CacheKey(originalModel, textures, rand, state, side));
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData data) {
        if (state == null) {
            return originalModel.getQuads(null, side, rand, data);
        }

        if (this.state != null && !this.state.test(state)) {
            return originalModel.getQuads(state, side, rand, data);
        }

        if (!doCaching) {
            return transformQuads(originalModel.getQuads(state, side, rand, data), textures);
        }

        return CACHE.getUnchecked(new CacheKey(originalModel, textures, rand, state, side));
    }

    private static List<BakedQuad> transformQuads(List<BakedQuad> oldQuads, Set<ResourceLocation> textures) {
        List<BakedQuad> quads = new ArrayList<>(oldQuads);

        for (int i = 0; i < quads.size(); ++i) {
            BakedQuad quad = quads.get(i);

            if (textures.contains(quad.getSprite().getName())) {
                quads.set(i, transformQuad(quad));
            }
        }

        return quads;
    }

    private static BakedQuad transformQuad(BakedQuad quad) {
        int[] vertexData = quad.getVertices().clone();
        int step = vertexData.length / 4;

        // Set lighting to fullbright on all vertices
        vertexData[6] = 0x00F000F0;
        vertexData[6 + step] = 0x00F000F0;
        vertexData[6 + 2 * step] = 0x00F000F0;
        vertexData[6 + 3 * step] = 0x00F000F0;

        return new BakedQuad(
                vertexData,
                quad.getTintIndex(),
                quad.getDirection(),
                quad.getSprite(),
                quad.isShade()
        );
    }

    private static class CacheKey {
        private final BakedModel base;
        private final Set<ResourceLocation> textures;
        private final Random random;
        private final BlockState state;
        private final Direction side;

        public CacheKey(BakedModel base, Set<ResourceLocation> textures, Random random, BlockState state, Direction side) {
            this.base = base;
            this.textures = textures;
            this.random = random;
            this.state = state;
            this.side = side;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CacheKey cacheKey = (CacheKey) o;

            if (cacheKey.side != side) {
                return false;
            }

            return state.equals(cacheKey.state);
        }

        @Override
        public int hashCode() {
            return state.hashCode() + (31 * (side != null ? side.hashCode() : 0));
        }
    }


    public static class UnbakedFullBrightModel implements IModelGeometry<UnbakedFullBrightModel> {

        private final BlockModel baseModel;

        public UnbakedFullBrightModel(BlockModel baseModel) {
            this.baseModel = baseModel;
        }

        @Override
        public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material,
                TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
            return new FullBrightModel(baseModel.bake(bakery, baseModel, spriteGetter, modelTransform, modelLocation, true),false,new ResourceLocation("davespotioneering:item/lit_potioneer_gauntlet_bright"));
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            return baseModel.getMaterials(modelGetter, missingTextureErrors);
        }
    }
}
