package ru.hachclient.utils.render;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.render.RenderLayer;
// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.render.VertexConsumer;
// created by ЗНАХАРКА АФТОДИЯ
import net.minecraft.client.render.VertexConsumerProvider;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
public class HandsTintProvider implements VertexConsumerProvider {
// created by ЗНАХАРКА АФТОДИЯ
    private final VertexConsumerProvider parent;
// created by ЗНАХАРКА АФТОДИЯ
    private final int tint;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    public HandsTintProvider(VertexConsumerProvider parent, int tint) {
// created by ЗНАХАРКА АФТОДИЯ
        this.parent = parent;
// created by ЗНАХАРКА АФТОДИЯ
        this.tint = tint;
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    @Override
// created by ЗНАХАРКА АФТОДИЯ
    public VertexConsumer getBuffer(RenderLayer layer) {
// created by ЗНАХАРКА АФТОДИЯ
        return new Tinted(parent.getBuffer(layer), tint);
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
    private static class Tinted implements VertexConsumer {
// created by ЗНАХАРКА АФТОДИЯ
        private final VertexConsumer parent;
// created by ЗНАХАРКА АФТОДИЯ
        private final int tr;
// created by ЗНАХАРКА АФТОДИЯ
        private final int tg;
// created by ЗНАХАРКА АФТОДИЯ
        private final int tb;
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        Tinted(VertexConsumer parent, int tint) {
// created by ЗНАХАРКА АФТОДИЯ
            this.parent = parent;
// created by ЗНАХАРКА АФТОДИЯ
            this.tr = tint >> 16 & 0xFF;
// created by ЗНАХАРКА АФТОДИЯ
            this.tg = tint >> 8 & 0xFF;
// created by ЗНАХАРКА АФТОДИЯ
            this.tb = tint & 0xFF;
// created by ЗНАХАРКА АФТОДИЯ
        }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        @Override
// created by ЗНАХАРКА АФТОДИЯ
        public VertexConsumer vertex(float x, float y, float z) {
// created by ЗНАХАРКА АФТОДИЯ
            parent.vertex(x, y, z);
// created by ЗНАХАРКА АФТОДИЯ
            return this;
// created by ЗНАХАРКА АФТОДИЯ
        }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        @Override
// created by ЗНАХАРКА АФТОДИЯ
        public VertexConsumer color(int red, int green, int blue, int alpha) {
// created by ЗНАХАРКА АФТОДИЯ
            parent.color(red * tr / 255, green * tg / 255, blue * tb / 255, alpha);
// created by ЗНАХАРКА АФТОДИЯ
            return this;
// created by ЗНАХАРКА АФТОДИЯ
        }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        @Override
// created by ЗНАХАРКА АФТОДИЯ
        public VertexConsumer texture(float u, float v) {
// created by ЗНАХАРКА АФТОДИЯ
            parent.texture(u, v);
// created by ЗНАХАРКА АФТОДИЯ
            return this;
// created by ЗНАХАРКА АФТОДИЯ
        }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        @Override
// created by ЗНАХАРКА АФТОДИЯ
        public VertexConsumer overlay(int u, int v) {
// created by ЗНАХАРКА АФТОДИЯ
            parent.overlay(u, v);
// created by ЗНАХАРКА АФТОДИЯ
            return this;
// created by ЗНАХАРКА АФТОДИЯ
        }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        @Override
// created by ЗНАХАРКА АФТОДИЯ
        public VertexConsumer light(int u, int v) {
// created by ЗНАХАРКА АФТОДИЯ
            parent.light(u, v);
// created by ЗНАХАРКА АФТОДИЯ
            return this;
// created by ЗНАХАРКА АФТОДИЯ
        }
// created by ЗНАХАРКА АФТОДИЯ

// created by ЗНАХАРКА АФТОДИЯ
        @Override
// created by ЗНАХАРКА АФТОДИЯ
        public VertexConsumer normal(float x, float y, float z) {
// created by ЗНАХАРКА АФТОДИЯ
            parent.normal(x, y, z);
// created by ЗНАХАРКА АФТОДИЯ
            return this;
// created by ЗНАХАРКА АФТОДИЯ
        }
// created by ЗНАХАРКА АФТОДИЯ
    }
// created by ЗНАХАРКА АФТОДИЯ
}
// created by ЗНАХАРКА АФТОДИЯ
