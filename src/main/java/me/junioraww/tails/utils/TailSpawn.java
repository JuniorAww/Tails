package me.junioraww.tails.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.math.Transformation;
import me.junioraww.tails.Main;
import me.junioraww.tails.data.types.animation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class TailSpawn {
    public static void action(Player sender) throws IOException {
        Gson g = new GsonBuilder()
                .registerTypeAdapter(Matrix4f.class, new Matrix4fDeserializer())
                .create();

        File file = new File("plugins/Tails/fox.json");
        String data = Files.readString(file.toPath());
        Animation animation = g.fromJson(data, Animation.class);

        if (animation != null) {
            sender.sendMessage(String.valueOf(animation.nodes.size()));
        }

        World world = sender.getWorld();
        Location center = sender.getLocation().clone();

        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        ServerPlayer nmsPlayer = ((CraftPlayer) sender).getHandle();

        BlockPos blockPos = new BlockPos(center.getBlockX(), center.getBlockY(), center.getBlockZ());
        Map<UUID, Display.BlockDisplay> displays = new HashMap<>();

        for (var entry : animation.nodes.entrySet()) {
            UUID key = entry.getKey();
            var value = entry.getValue();
            Transform t = value.default_transform;

            Display.BlockDisplay display = EntityType.BLOCK_DISPLAY.create(
                    nmsWorld,
                    null,
                    blockPos,
                    EntitySpawnReason.COMMAND,
                    false,
                    false
            );

            display.startRiding(nmsPlayer, true);
            display.setTransformation(new Transformation(t.matrix));
            display.setTransformationInterpolationDuration(1);
            display.setTransformationInterpolationDelay(0);

            sender.sendRichMessage("<green>" + value.block);

            ResourceLocation id = ResourceLocation.parse(value.block);
            Optional<Holder.Reference<Block>> optionalHolder = BuiltInRegistries.BLOCK.get(id);

            if (optionalHolder.isPresent()) {
                Holder<Block> holder = optionalHolder.get();
                Block block = holder.value();
                BlockState defaultState = block.defaultBlockState();
                display.setBlockState(defaultState);
            } else {
                System.out.println("Block not found: " + id);
            }

            ClientboundAddEntityPacket spawnPacket = new ClientboundAddEntityPacket(
                    display.getId(),
                    key,
                    display.getX(),
                    display.getY() + 0.5,
                    display.getZ(),
                    0f,
                    0f,
                    display.getType(),
                    0,
                    display.getDeltaMovement(),
                    0.0
            );

            ClientboundSetEntityDataPacket dataPacket =
                    new ClientboundSetEntityDataPacket(display.getId(), display.getEntityData().packDirty());

            nmsPlayer.connection.send(spawnPacket);
            nmsPlayer.connection.send(dataPacket);
            displays.put(key, display);
        }

        var passengerPacket = new ClientboundSetPassengersPacket(nmsPlayer);
        nmsPlayer.connection.send(passengerPacket);

        sender.sendMessage(String.valueOf(animation.animations.values().iterator().next().frames.size()));
        var animations = animation.animations;

        NamedAnimation idle = animations.values().stream()
                .filter(anime -> Objects.equals(anime.name, "idle")).findFirst().get();
        NamedAnimation walking = animations.values().stream()
                .filter(anime -> Objects.equals(anime.name, "walking")).findFirst().get();

        AnimationContainer container = new AnimationContainer();

        @SuppressWarnings("unchecked")
        Map<UUID, Transform>[] idleArray = (Map<UUID, Transform>[]) new Map[idle.duration];
        @SuppressWarnings("unchecked")
        Map<UUID, Transform>[] walkingArray = (Map<UUID, Transform>[]) new Map[walking.duration];

        container.idle = idleArray;
        container.walking = walkingArray;

        Arrays.fill(container.idle, Collections.emptyMap());

        for (var frame : idle.frames) {
            idleArray[(int) (frame.time * 20)] = frame.node_transforms;
        }
        for (var frame : walking.frames) {
            walkingArray[(int) (frame.time * 20)] = frame.node_transforms;
        }

        final int[] i = {0};
        final boolean[] prevState = {false};
        final int idleDur = idle.duration;
        final int walkDur = walking.duration;

        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getPlugin(), () -> {
            float yaw = getMovementYaw(nmsPlayer);
            boolean sneak = sender.isSneaking();

            boolean running = sender.isSprinting() || sender.getVelocity().length() > 0.1;
            if (prevState[0] != running) {
                i[0] = 0;
                prevState[0] = running;
            }

            if (i[0] >= (running ? walkDur : idleDur) - 1) i[0] = 0;
            for (Map.Entry<UUID, Transform> modified : (running ? walkingArray : idleArray)[i[0]].entrySet()) {
                UUID uuid = modified.getKey();
                Transform transform = modified.getValue();
                Display.BlockDisplay display = displays.get(uuid);
                if (display != null) {
                    display.setTransformationInterpolationDuration(1);
                    display.setTransformationInterpolationDelay(0);

                    Transformation currentTransformation = new Transformation(transform.matrix);
                    Matrix4f currentMatrix = new Matrix4f(currentTransformation.getMatrix());
                    float angleRadians = (float) Math.toRadians(180 - yaw);
                    Matrix4f rotationMatrixY = new Matrix4f().rotateY(angleRadians).translate(0,-1.9f, sneak ? 0.75f : 0.5f);

                    Matrix4f newMatrix = rotationMatrixY.mul(currentMatrix);

                    Transformation newTransformation = new Transformation(newMatrix);
                    display.setTransformation(newTransformation);

                    if (display.getEntityData().isDirty()) {
                        ClientboundSetEntityDataPacket packet =
                                new ClientboundSetEntityDataPacket(display.getId(), display.getEntityData().packDirty());
                        nmsPlayer.connection.send(packet);
                    }
                }
            }
            i[0]++;
        }, 1L, 1L);
    }

    public static float getMovementYaw(ServerPlayer player) {
        String name = player.displayName;

        Vector3f current = new Vector3f(
                (float) player.position().x,
                0f,
                (float) player.position().z
        ); // текущее местоположение

        if (!lastPositions.containsKey(name)) {
            lastPositions.put(name, new Vector3f(current));
            return player.yBodyRot;
        }

        Vector3f last = lastPositions.get(name);
        if (current.equals(last) && lastYaw.containsKey(name)) {
            player.getBukkitEntity().sendMessage(last + " " + current);
            return lastYaw.get(name);
        }
        lastPositions.put(name, current);

        Vector3f delta = new Vector3f(current).sub(last);

        // куда смотрит игрок
        double yawRad = Math.toRadians(player.getBukkitYaw());
        Vector3f direction = new Vector3f(
                (float) -Math.sin(yawRad),
                0f,
                (float) Math.cos(yawRad)
        );

        float diff = new Vector3f(direction).add(delta).lengthSquared();
        float angle = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));

        float yaw = diff < 1 ? angle - 180f : angle;
        lastYaw.put(name, yaw);
        return yaw;
    }

    private static final Map<String, Vector3f> lastPositions = new HashMap<>();
    private static final Map<String, Float> lastYaw = new HashMap<>();
}
