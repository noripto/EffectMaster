package effect_master.network

import effect_master.client.EffectConfigScreen
import effect_master.config.EffectOption
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.network.NetworkEvent

/**
 * サーバー → クライアント。設定画面を開かせる。
 *
 * 画面が表示に使うスナップショットを同梱するので、クライアントは設定を保持しなくてよい。
 * [editable] が false なら閲覧専用で開く。owner でない人が `/effectconfig` を打った場合で、
 * サーバーは書き込みパケットを受けたときにもう一度権限を確かめる
 * （[UpdateEffectSettingsPacket] 参照）。
 */
class OpenConfigScreenPacket(
    private val settings: Map<ResourceLocation, EffectOption>,
    private val editable: Boolean,
) {
    fun encode(buf: FriendlyByteBuf) {
        buf.writeBoolean(editable)
        EffectSettingsCodec.encode(buf, settings)
    }

    fun handle(context: NetworkEvent.Context) {
        // 物理サーバーには画面のクラスが無いので、クライアントでだけ触る。
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT) {
            Runnable { EffectConfigScreen.open(settings, editable) }
        }
    }

    companion object {
        fun decode(buf: FriendlyByteBuf): OpenConfigScreenPacket {
            val editable = buf.readBoolean()
            return OpenConfigScreenPacket(EffectSettingsCodec.decode(buf), editable)
        }
    }
}
