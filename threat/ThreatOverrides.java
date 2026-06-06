package com.megu.neferpitou.threat;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lista de overrides de forca, editavel pelo KubeJS EM TEMPO REAL. Tem PRIORIDADE
 * sobre o calculo automatico e sobre as tags.
 *
 * Uso no KubeJS (ex: server_scripts), com /reload para reaplicar:
 *
 *   const Threat = Java.loadClass('com.megu.neferpitou.threat.ThreatOverrides')
 *   Threat.clearAll()                                   // limpa o que ficou de antes
 *   Threat.set('minecraft:zombie', 'TRIVIAL')
 *   Threat.set('minecraft:warden', 'STRONG')
 *   Threat.set('saintsdragons:lightning_dragon', 'APEX')
 *
 * Niveis validos: TRIVIAL, WEAK, MEDIUM, STRONG, REALLY_STRONG, APEX, PREY.
 */
public final class ThreatOverrides {

    private ThreatOverrides() {}

    private static final Map<ResourceLocation, ThreatLevel> MAP = new ConcurrentHashMap<>();

    /** Define o tier de um tipo de entidade (ex: "minecraft:zombie", "WARDEN" etc). */
    public static void set(String entityType, String level) {
        ResourceLocation rl = ResourceLocation.tryParse(entityType);
        ThreatLevel lvl = parse(level);
        if (rl != null && lvl != null) {
            MAP.put(rl, lvl);
        }
    }

    public static void clear(String entityType) {
        ResourceLocation rl = ResourceLocation.tryParse(entityType);
        if (rl != null) MAP.remove(rl);
    }

    public static void clearAll() {
        MAP.clear();
    }

    /** Usado pelo ThreatClassifier. Retorna null se nao houver override. */
    public static ThreatLevel get(ResourceLocation type) {
        return type == null ? null : MAP.get(type);
    }

    private static ThreatLevel parse(String s) {
        if (s == null) return null;
        try {
            return ThreatLevel.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
