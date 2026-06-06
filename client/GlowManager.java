package com.megu.neferpitou.client;

import com.megu.neferpitou.Neferpitou;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Controla o glowing (contorno) das entidades no client de forma centralizada.
 *
 * Varios sistemas (caca de creeper, cheirar) querem iluminar entidades. Se cada um
 * ligar/desligar o glow direto, eles brigam. Aqui cada sistema apenas REGISTRA o
 * conjunto de entidades que quer iluminar (por uma chave de "fonte"), e este
 * gerenciador aplica a UNIAO a cada tick, ligando os novos e desligando os que
 * nenhuma fonte quer mais.
 */
@Mod.EventBusSubscriber(modid = Neferpitou.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class GlowManager {

    private static final Map<String, Set<Integer>> REQUESTS = new HashMap<>();
    private static final Set<Integer> APPLIED = new HashSet<>();

    /** Define o conjunto de ids que esta fonte quer iluminar neste momento. */
    public static void setRequest(String source, Set<Integer> ids) {
        REQUESTS.put(source, ids);
    }

    /** Esta fonte nao quer iluminar nada agora. */
    public static void clearRequest(String source) {
        REQUESTS.remove(source);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            APPLIED.clear();
            REQUESTS.clear();
            return;
        }

        // Uniao de tudo que as fontes querem iluminar.
        Set<Integer> desired = new HashSet<>();
        for (Set<Integer> s : REQUESTS.values()) desired.addAll(s);

        // Liga os novos.
        for (int id : desired) {
            if (!APPLIED.contains(id)) {
                Entity e = mc.level.getEntity(id);
                if (e != null) e.setGlowingTag(true);
            }
        }
        // Desliga os que ninguem quer mais.
        for (int id : APPLIED) {
            if (!desired.contains(id)) {
                Entity e = mc.level.getEntity(id);
                if (e != null) e.setGlowingTag(false);
            }
        }

        APPLIED.clear();
        APPLIED.addAll(desired);
    }
}
