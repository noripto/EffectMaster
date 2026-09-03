package effect_master.server

import effect_master.EffectMaster
import net.minecraft.server.level.ServerPlayer

/** 設定を書き換えてよいのは誰か、を一箇所で決める。 */
object EffectMasterPermissions {
    /**
     * シングルプレイのホストは、チートを許可していなくても（権限レベルが 0 でも）
     * ワールドの持ち主なので常に許す。
     *
     * LAN に公開した場合、ホスト以外はここで false になるので op が要る。
     * 専用サーバーでは [EffectMaster.OWNER_PERMISSION_LEVEL] だけが判定材料になる。
     */
    fun isOwner(player: ServerPlayer): Boolean =
        player.server.isSingleplayerOwner(player.gameProfile) ||
            player.hasPermissions(EffectMaster.OWNER_PERMISSION_LEVEL)
}
