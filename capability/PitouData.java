package com.megu.neferpitou.capability;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * Estado da Pitou anexado a um player. Tudo o que precisa sobreviver a morte,
 * relog e troca de dimensao mora aqui. Autoritativo no servidor; o client recebe
 * uma copia via SyncPitouDataPacket para reagir (camera, auras, sons).
 *
 * IMPORTANTE: ao adicionar um campo novo, lembre de tratar em copyFrom, saveNBT e loadNBT.
 */
public class PitouData {

    // --- Transformacao / progressao ---
    private boolean transformed = false;
    private PitouMode unlocked = PitouMode.IDENTITY; // tier mais alto desbloqueado

    // --- Identidade: garras retrateis ---
    private boolean clawsExposed = false;

    // --- Identidade: carga do pulo carregado (shift segurado) ---
    private int chargedJumpTicks = 0;

    // --- Instinto: modo "brincar com a presa" ---
    private boolean playWithPrey = false;

    // --- Ativacoes que ligam/desligam (logica vem nas proximas etapas) ---
    private boolean enActive = false;
    private boolean ryuActive = false;
    private boolean renActive = false;
    private boolean blytheActive = false;
    private boolean terpsichoraActive = false;

    // --- O Rei ---
    private UUID kingId = null;
    private int kingDeaths = 0;

    // --- Paranoia (0 a 4) ---
    private int paranoia = 0;
    private boolean paranoiaActive = false; // so flicka quando longe do Rei; guardada quando perto
    private boolean kingNear = false;       // Rei a <= kingCloseDistance (suprime rabo/sentar)

    // --- Progressao para o Nen ---
    private int nenStrongKills = 0;
    private int nenMediumKills = 0;
    private int nenBrains = 0;

    // --- Maestrias (quanto mais usa, mais forte). 0.0+ ---
    private double ryuMastery = 0.0;
    private double blytheMastery = 0.0;

    // ============================ getters/setters ============================

    public boolean isTransformed() { return transformed; }
    public void setTransformed(boolean v) { this.transformed = v; }

    public PitouMode getUnlocked() { return unlocked; }
    public void setUnlocked(PitouMode m) { this.unlocked = m == null ? PitouMode.IDENTITY : m; }
    public boolean hasUnlocked(PitouMode required) { return unlocked.hasReached(required); }

    public boolean isClawsExposed() { return clawsExposed; }
    public void setClawsExposed(boolean v) { this.clawsExposed = v; }

    public int getChargedJumpTicks() { return chargedJumpTicks; }
    public void setChargedJumpTicks(int v) { this.chargedJumpTicks = Math.max(0, v); }

    public boolean isPlayWithPrey() { return playWithPrey; }
    public void setPlayWithPrey(boolean v) { this.playWithPrey = v; }

    public boolean isEnActive() { return enActive; }
    public void setEnActive(boolean v) { this.enActive = v; }

    public boolean isRyuActive() { return ryuActive; }
    public void setRyuActive(boolean v) { this.ryuActive = v; }

    public boolean isRenActive() { return renActive; }
    public void setRenActive(boolean v) { this.renActive = v; }

    public boolean isBlytheActive() { return blytheActive; }
    public void setBlytheActive(boolean v) { this.blytheActive = v; }

    public boolean isTerpsichoraActive() { return terpsichoraActive; }
    public void setTerpsichoraActive(boolean v) { this.terpsichoraActive = v; }

    public UUID getKingId() { return kingId; }
    public void setKingId(UUID id) { this.kingId = id; }
    public boolean hasKing() { return kingId != null; }

    public int getKingDeaths() { return kingDeaths; }
    public void setKingDeaths(int v) { this.kingDeaths = Math.max(0, v); }
    public void addKingDeath() { this.kingDeaths++; }

    public int getParanoia() { return paranoia; }
    public void setParanoia(int v) { this.paranoia = Math.max(0, Math.min(4, v)); }

    public boolean isParanoiaActive() { return paranoiaActive; }
    public void setParanoiaActive(boolean v) { this.paranoiaActive = v; }

    public boolean isKingNear() { return kingNear; }
    public void setKingNear(boolean v) { this.kingNear = v; }

    public int getNenStrongKills() { return nenStrongKills; }
    public void addNenStrongKill() { this.nenStrongKills++; }

    public int getNenMediumKills() { return nenMediumKills; }
    public void addNenMediumKill() { this.nenMediumKills++; }

    public int getNenBrains() { return nenBrains; }
    public void addNenBrain() { this.nenBrains++; }

    public double getRyuMastery() { return ryuMastery; }
    public void setRyuMastery(double v) { this.ryuMastery = Math.max(0.0, v); }

    public double getBlytheMastery() { return blytheMastery; }
    public void setBlytheMastery(double v) { this.blytheMastery = Math.max(0.0, v); }

    // ============================ ciclo de vida ============================

    public void clearAll() {
        transformed = false;
        unlocked = PitouMode.IDENTITY;
        clawsExposed = false;
        chargedJumpTicks = 0;
        playWithPrey = false;
        enActive = ryuActive = renActive = blytheActive = terpsichoraActive = false;
        kingId = null;
        kingDeaths = 0;
        paranoia = 0;
        paranoiaActive = false;
        kingNear = false;
        nenStrongKills = 0;
        nenMediumKills = 0;
        nenBrains = 0;
        ryuMastery = 0.0;
        blytheMastery = 0.0;
    }

    public void copyFrom(PitouData s) {
        this.transformed = s.transformed;
        this.unlocked = s.unlocked;
        this.clawsExposed = s.clawsExposed;
        this.playWithPrey = s.playWithPrey;
        this.enActive = s.enActive;
        this.ryuActive = s.ryuActive;
        this.renActive = s.renActive;
        this.blytheActive = s.blytheActive;
        this.terpsichoraActive = s.terpsichoraActive;
        this.kingId = s.kingId;
        this.kingDeaths = s.kingDeaths;
        this.paranoia = s.paranoia;
        this.paranoiaActive = s.paranoiaActive;
        this.kingNear = s.kingNear;
        this.nenStrongKills = s.nenStrongKills;
        this.nenMediumKills = s.nenMediumKills;
        this.nenBrains = s.nenBrains;
        this.ryuMastery = s.ryuMastery;
        this.blytheMastery = s.blytheMastery;
    }

    public void saveNBT(CompoundTag nbt) {
        nbt.putBoolean("transformed", transformed);
        nbt.putInt("unlocked", unlocked.ordinal());
        nbt.putBoolean("clawsExposed", clawsExposed);
        nbt.putBoolean("playWithPrey", playWithPrey);
        nbt.putBoolean("enActive", enActive);
        nbt.putBoolean("ryuActive", ryuActive);
        nbt.putBoolean("renActive", renActive);
        nbt.putBoolean("blytheActive", blytheActive);
        nbt.putBoolean("terpsichoraActive", terpsichoraActive);
        if (kingId != null) nbt.putUUID("kingId", kingId);
        nbt.putInt("kingDeaths", kingDeaths);
        nbt.putInt("paranoia", paranoia);
        nbt.putBoolean("paranoiaActive", paranoiaActive);
        nbt.putBoolean("kingNear", kingNear);
        nbt.putInt("nenStrongKills", nenStrongKills);
        nbt.putInt("nenMediumKills", nenMediumKills);
        nbt.putInt("nenBrains", nenBrains);
        nbt.putDouble("ryuMastery", ryuMastery);
        nbt.putDouble("blytheMastery", blytheMastery);
    }

    public void loadNBT(CompoundTag nbt) {
        transformed = nbt.getBoolean("transformed");
        unlocked = PitouMode.byOrdinalSafe(nbt.getInt("unlocked"));
        clawsExposed = nbt.getBoolean("clawsExposed");
        playWithPrey = nbt.getBoolean("playWithPrey");
        enActive = nbt.getBoolean("enActive");
        ryuActive = nbt.getBoolean("ryuActive");
        renActive = nbt.getBoolean("renActive");
        blytheActive = nbt.getBoolean("blytheActive");
        terpsichoraActive = nbt.getBoolean("terpsichoraActive");
        kingId = nbt.hasUUID("kingId") ? nbt.getUUID("kingId") : null;
        kingDeaths = nbt.getInt("kingDeaths");
        paranoia = nbt.getInt("paranoia");
        paranoiaActive = nbt.getBoolean("paranoiaActive");
        kingNear = nbt.getBoolean("kingNear");
        nenStrongKills = nbt.getInt("nenStrongKills");
        nenMediumKills = nbt.getInt("nenMediumKills");
        nenBrains = nbt.getInt("nenBrains");
        ryuMastery = nbt.getDouble("ryuMastery");
        blytheMastery = nbt.getDouble("blytheMastery");
    }
}
