package me.junioraww.tails.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.math.Transformation;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.junioraww.tails.Main;
import me.junioraww.tails.data.types.Animation;
import me.junioraww.tails.data.types.Matrix4fDeserializer;
import me.junioraww.tails.data.types.Transform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Equip implements CommandExecutor {
    public Equip() {

    }

    public void action(Player sender) throws IOException {
        Gson g = new GsonBuilder()
                .registerTypeAdapter(Matrix4f.class, new Matrix4fDeserializer())
            .create();

        File file = new File("fox.json");
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

        /*ByteBuf nettyBuffer = Unpooled.buffer();
        FriendlyByteBuf buf = new FriendlyByteBuf(nettyBuffer);
        buf.writeInt(nmsPlayer.getId());
        buf.writeVarIntArray();*/

        var passengerPacket = new ClientboundSetPassengersPacket(nmsPlayer);
        nmsPlayer.connection.send(passengerPacket);

        sender.sendMessage(String.valueOf(animation.animations.values().iterator().next().frames.size()));
        var anime = animation.animations.values().iterator().next();

        @SuppressWarnings("unchecked")
        Map<UUID, Transform>[] frames = new Map[anime.duration];
        Arrays.fill(frames, Collections.emptyMap());

        for (var frame : anime.frames) {
            frames[(int) (frame.time * 20)] = frame.node_transforms;
            sender.sendMessage(frame.time + " " + frame.node_transforms.size());
        }

        final int[] i = {0};
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getPlugin(), () -> {
            float yaw = getMovementYaw(sender);

            if (i[0] >= anime.duration - 1) i[0] = 0;
            for (Map.Entry<UUID, Transform> modified : frames[i[0]].entrySet()) {
                UUID uuid = modified.getKey();
                Transform transform = modified.getValue();
                Display.BlockDisplay display = displays.get(uuid);
                if (display != null) {
                    display.setTransformationInterpolationDuration(1);
                    display.setTransformationInterpolationDelay(0);

                    Transformation currentTransformation = new Transformation(transform.matrix);
                    Matrix4f currentMatrix = new Matrix4f(currentTransformation.getMatrix());
                    float angleRadians = (float) Math.toRadians(180 - yaw);
                    Matrix4f rotationMatrixY = new Matrix4f().rotateY(angleRadians).translate(0,-1.9f,0.5f);

                    /*Vector3f offset = new Vector3f(-1, 0, 0);
                    offset.rotateY(angleRadians);
                    Matrix4f finalMatrix = new Matrix4f().translate(offset.x, 0, offset.z);
                    currentMatrix = currentMatrix.add(finalMatrix);*/

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

    public static float getMovementYaw(Player player) {
        String name = player.getName();

        Vector3f current = new Vector3f(
                (float) player.getLocation().getX(),
                0f,
                (float) player.getLocation().getZ()
        ); // текущее местоположение

        Vector3f last = lastPositions.getOrDefault(name, new Vector3f(current));
        lastPositions.put(name, new Vector3f(current)); // обновляем последнее положение

        Vector3f delta = new Vector3f(current).sub(last); // вектор направления
        float bodyYaw = player.getBodyYaw(); // куда направлено тело игрока

        if (delta.lengthSquared() < 0.0001f) {
            return bodyYaw; // стоит на месте
        }

        // куда смотрит игрок
        double yawRad = Math.toRadians(player.getYaw());
        Vector3f direction = new Vector3f(
                (float) -Math.sin(yawRad),
                0f,
                (float) Math.cos(yawRad)
        );

        float diff = new Vector3f(direction).add(delta).lengthSquared();
        float angle = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));

        if (diff < 1) {
            return angle - 180f;
        } else {
            return angle;
        }
    }

    private static final Map<String, Vector3f> lastPositions = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            try {
                action(player);
            } catch (IOException e) {
                player.sendRichMessage("<red>Ошибка!");
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }
}
