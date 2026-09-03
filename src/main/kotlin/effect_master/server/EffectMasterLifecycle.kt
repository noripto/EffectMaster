package effect_master.server

import effect_master.EffectMaster
import effect_master.config.EffectSettings
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.server.ServerAboutToStartEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

/**
 * サーバーの起動に合わせた初期化。
 *
 * 設定の書き出しは変更のたびに行うので（[EffectSettingsService.apply]）、
 * ここで受け持つのは読み込みとコマンド登録だけ。
 */
@Mod.EventBusSubscriber(modid = EffectMaster.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object EffectMasterLifecycle {
    @SubscribeEvent
    fun onServerAboutToStart(event: ServerAboutToStartEvent) {
        EffectSettings.load()
    }

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        EffectMasterCommand.register(event.dispatcher)
    }
}
