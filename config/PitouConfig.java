package com.megu.neferpitou.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Config COMMON do mod. Vira o arquivo: config/neferpitou-common.toml
 *
 * Para mudar qualquer numero: abra esse .toml no bloco de notas, edite, salve e
 * reabra o mundo. Nada de recompilar. Cada valor tem comentario explicando.
 *
 * Os limiares de THREAT (forca de mob) sao o que voce mais vai calibrar testando
 * in-game contra os mobs do seu modpack.
 */
public class PitouConfig {

    public static final ForgeConfigSpec SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        SPEC = pair.getRight();
    }

    public static class Common {

        // ---------------- IDENTIDADE ----------------
        public final ForgeConfigSpec.IntValue creeperDetectRadius;
        public final ForgeConfigSpec.IntValue creeperFlickIntervalTicks;
        public final ForgeConfigSpec.IntValue kingHighlightRadius;
        public final ForgeConfigSpec.DoubleValue kingBuffMaxMultiplier;
        public final ForgeConfigSpec.DoubleValue kingBuffFloorHealthRatio;
        public final ForgeConfigSpec.IntValue kingFarDistance;
        public final ForgeConfigSpec.IntValue kingFarTimeTicks;
        public final ForgeConfigSpec.IntValue kingCalmTicks;
        public final ForgeConfigSpec.IntValue kingCloseDistance;
        public final ForgeConfigSpec.IntValue kingAggroRange;
        public final ForgeConfigSpec.IntValue kingDeathsForParanoia;
        public final ForgeConfigSpec.IntValue kingSmellRadius;
        public final ForgeConfigSpec.DoubleValue kingBuffMinFraction;
        public final ForgeConfigSpec.DoubleValue paranoiaFlickChancePerLevel;
        public final ForgeConfigSpec.DoubleValue paranoiaFlickMaxDeg;
        public final ForgeConfigSpec.DoubleValue fallDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue clawDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue chargedJumpMaxMultiplier;
        public final ForgeConfigSpec.IntValue chargedJumpMaxHoldTicks;
        public final ForgeConfigSpec.IntValue chargedJumpCooldownTicks;
        public final ForgeConfigSpec.DoubleValue chargedDashMinChargeFraction;
        public final ForgeConfigSpec.IntValue chargedDashDurationTicks;
        public final ForgeConfigSpec.DoubleValue chargedDashForwardBlocks;
        public final ForgeConfigSpec.DoubleValue chargedDashUpwardBlocks;
        public final ForgeConfigSpec.DoubleValue jumpHitMultiplier;
        public final ForgeConfigSpec.DoubleValue chargedJumpBaseSpeed;
        public final ForgeConfigSpec.DoubleValue baseJumpBonus;
        public final ForgeConfigSpec.DoubleValue speedBonus;
        public final ForgeConfigSpec.DoubleValue attackBonus;
        public final ForgeConfigSpec.DoubleValue stepHeightBonus;
        public final ForgeConfigSpec.DoubleValue knockbackResistance;
        public final ForgeConfigSpec.DoubleValue armorBonus;
        public final ForgeConfigSpec.DoubleValue armorToughnessBonus;
        public final ForgeConfigSpec.DoubleValue breakSpeedMultiplier;
        public final ForgeConfigSpec.BooleanValue nightVision;
        public final ForgeConfigSpec.BooleanValue waterSlowness;
        public final ForgeConfigSpec.IntValue fishFoodBonusRaw;
        public final ForgeConfigSpec.IntValue fishFoodBonusCooked;
        public final ForgeConfigSpec.DoubleValue fishSaturationModifier;
        public final ForgeConfigSpec.IntValue villagerEatFood;
        public final ForgeConfigSpec.DoubleValue villagerSaturationModifier;
        public final ForgeConfigSpec.DoubleValue damageResistance;

        // ---------------- INSTINTO ----------------
        public final ForgeConfigSpec.IntValue smellRadius;
        public final ForgeConfigSpec.DoubleValue smellCaveReduction;
        public final ForgeConfigSpec.DoubleValue smellNauseaPerTick;
        public final ForgeConfigSpec.DoubleValue smellNauseaTrigger;
        public final ForgeConfigSpec.DoubleValue smellNauseaMax;
        public final ForgeConfigSpec.DoubleValue smellNauseaDecay;
        public final ForgeConfigSpec.DoubleValue smellNauseaIntensity;
        public final ForgeConfigSpec.DoubleValue catScareBaseChance;
        public final ForgeConfigSpec.IntValue strongPreyDetectRadius;
        public final ForgeConfigSpec.IntValue strongPreyVisionMinY;
        public final ForgeConfigSpec.DoubleValue playDamagePctNoClaw;
        public final ForgeConfigSpec.DoubleValue playDamagePctClaw;
        public final ForgeConfigSpec.DoubleValue playDamagePctNen;
        public final ForgeConfigSpec.DoubleValue huntKillBuffPercent;
        public final ForgeConfigSpec.IntValue huntKillBuffDurationTicks;
        public final ForgeConfigSpec.IntValue huntFlickIntervalTicks;

        // ---------------- NEN ----------------
        public final ForgeConfigSpec.IntValue enRadius;
        public final ForgeConfigSpec.IntValue threatTickInterval;
        public final ForgeConfigSpec.BooleanValue nenEnabled;
        public final ForgeConfigSpec.DoubleValue nenMaxMultiplier;
        public final ForgeConfigSpec.DoubleValue nenSpeedDivisor;
        public final ForgeConfigSpec.DoubleValue nenBrainDropChance;
        public final ForgeConfigSpec.IntValue nenStrongKillsNeeded;
        public final ForgeConfigSpec.IntValue nenMediumKillsNeeded;
        public final ForgeConfigSpec.IntValue nenBrainsNeeded;
        // ---- Habilidades de Nen (Ryu/En/Ren) ----
        public final ForgeConfigSpec.DoubleValue ryuDamageResistStart;
        public final ForgeConfigSpec.DoubleValue ryuDamageResistMax;
        public final ForgeConfigSpec.DoubleValue ryuSpeedStart;
        public final ForgeConfigSpec.DoubleValue ryuSpeedMax;
        public final ForgeConfigSpec.DoubleValue ryuMasteryMaxTicks;
        public final ForgeConfigSpec.BooleanValue enHighlightItems;
        public final ForgeConfigSpec.IntValue renRadius;
        public final ForgeConfigSpec.DoubleValue renDamageBonus;
        public final ForgeConfigSpec.DoubleValue renSpeedBonus;

        // ---------------- THREAT (limiares de forca) ----------------
        public final ForgeConfigSpec.DoubleValue threatArmorWeight;
        public final ForgeConfigSpec.DoubleValue threatToughnessWeight;
        public final ForgeConfigSpec.DoubleValue threatAttackWeight;
        public final ForgeConfigSpec.DoubleValue threatFallbackAttack;
        public final ForgeConfigSpec.DoubleValue tierTrivialMax;
        public final ForgeConfigSpec.DoubleValue tierWeakMax;
        public final ForgeConfigSpec.DoubleValue tierMediumMax;
        public final ForgeConfigSpec.DoubleValue tierStrongMax;
        public final ForgeConfigSpec.DoubleValue tierReallyStrongMax;
        public final ForgeConfigSpec.DoubleValue tierApexMax;
        public final ForgeConfigSpec.IntValue threatCacheTtlTicks;

        // ---------------- HATSU (Doctor Blythe) ----------------
        public final ForgeConfigSpec.IntValue blytheAnchorRange;
        public final ForgeConfigSpec.IntValue blythePlayerLeash;
        public final ForgeConfigSpec.DoubleValue blytheBaseHealPerSecond;

        // ---------------- TERPSICHORA ----------------
        public final ForgeConfigSpec.IntValue terpsichoraDashRange;
        public final ForgeConfigSpec.IntValue reviveDurationTicks;
        public final ForgeConfigSpec.DoubleValue reviveStrengthMultiplier;

        Common(ForgeConfigSpec.Builder b) {

            b.comment("Identidade base da Pitou (sempre ativa quando transformada).").push("identidade");
            creeperDetectRadius = b.comment("Raio (blocos) em que creepers fazem a camera 'flickar' na direcao deles.")
                    .defineInRange("creeperDetectRadius", 24, 1, 128);
            creeperFlickIntervalTicks = b.comment("Intervalo (ticks) entre flicks de camera ao ver creeper. 20 ticks = 1s.")
                    .defineInRange("creeperFlickIntervalTicks", 40, 1, 1200);
            kingHighlightRadius = b.comment("Raio (blocos) para destacar mobs perto do Rei.")
                    .defineInRange("kingHighlightRadius", 25, 1, 128);
            kingBuffMaxMultiplier = b.comment("Forca total do buff do Rei quando ele esta no piso de vida (melhor caso).")
                    .defineInRange("kingBuffMaxMultiplier", 1.0, 0.0, 10.0);
            kingBuffFloorHealthRatio = b.comment("Ratio de vida do Rei (atual/max) em que o buff chega ao maximo. 0.1 = 10% de vida.")
                    .defineInRange("kingBuffFloorHealthRatio", 0.1, 0.0, 1.0);
            kingFarDistance = b.comment("Distancia (blocos) do Rei a partir da qual conta como 'longe' (a paranoia so flicka quando longe).")
                    .defineInRange("kingFarDistance", 200, 1, 4096);
            kingFarTimeTicks = b.comment("Tempo (ticks) longe do Rei para SUBIR 1 nivel de paranoia. 1200 = 60s.")
                    .defineInRange("kingFarTimeTicks", 1200, 1, 72000);
            kingCalmTicks = b.comment("Tempo (ticks) perto do Rei (dentro de kingFarDistance) para DESCER 1 nivel de paranoia. 3600 = 3min.")
                    .defineInRange("kingCalmTicks", 3600, 1, 72000);
            kingCloseDistance = b.comment("Distancia (blocos) para contar como 'Rei perto' (suprime rabo/orelhas/sentar).")
                    .defineInRange("kingCloseDistance", 5, 1, 64);
            kingAggroRange = b.comment("Raio (blocos) ao redor do Rei para detectar mobs agredindo ele (ativa o buff).")
                    .defineInRange("kingAggroRange", 30, 1, 128);
            kingDeathsForParanoia = b.comment("Quantas mortes do Rei contam como 'varias vezes' para subir a paranoia.")
                    .defineInRange("kingDeathsForParanoia", 3, 1, 100000);
            kingSmellRadius = b.comment("Raio (blocos) em que o cheiro detecta o Rei (bem maior que mobs normais).")
                    .defineInRange("kingSmellRadius", 200, 1, 2048);
            kingBuffMinFraction = b.comment("Fracao MINIMA do buff do Rei, com ele na vida cheia. 0.01 = 1%.")
                    .defineInRange("kingBuffMinFraction", 0.01, 0.0, 1.0);
            paranoiaFlickChancePerLevel = b.comment("Chance por tick de uma flickada de paranoia, por nivel. 0.01 = 1%/tick por nivel.")
                    .defineInRange("paranoiaFlickChancePerLevel", 0.01, 0.0, 1.0);
            paranoiaFlickMaxDeg = b.comment("Tamanho maximo (graus) da flickada de paranoia no nivel 4.")
                    .defineInRange("paranoiaFlickMaxDeg", 6.0, 0.0, 90.0);
            fallDamageMultiplier = b.comment("Multiplicador do dano de queda. 0.0 = imune, 1.0 = normal.")
                    .defineInRange("fallDamageMultiplier", 0.1, 0.0, 1.0);
            clawDamageMultiplier = b.comment("Multiplicador do dano melee com garras expostas.")
                    .defineInRange("clawDamageMultiplier", 2.0, 1.0, 10.0);
            chargedJumpMaxMultiplier = b.comment("Forca maxima do pulo carregado (shift segurado).")
                    .defineInRange("chargedJumpMaxMultiplier", 8.0, 1.0, 20.0);
            chargedJumpMaxHoldTicks = b.comment("Tempo (ticks) de carga ate o pulo chegar no maximo.")
                    .defineInRange("chargedJumpMaxHoldTicks", 200, 1, 200);
            chargedJumpCooldownTicks = b.comment("Cooldown (ticks) apos um pulo carregado. 60 = 3s.")
                    .defineInRange("chargedJumpCooldownTicks", 60, 0, 1200);
            chargedDashMinChargeFraction = b.comment("Fracao MINIMA do dash com carga zero (0.3 = 30% do alcance).")
                    .defineInRange("chargedDashMinChargeFraction", 0.3, 0.0, 1.0);
            chargedDashDurationTicks = b.comment("Duracao (ticks) do dash sustentado do pulo carregado.")
                    .defineInRange("chargedDashDurationTicks", 8, 1, 100);
            chargedDashForwardBlocks = b.comment("Alcance horizontal (blocos) do dash na carga maxima, olhando reto pra frente.")
                    .defineInRange("chargedDashForwardBlocks", 30.0, 1.0, 200.0);
            chargedDashUpwardBlocks = b.comment("Alcance vertical (blocos) do dash na carga maxima, olhando reto pra cima.")
                    .defineInRange("chargedDashUpwardBlocks", 15.0, 1.0, 200.0);
            jumpHitMultiplier = b.comment("Multiplicador de dano quando o dash do pulo acerta um mob na trajetoria. 2.0 = 2x.")
                    .defineInRange("jumpHitMultiplier", 2.0, 0.0, 100.0);
            chargedJumpBaseSpeed = b.comment("Velocidade base do salto carregado (na carga minima). Escala ate o max multiplier.")
                    .defineInRange("chargedJumpBaseSpeed", 0.9, 0.1, 10.0);
            baseJumpBonus = b.comment("Bonus de altura no pulo NORMAL (sem shift). Velocidade Y extra adicionada.")
                    .defineInRange("baseJumpBonus", 0.18, 0.0, 5.0);
            speedBonus = b.comment("Bonus de velocidade de movimento (multiplicador). 0.4 = +40%. Some na agua.")
                    .defineInRange("speedBonus", 0.4, 0.0, 10.0);
            attackBonus = b.comment("Bonus de dano de ataque (forca). Somado ao atributo base.")
                    .defineInRange("attackBonus", 40.0, 0.0, 100.0);
            stepHeightBonus = b.comment("Bonus de step height (sobe degraus mais altos). 0.5 deixa subir ~1 bloco.")
                    .defineInRange("stepHeightBonus", 0.5, 0.0, 5.0);
            knockbackResistance = b.comment("Resistencia a knockback (0 a 1).")
                    .defineInRange("knockbackResistance", 0.5, 0.0, 1.0);
            armorBonus = b.comment("Bonus de armadura (resistencia a dano).")
                    .defineInRange("armorBonus", 6.0, 0.0, 100.0);
            armorToughnessBonus = b.comment("Bonus de tenacidade de armadura.")
                    .defineInRange("armorToughnessBonus", 4.0, 0.0, 100.0);
            breakSpeedMultiplier = b.comment("Multiplicador de velocidade pra quebrar blocos.")
                    .defineInRange("breakSpeedMultiplier", 1.5, 0.1, 20.0);
            nightVision = b.comment("Visao noturna constante enquanto transformada.")
                    .define("nightVision", true);
            waterSlowness = b.comment("Aplica Slowness I na agua (gatos odeiam agua).")
                    .define("waterSlowness", true);
            fishFoodBonusRaw = b.comment("Comida EXTRA ao comer peixe CRU (cod, salmon, etc), alem da comida normal do item.")
                    .defineInRange("fishFoodBonusRaw", 4, 0, 20);
            fishFoodBonusCooked = b.comment("Comida EXTRA ao comer peixe COZIDO.")
                    .defineInRange("fishFoodBonusCooked", 2, 0, 20);
            fishSaturationModifier = b.comment("Modificador de saturacao do bonus de peixe (0..1). Maior = mais saturacao.")
                    .defineInRange("fishSaturationModifier", 0.4, 0.0, 1.0);
            villagerEatFood = b.comment("Comida ganha ao COMER um villager (clique direito enquanto transformada).")
                    .defineInRange("villagerEatFood", 8, 0, 20);
            villagerSaturationModifier = b.comment("Modificador de saturacao ao comer villager (0..1).")
                    .defineInRange("villagerSaturationModifier", 0.8, 0.0, 1.0);
            damageResistance = b.comment("Reducao de TODO dano recebido enquanto transformada (0..0.95). 0.6 = recebe so 40% do dano. Empilha com a armadura.")
                    .defineInRange("damageResistance", 0.6, 0.0, 0.95);
            b.pop();

            b.comment("Instinto: cheirar, susto do gato, caca de seres fortes, brincar com presa.").push("instinto");
            smellRadius = b.comment("Raio (blocos) do 'cheirar' (glowing nos mobs proximos, atravessa paredes).")
                    .defineInRange("smellRadius", 48, 1, 256);
            smellCaveReduction = b.comment("Mobs em caverna (sem ceu) ou na agua: raio multiplicado por isto. 1.0 = sem reducao, 0.5 = metade do alcance.")
                    .defineInRange("smellCaveReduction", 0.5, 0.05, 1.0);
            smellNauseaPerTick = b.comment("Nausea ganha por tick, por mob de cheiro ruim proximo (escala com forca do cheiro e proximidade).")
                    .defineInRange("smellNauseaPerTick", 0.05, 0.0, 10.0);
            smellNauseaTrigger = b.comment("Acima deste valor de nausea acumulada, a tela embola (efeito Nausea).")
                    .defineInRange("smellNauseaTrigger", 1.0, 0.0, 100.0);
            smellNauseaMax = b.comment("Teto do acumulador de nausea (quanto mais alto, mais a nausea demora a passar depois).")
                    .defineInRange("smellNauseaMax", 3.0, 0.1, 100.0);
            smellNauseaDecay = b.comment("Quanto a nausea cai por tick (sempre que voce esta cheirando, e na hora que desliga).")
                    .defineInRange("smellNauseaDecay", 0.02, 0.0, 10.0);
            smellNauseaIntensity = b.comment("Intensidade MAXIMA da distorcao de tela (0..1). 1.0 = nausea cheia do vanilla (muito forte, tipo portal). 0.2 = leve.")
                    .defineInRange("smellNauseaIntensity", 0.2, 0.0, 1.0);
            catScareBaseChance = b.comment("Chance base (0-1) do susto do gato quando um mob chega por tras.")
                    .defineInRange("catScareBaseChance", 0.15, 0.0, 1.0);
            strongPreyDetectRadius = b.comment("Raio (blocos) para detectar seres fortes para cacar.")
                    .defineInRange("strongPreyDetectRadius", 128, 1, 1024);
            strongPreyVisionMinY = b.comment("Altura Y minima para ver a indicacao do alvo forte.")
                    .defineInRange("strongPreyVisionMinY", 200, 0, 320);
            playDamagePctNoClaw = b.comment("'Brincar com a presa': dano = % da vida MAXIMA do alvo, sem garras.")
                    .defineInRange("playDamagePctNoClaw", 0.05, 0.0, 1.0);
            playDamagePctClaw = b.comment("'Brincar com a presa': % da vida maxima do alvo, com garras.")
                    .defineInRange("playDamagePctClaw", 0.10, 0.0, 1.0);
            playDamagePctNen = b.comment("'Brincar com a presa': % da vida maxima do alvo, com Nen ativo.")
                    .defineInRange("playDamagePctNen", 0.20, 0.0, 1.0);
            huntKillBuffPercent = b.comment("Buff ao matar um ser forte: +X em velocidade e attack speed (0.15 = +15%).")
                    .defineInRange("huntKillBuffPercent", 0.15, 0.0, 2.0);
            huntKillBuffDurationTicks = b.comment("Duracao (ticks) do buff de caca. 200 = 10s.")
                    .defineInRange("huntKillBuffDurationTicks", 200, 1, 24000);
            huntFlickIntervalTicks = b.comment("Intervalo (ticks) entre flicks de camera na direcao do ser forte (distracao).")
                    .defineInRange("huntFlickIntervalTicks", 60, 1, 1200);
            b.pop();

            b.comment("Nen: Ten/Ren/Ryu/En/Ko. Por enquanto so En e parametros gerais.").push("nen");
            enRadius = b.comment("Raio (blocos) do En. O En e um quadrado/dome ao redor da Pitou.")
                    .defineInRange("enRadius", 100, 1, 256);
            threatTickInterval = b.comment("De quantos em quantos ticks o En reavalia os mobs dentro dele (performance).")
                    .defineInRange("threatTickInterval", 10, 1, 200);
            nenEnabled = b.comment("Liga/desliga o sistema de mobs com Nen (mobs hostis ganham forca ao spawnar).")
                    .define("nenEnabled", true);
            nenMaxMultiplier = b.comment("Maior multiplicador de Nen permitido. Categorias acima disso nao spawnam.",
                            "Coloque 25 para tirar os 3 mais fortes (50x, 500x, 1000x).")
                    .defineInRange("nenMaxMultiplier", 1000.0, 1.0, 1000.0);
            nenSpeedDivisor = b.comment("A velocidade ganha bonus = multiplier / divisor. 100 => 2x = +2%, 1000x = +1000%.")
                    .defineInRange("nenSpeedDivisor", 100.0, 1.0, 10000.0);
            nenBrainDropChance = b.comment("Chance de um mob com Nen dropar o cerebro ao morrer. 0.5 = 50%.")
                    .defineInRange("nenBrainDropChance", 0.5, 0.0, 1.0);
            nenStrongKillsNeeded = b.comment("Quantos seres STRONG (ou acima) matar para desbloquear o Nen.")
                    .defineInRange("nenStrongKillsNeeded", 2, 0, 100000);
            nenMediumKillsNeeded = b.comment("Quantos seres MEDIUM matar para desbloquear o Nen.")
                    .defineInRange("nenMediumKillsNeeded", 10, 0, 100000);
            nenBrainsNeeded = b.comment("Quantos cerebros usar (graveto + botao direito) para desbloquear o Nen.")
                    .defineInRange("nenBrainsNeeded", 10, 0, 100000);
            b.pop();

            b.comment("Habilidades de Nen: Ryu (evolui), En (highlight), Ren (aura).").push("nen_abilities");
            ryuDamageResistStart = b.comment("Bonus inicial de dano E resistencia do Ryu. 0.5 = +50%.")
                    .defineInRange("ryuDamageResistStart", 0.5, 0.0, 100.0);
            ryuDamageResistMax = b.comment("Teto de dano E resistencia do Ryu. 3.0 = +300%.")
                    .defineInRange("ryuDamageResistMax", 3.0, 0.0, 100.0);
            ryuSpeedStart = b.comment("Bonus inicial de velocidade do Ryu. 0.25 = +25%.")
                    .defineInRange("ryuSpeedStart", 0.25, 0.0, 10.0);
            ryuSpeedMax = b.comment("Teto de velocidade do Ryu. 0.5 = +50%.")
                    .defineInRange("ryuSpeedMax", 0.5, 0.0, 10.0);
            ryuMasteryMaxTicks = b.comment("Ticks de Ryu ativo para chegar ao maximo. 24000 = 20min de uso ativo.")
                    .defineInRange("ryuMasteryMaxTicks", 24000.0, 1.0, 1.0E9);
            enHighlightItems = b.comment("Se o En tambem destaca itens dropados no chao.")
                    .define("enHighlightItems", true);
            renRadius = b.comment("Raio (blocos) da aura do Ren.")
                    .defineInRange("renRadius", 30, 1, 256);
            renDamageBonus = b.comment("Bonus de dano do Ren (fixo). 0.75 = +75%.")
                    .defineInRange("renDamageBonus", 0.75, 0.0, 100.0);
            renSpeedBonus = b.comment("Bonus de velocidade do Ren (fixo). 0.25 = +25%.")
                    .defineInRange("renSpeedBonus", 0.25, 0.0, 10.0);
            b.pop();

            b.comment("Classificacao de forca de mob (ThreatLevel). Calibre testando contra seu modpack.",
                            "score = vidaEfetiva + (threatAttackWeight * danoEfetivo)",
                            "vidaEfetiva = vidaMax * (1 + armorWeight*armadura + toughnessWeight*tenacidade)")
                    .push("threat");
            threatArmorWeight = b.comment("Peso de cada ponto de armadura na vida efetiva.")
                    .defineInRange("threatArmorWeight", 0.04, 0.0, 1.0);
            threatToughnessWeight = b.comment("Peso de cada ponto de armor toughness na vida efetiva.")
                    .defineInRange("threatToughnessWeight", 0.08, 0.0, 1.0);
            threatAttackWeight = b.comment("Quanto o dano de ataque pesa no score (multiplicador). Baixo = HP domina (mais intuitivo).")
                    .defineInRange("threatAttackWeight", 3.0, 0.0, 100.0);
            threatFallbackAttack = b.comment("Dano assumido para mobs sem atributo de ataque (creeper, ranged, etc).")
                    .defineInRange("threatFallbackAttack", 3.0, 0.0, 100.0);
            tierTrivialMax = b.comment("Score maximo para TRIVIAL (pacificos, zumbis). Ex: zumbi ~30.")
                    .defineInRange("tierTrivialMax", 50.0, 0.0, 1.0E9);
            tierWeakMax = b.comment("Score maximo para WEAK (brutes, endermans). Ex: enderman ~60, brute ~90.")
                    .defineInRange("tierWeakMax", 130.0, 0.0, 1.0E9);
            tierMediumMax = b.comment("Score maximo para MEDIUM (iron golem ~145).")
                    .defineInRange("tierMediumMax", 180.0, 0.0, 1.0E9);
            tierStrongMax = b.comment("Score maximo para STRONG (wither, ender dragon, warden ~590).")
                    .defineInRange("tierStrongMax", 1000.0, 0.0, 1.0E9);
            tierReallyStrongMax = b.comment("Score maximo para REALLY_STRONG (comparavel a Pitou; no vanilla ninguem chega).")
                    .defineInRange("tierReallyStrongMax", 3000.0, 0.0, 1.0E9);
            tierApexMax = b.comment("Score maximo para APEX (mais forte que ela, mas vencivel). Acima disso = PREY (quase impossivel).")
                    .defineInRange("tierApexMax", 8000.0, 0.0, 1.0E12);
            threatCacheTtlTicks = b.comment("Por quantos ticks o tier de um mob fica em cache antes de recalcular.")
                    .defineInRange("threatCacheTtlTicks", 100, 1, 12000);
            b.pop();

            b.comment("Hatsu: Doctor Blythe (cura presa pela boneca).").push("hatsu");
            blytheAnchorRange = b.comment("Distancia maxima (blocos) que o alvo curado pode se afastar do spawn da boneca.")
                    .defineInRange("blytheAnchorRange", 20, 1, 128);
            blythePlayerLeash = b.comment("Distancia maxima (blocos) que a Pitou pode ficar da boneca.")
                    .defineInRange("blythePlayerLeash", 10, 1, 128);
            blytheBaseHealPerSecond = b.comment("Cura base por segundo (antes do bonus de maestria).")
                    .defineInRange("blytheBaseHealPerSecond", 2.0, 0.0, 1000.0);
            b.pop();

            b.comment("Terpsichora: forma final + Volta da Morte.").push("terpsichora");
            terpsichoraDashRange = b.comment("Alcance (blocos) do dash/tp em direcao a um alvo.")
                    .defineInRange("terpsichoraDashRange", 20, 1, 128);
            reviveDurationTicks = b.comment("Duracao (ticks) da Volta da Morte. 20 ticks = 1s.")
                    .defineInRange("reviveDurationTicks", 200, 1, 12000);
            reviveStrengthMultiplier = b.comment("Multiplicador de forca durante a Volta da Morte.")
                    .defineInRange("reviveStrengthMultiplier", 2.0, 1.0, 10.0);
            b.pop();
        }
    }
}
