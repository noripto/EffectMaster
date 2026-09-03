package effect_master.server

import effect_master.EffectMaster
import effect_master.config.EffectOption
import effect_master.config.EffectSettings
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraftforge.registries.ForgeRegistries

/**
 * 設定の書き換えと、その結果をログイン中のプレイヤーへ反映させる処理。
 *
 * 設定を変えてよいかの判断はここでは行わない。呼び出し元
 * （[effect_master.network.UpdateEffectSettingsPacket]）が権限を確かめてから呼ぶ。
 */
object EffectSettingsService {
    fun apply(server: MinecraftServer, changes: Map<ResourceLocation, EffectOption>) {
        var changed = false

        for ((id, option) in changes) {
            val effect = ForgeRegistries.MOB_EFFECTS.getValue(id)
            if (effect == null) {
                EffectMaster.LOGGER.warn("Ignoring setting for unknown effect {}", id)
                continue
            }

            val previous = EffectSettings[id]
            if (!EffectSettings.set(id, option)) continue

            changed = true
            EffectMaster.LOGGER.info("Effect {} changed from {} to {}", id, previous, option)
            server.playerList.players.forEach { applyTo(it, effect, previous, option) }
        }

        // 変更のたびに書き出す。専用サーバーが落ちても設定が消えないようにするため。
        if (changed) EffectSettings.save()
    }

    /** 常時付与用のインスタンス。パーティクルは出さず、HUD のアイコンだけ残す。 */
    fun persistentInstance(effect: MobEffect): MobEffectInstance =
        MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION, 0, true, false, true)

    private fun applyTo(player: ServerPlayer, effect: MobEffect, previous: EffectOption, option: EffectOption) {
        when (option) {
            EffectOption.DISABLED -> player.removeEffect(effect)
            EffectOption.PERSISTENT -> player.addEffect(persistentInstance(effect))
            EffectOption.DEFAULT -> if (previous == EffectOption.PERSISTENT) removePersistent(player, effect)
        }
    }

    /**
     * PERSISTENT をやめたときの後始末。
     *
     * MOD が付けた効果かどうかは記録していないので、無限時間かどうかで見分ける。
     * ビーコンや `/effect give ... infinite` で付いた同じ効果も巻き添えで消えるが、
     * 由来を追跡するコストに見合わないと判断してこの近似にしている。
     */
    private fun removePersistent(player: ServerPlayer, effect: MobEffect) {
        if (player.getEffect(effect)?.isInfiniteDuration == true) player.removeEffect(effect)
    }
}
