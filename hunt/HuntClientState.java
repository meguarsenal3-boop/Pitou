package com.megu.neferpitou.hunt;

/**
 * Estado client da caca, preenchido pelo HuntClientHandler e lido por outros
 * sistemas (indicador no HUD, e futuramente a aura vermelha e o boost de pulo).
 */
public final class HuntClientState {

    private HuntClientState() {}

    /** Id da entidade-alvo (ser forte) atual, ou -1 se nenhum. */
    public static int targetId = -1;

    /** True quando ha alvo E o player esta alto o suficiente para ver a indicacao. */
    public static boolean seeingIndicator = false;
}
