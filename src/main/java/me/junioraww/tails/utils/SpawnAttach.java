package me.junioraww.tails.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.math.Transformation;
import me.junioraww.tails.Main;
import me.junioraww.tails.data.types.animation.*;
import me.junioraww.tails.data.types.items.Attach;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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
import java.util.concurrent.ConcurrentHashMap;

public class SpawnAttach {
    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Matrix4f.class, new Matrix4fDeserializer())
            .create();

    /*
    todo code review & shrink
    todo timeouts
     */
    public static void wear(Player sender, Attach attach) throws IOException {
        if (players.contains(sender)) {
            sender.sendRichMessage("<red>Вы уже надели хвост!");
            return;
        }

        Attach.Meta meta = attach.getMeta();

        File file = new File(meta.getUri());
        String data = Files.readString(file.toPath());
        Animation animation = gson.fromJson(data, Animation.class);

        if (animation == null) {
            return;
        }

        World world = sender.getWorld();
        Location center = sender.getLocation().clone();

        ServerLevel nmsWorld = ((CraftWorld) world).getHandle();
        ServerPlayer nmsPlayer = ((CraftPlayer) sender).getHandle();

        BlockPos blockPos = new BlockPos(center.getBlockX(), center.getBlockY(), center.getBlockZ());
        Map<UUID, Display.BlockDisplay> displays = new HashMap<>();

        int size = animation.nodes.size();
        int[] entityIds = new int[size];
        var spawnPackets = new Packet[size];
        var dataPackets = new Packet[size + 1];

        int idx = 0;

        for (Map.Entry<UUID, AnimatedEntity> entry : animation.nodes.entrySet()) {
            UUID key = entry.getKey();
            AnimatedEntity value = entry.getValue();
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

            entityIds[idx] = display.getId();

            spawnPackets[idx] = new ClientboundAddEntityPacket(
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

            dataPackets[idx] = new ClientboundSetEntityDataPacket(display.getId(), display.getEntityData().packDirty());

            displays.put(key, display);

            idx++;
        }

        var destroyPacket = new ClientboundRemoveEntitiesPacket(entityIds);

        dataPackets[idx] = new ClientboundSetPassengersPacket(nmsPlayer);

        /*
        Загружаем анимации в память
         */
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
        final boolean[] prevState = { false };
        final int idleDur = idle.duration;
        final int walkDur = walking.duration;
        final World[] curWorld = {sender.getWorld()};

        Chunk chunk = sender.getChunk();
        final List<Player> watching = new ArrayList<>(sender.getWorld().getPlayersSeeingChunk(chunk.getX(), chunk.getZ()));

        for (var watcher : watching) {
            var connection = ((CraftPlayer) watcher).getHandle().connection;
            for (var p : spawnPackets) connection.send(p);
            for (var p : dataPackets) connection.send(p);
        }

        players.add(sender);

        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getPlugin(), timer -> {
            boolean equipped = players.contains(sender);
            if (!equipped || !sender.isOnline()) {
                timer.cancel();

                if (equipped) players.remove(sender);

                for (var watcher : watching) {
                    if (watcher.isOnline())
                        ((CraftPlayer) watcher).getHandle().connection.send(destroyPacket);
                }

                displays.clear();
                watching.clear();
            }
            else {
                if (!curWorld[0].equals(sender.getWorld())) {
                    curWorld[0] = sender.getWorld();
                    var newLevel = ((CraftWorld) curWorld[0]).getHandle();
                    displays.forEach((uuid, display) -> {
                        display.teleportTo(newLevel, sender.getX(), sender.getY(), sender.getZ(), Set.of(), 0, 0, false);
                    });
                    watching.clear();
                }

                float yaw = getMovementYaw(nmsPlayer);
                boolean sneak = sender.isSneaking();

                boolean running = sender.isSprinting() || sender.getVelocity().length() > 0.1;
                if (prevState[0] != running) {
                    i[0] = 0;
                    prevState[0] = running;
                }

                if (Bukkit.getCurrentTick() % 20 == 0) {
                    Chunk _chunk = sender.getChunk();
                    var chunkWatchers = sender.getWorld().getPlayersSeeingChunk(_chunk.getX(), _chunk.getZ());
                    for (var player : watching) {
                        if (!player.isOnline()) {
                            watching.remove(player);
                        }
                        else if (!chunkWatchers.contains(player)) {
                            ((CraftPlayer) player).getHandle().connection.send(destroyPacket);
                            watching.remove(player);
                        }
                    }
                    for (var player : chunkWatchers) {
                        if (!watching.contains(player)) {
                            var conn = ((CraftPlayer) player).getHandle().connection;
                            for (var p : spawnPackets) conn.send(p);
                            for (var p : dataPackets) conn.send(p);
                            watching.add(player);
                        }
                    }
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
                        Matrix4f rotationMatrixY = new Matrix4f().rotateY(angleRadians).translate(0, -1.9f, sneak && !sender.isFlying() ? 0.75f : 0.5f);

                        Matrix4f newMatrix = rotationMatrixY.mul(currentMatrix);

                        Transformation newTransformation = new Transformation(newMatrix);
                        display.setTransformation(newTransformation);

                        if (display.getEntityData().isDirty()) {
                            ClientboundSetEntityDataPacket packet =
                                    new ClientboundSetEntityDataPacket(display.getId(), display.getEntityData().packDirty());
                            for (var watcher : watching) {
                                ((CraftPlayer) watcher).getHandle().connection.send(packet);
                            }
                        }
                    }
                }
                i[0]++;
            }
        }, 1L, 1L);
    }

    public static void unwear(Player player) {
        if (!players.contains(player)) return;
        players.remove(player);
        player.sendRichMessage("<green>Хвост снят!");
    }


    public static boolean isEquipped(Player player) {
        return players.contains(player);
    }

    private static final Map<String, Vector3f> lastPositions = new ConcurrentHashMap<>();
    private static final Map<String, Float> lastYaw = new ConcurrentHashMap<>();

    public static float getMovementYaw(ServerPlayer player) {
        String name = player.getGameProfile().getName();

        Vector3f currentPos = new Vector3f(
                (float) player.position().x,
                0f,
                (float) player.position().z
        );

        if (!lastPositions.containsKey(name)) {
            lastPositions.put(name, new Vector3f(currentPos));
            lastYaw.put(name, player.yBodyRot);
            return player.yBodyRot;
        }

        Vector3f lastPos = lastPositions.get(name);
        Vector3f delta = new Vector3f(currentPos).sub(lastPos);
        lastPositions.put(name, new Vector3f(currentPos));

        float finalBodyYaw;

        if (delta.lengthSquared() < 1.0E-7) {
            float lastBodyYaw = lastYaw.getOrDefault(name, player.yBodyRot);
            float headYaw = player.getYRot();
            float yawDifference = normalizeAngle(headYaw - lastBodyYaw);

            float deadZone = 45.0f;

            if (Math.abs(yawDifference) > deadZone) {
                float turnAmount = Math.abs(yawDifference) - deadZone;

                finalBodyYaw = lastBodyYaw + (turnAmount * Math.signum(yawDifference));
            } else {
                finalBodyYaw = lastBodyYaw;
            }

        } else {
            float headYaw = player.getYRot();
            float movementYaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
            float yawDifference = normalizeAngle(headYaw - movementYaw);

            if (Math.abs(yawDifference) > 95.0f) {
                finalBodyYaw = headYaw;
            } else {
                float offset = yawDifference * 0.6f;
                offset = Math.max(-45.0f, Math.min(45.0f, offset));
                finalBodyYaw = headYaw - offset;
            }
        }

        lastYaw.put(name, finalBodyYaw);
        return finalBodyYaw;
    }

    // диапазон [-180, 180]
    public static float normalizeAngle(float angle) {
        angle %= 360.0f;
        if (angle >= 180.0f) {
            angle -= 360.0f;
        }
        if (angle < -180.0f) {
            angle += 360.0f;
        }
        return angle;
    }

    private static final List<Player> players = new ArrayList<>();
}
