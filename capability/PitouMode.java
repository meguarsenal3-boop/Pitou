package com.megu.neferpitou.capability;

/**
 * Tiers de progressao da Pitou. Sao LINEARES e cumulativos:
 * para ter Hatsu e preciso ter Nen; Terpsichora e a forma final.
 * O estado guarda o tier MAIS ALTO desbloqueado; tudo abaixo dele esta disponivel.
 *
 * Estados que LIGAM/DESLIGAM (En ativo, Ryu ativo, Doctor Blythe ativo,
 * Terpsichora ativa) NAO ficam aqui - sao flags separadas em PitouData,
 * porque sao "ativacoes" e nao "desbloqueios".
 */
public enum PitouMode {
    IDENTITY,     // base: gato, garras, dash, pulo, buff do Rei...
    NEN,          // desbloqueia Ten/Ren/Ryu/En/Ko...
    HATSU,        // desbloqueia Doctor Blythe
    TERPSICHORA;  // forma final + Volta da Morte

    private static final PitouMode[] VALUES = values();

    public boolean hasReached(PitouMode required) {
        return this.ordinal() >= required.ordinal();
    }

    public PitouMode next() {
        int i = ordinal() + 1;
        return i < VALUES.length ? VALUES[i] : this;
    }

    public static PitouMode byOrdinalSafe(int ordinal) {
        if (ordinal < 0) return IDENTITY;
        if (ordinal >= VALUES.length) return TERPSICHORA;
        return VALUES[ordinal];
    }

    public static PitouMode byNameSafe(String name) {
        for (PitouMode m : VALUES) {
            if (m.name().equalsIgnoreCase(name)) return m;
        }
        return null;
    }
}
