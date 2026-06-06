package com.megu.neferpitou.nen;

/**
 * Categorias de Nen que um mob hostil pode receber ao spawnar.
 * multiplier = quanto mais forte fica (vida/forca/resistencia x multiplier).
 * chance     = probabilidade de spawnar com essa categoria (a marginal e aproximada,
 *              pois o sorteio pega a categoria MAIS RARA que o numero sorteado alcanca).
 *
 * Velocidade NAO usa o multiplier cheio: o bonus e multiplier / nenSpeedDivisor (100),
 * entao 2x = +2% de velocidade, 1000x = +1000%.
 */
public enum NenTier {
    T2(2, 1.0e-1),       // ~1 em 10
    T5(5, 1.0e-2),       // ~1 em 100
    T10(10, 1.0e-3),     // ~1 em 1.000
    T25(25, 1.0e-4),     // ~1 em 10.000
    T50(50, 1.0e-5),     // ~1 em 100.000
    T500(500, 1.0e-6),   // ~1 em 1.000.000
    T1000(1000, 1.0e-7); // ~1 em 10.000.000

    public final int multiplier;
    public final double chance;

    NenTier(int multiplier, double chance) {
        this.multiplier = multiplier;
        this.chance = chance;
    }

    // do mais raro para o mais comum: assim o mob ganha a categoria mais forte que o sorteio permitir
    private static final NenTier[] RAREST_FIRST = { T1000, T500, T50, T25, T10, T5, T2 };

    /**
     * Sorteia uma categoria a partir de um numero r em [0,1). Categorias com multiplier
     * acima de maxMultiplier sao ignoradas (permite "tirar os mais fortes" pelo config).
     * Retorna null se o mob nao deve ganhar Nen.
     */
    public static NenTier roll(double r, double maxMultiplier) {
        for (NenTier t : RAREST_FIRST) {
            if (t.multiplier <= maxMultiplier && r < t.chance) {
                return t;
            }
        }
        return null;
    }
}
