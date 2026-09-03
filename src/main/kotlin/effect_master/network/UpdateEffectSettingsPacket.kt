package effect_master.network

import effect_master.EffectMaster
import effect_master.config.EffectOption
import effect_master.server.EffectMasterPermissions
import effect_master.server.EffectSettingsService
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.network.NetworkEvent

/**
 * クライアント → サーバー。設定画面で変えた分だけをまとめて送る。
 *
 * 画面を閲覧専用で開かせていても、細工したクライアントは中身に関係なくこれを送れる。
 * 権限はサーバー側でもう一度確かめる。
 */
class UpdateEffectSettingsPacket(private val changes: Map<ResourceLocation, EffectOption>) {
    fun encode(buf: FriendlyByteBuf) {
        EffectSettingsCodec.encode(buf, changes)
    }

    fun handle(context: NetworkEvent.Context) {
        val player = context.sender ?: return

        if (!EffectMasterPermissions.isOwner(player)) {
            EffectMaster.LOGGER.warn(
                "Rejected an effect settings update from {}: not the owner of this world",
                player.gameProfile.name,
            )
            player.sendSystemMessage(Component.translatable("effect_master.message.owner_only"))
            return
        }

        EffectSettingsService.apply(player.server, changes)
    }

    companion object {
        fun decode(buf: FriendlyByteBuf): UpdateEffectSettingsPacket =
            UpdateEffectSettingsPacket(EffectSettingsCodec.decode(buf))
    }
}
