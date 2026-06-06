package com.megu.neferpitou.util;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia o glow NATIVO no servidor como UNIAO de varias fontes (cheiro, rei, etc).
 *
 * Cada fonte registra o conjunto de entidades que quer ver brilhando, usando uma chave
 * unica (ex: "smell:<uuid>", "king:<uuid>"). A cada tick o manager calcula a uniao de
 * todas as fontes e liga/desliga o glow de acordo, sem que uma fonte apague o que a
 * outra acendeu.
 *
 * Lembrete: o glow nativo do servidor e GLOBAL (todos que veem o mob veem o brilho) e
 * atravessa paredes. E o unico glow que renderiza de forma confiavel no client do davig.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerGlowManager {

    private ServerGlowManager() {}

    private static final Map<String, Set<Entity>> REQUESTS = new ConcurrentHashMap<>();
    private static final Set<Entity> APPLIED = Collections.newSetFromMap(new IdentityHashMap<>());

    /** Registra/atualiza o conjunto desejado de uma fonte. Passe um conjunto novo a cada update. */
    public static void setRequest(String source, Set<Entity> entities) {
        if (entities == null || entities.isEmpty()) {
            REQUESTS.remove(source);
        } else {
            REQUESTS.put(source, entities);
        }
    }

    /** Remove uma fonte (ex: parou de cheirar, deslogou). */
    public static void clearRequest(String source) {
        REQUESTS.remove(source);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Set<Entity> desired = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Set<Entity> set : REQUESTS.values()) {
            desired.addAll(set);
        }

        // liga o glow do que deve brilhar
        for (Entity e : desired) {
            if (e != null && e.isAlive() && !e.hasGlowingTag()) {
                e.setGlowingTag(true);
            }
        }

        // desliga o que brilhava e nao deve mais
        for (Entity e : APPLIED) {
            if (e != null && !desired.contains(e)) {
                if (e.isAlive()) e.setGlowingTag(false);
            }
        }

        APPLIED.clear();
        APPLIED.addAll(desired);
    }
}
