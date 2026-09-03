package effect_master.server

import effect_master.EffectMaster
import effect_master.config.EffectSettings
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.living.MobEffectEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

/**
 * 設定どおりの状態をプレイヤーに保たせる。
 *
 * 無効化した効果は付与の時点で弾くのが基本で、tick での掃除は取りこぼしの保険。
 * 他 MOD が [MobEffectEvent.Applicable] を通さずに効果を付けた場合や、設定を変えた
 * 時点でオフラインだったプレイヤーが該当する。
 */
@Mod.EventBusSubscriber(modid = EffectMaster.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object EffectEnforcer {
    /** 掃除の間隔。毎 tick 全プレイヤーの効果を舐める必要はない。 */
    private const val RECONCILE_INTERVAL_TICKS = 20

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onEffectApplicable(event: MobEffectEvent.Applicable) {
        val player = event.entity as? ServerPlayer ?: return
        if (player.level().isClientSide) return

        if (EffectSettings.isDisabled(event.effectInstance.effect)) {
            event.result = Event.Result.DENY
        }
    }

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        if (EffectSettings.isEmpty()) return

        val server = event.server
        if (server.tickCount % RECONCILE_INTERVAL_TICKS != 0) return

        server.playerList.players.forEach(::reconcile)
    }

    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        reconcile(player)
    }

    /** 1 人分の効果を設定に合わせる。 */
    private fun reconcile(player: ServerPlayer) {
        // 走査中に removeEffect すると activeEffects を壊すので、先に対象を確定させる。
        player.activeEffects
            .filter { EffectSettings.isDisabled(it.effect) }
            .map { it.effect }
            .forEach { player.removeEffect(it) }

        EffectSettings.persistentEffects().forEach { effect ->
            if (player.getEffect(effect)?.isInfiniteDuration != true) {
                player.addEffect(EffectSettingsService.persistentInstance(effect))
            }
        }
    }
}
