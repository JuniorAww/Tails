package me.junioraww.tails.data.types.animation;

import java.util.Map;
import java.util.UUID;

public class AnimationContainer {
    public Map<UUID, Transform>[] idle;
    public Map<UUID, Transform>[] walking;
}
