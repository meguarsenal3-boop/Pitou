package com.megu.neferpitou.threat;

/**
 * Tier de ameaca de uma entidade (do mais fraco ao mais forte):
 *
 *  TRIVIAL       - pacificos e muito fracos (zumbis).
 *  WEAK          - piglin brutes, endermans.
 *  MEDIUM        - iron golem.
 *  STRONG        - wither, ender dragon, warden.
 *  REALLY_STRONG - comparaveis a forca da Pitou (no vanilla, so com mods de aura depois).
 *  APEX          - mais fortes que ela, mas ainda venciveis.
 *  PREY          - praticamente impossiveis de vencer.
 *
 * A CACA de seres fortes dispara de STRONG para cima (isStrongPrey).
 */
public enum ThreatLevel {
    TRIVIAL,
    WEAK,
    MEDIUM,
    STRONG,
    REALLY_STRONG,
    APEX,
    PREY;

    /** Conta como "ser forte" para a caca (STRONG ou acima). */
    public boolean isStrongPrey() {
        return this.ordinal() >= STRONG.ordinal();
    }
}
