package effect_master.network

import effect_master.EffectMaster
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor
import net.minecraftforge.network.simple.SimpleChannel

/**
 * パケットのチャンネル定義。
 *
 * 設定を持っているのはサーバーだけで、クライアントは画面を開くときにスナップショットを
 * 受け取り、閉じるときに差分を送り返す。
 */
object EffectMasterNetwork {
    private const val PROTOCOL_VERSION = "2"

    private val channel: SimpleChannel = NetworkRegistry.newSimpleChannel(
        ResourceLocation(EffectMaster.ID, "main"),
        { PROTOCOL_VERSION },
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals,
    )

    fun register() {
        var id = 0

        channel.messageBuilder(OpenConfigScreenPacket::class.java, id++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder { packet, buf -> packet.encode(buf) }
            .decoder { buf -> OpenConfigScreenPacket.decode(buf) }
            .consumerMainThread { packet, context -> packet.handle(context.get()) }
            .add()

        channel.messageBuilder(UpdateEffectSettingsPacket::class.java, id++, NetworkDirection.PLAY_TO_SERVER)
            .encoder { packet, buf -> packet.encode(buf) }
            .decoder { buf -> UpdateEffectSettingsPacket.decode(buf) }
            .consumerMainThread { packet, context -> packet.handle(context.get()) }
            .add()
    }

    fun sendToPlayer(packet: OpenConfigScreenPacket, player: ServerPlayer) {
        channel.send(PacketDistributor.PLAYER.with { player }, packet)
    }

    fun sendToServer(packet: UpdateEffectSettingsPacket) {
        channel.sendToServer(packet)
    }
}
