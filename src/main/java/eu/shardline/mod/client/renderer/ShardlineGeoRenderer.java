package eu.shardline.mod.client.renderer;

import eu.shardline.mod.Shardline;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * Standard-GeckoLib-Renderer fuer Shardline-Wesen. Laedt
 * geo/entity/&lt;name&gt;.geo.json, animations/entity/&lt;name&gt;.animation.json und textures/entity/&lt;name&gt;.png.
 * Mit {@code glowing} wird zusaetzlich textures/entity/&lt;name&gt;_glowmask.png leuchtend gerendert.
 */
public class ShardlineGeoRenderer<T extends Entity & GeoAnimatable> extends GeoEntityRenderer<T> {
    public ShardlineGeoRenderer(EntityRendererProvider.Context context, String name, float shadowRadius, boolean glowing) {
        this(context, new DefaultedEntityGeoModel<>(Shardline.id(name), true), shadowRadius, glowing);
    }

    public ShardlineGeoRenderer(EntityRendererProvider.Context context, GeoModel<T> model, float shadowRadius, boolean glowing) {
        super(context, model);
        this.shadowRadius = shadowRadius;
        if (glowing) {
            this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        }
    }
}
