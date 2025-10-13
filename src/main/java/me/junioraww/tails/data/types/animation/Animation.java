package me.junioraww.tails.data.types.animation;

import java.util.Map;
import java.util.UUID;

public class Animation {
    public Map<UUID, AnimatedEntity> nodes;
    public Map<UUID, NamedAnimation> animations;
}
