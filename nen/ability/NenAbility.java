package com.megu.neferpitou.nen.ability;

/** Habilidades de Nen que ligam/desligam por tecla. */
public enum NenAbility {
    RYU,
    EN,
    REN;

    private static final NenAbility[] VALUES = values();

    public static NenAbility byId(int id) {
        return (id >= 0 && id < VALUES.length) ? VALUES[id] : RYU;
    }
}
