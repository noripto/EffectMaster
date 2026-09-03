package effect_master.network

import effect_master.config.EffectOption
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation

/**
 * 効果 id と設定の組をバッファに読み書きする。送受信の両パケットで使う。
 *
 * 受信側では [MAX_ENTRIES] を超えた時点で例外を投げる。壊れた、あるいは細工された
 * パケットで大量のエントリを読ませないための上限で、Forge が接続を切ってくれる。
 */
internal object EffectSettingsCodec {
    private const val MAX_ENTRIES = 4096

    fun encode(buf: FriendlyByteBuf, settings: Map<ResourceLocation, EffectOption>) {
        buf.writeVarInt(settings.size)
        for ((id, option) in settings) {
            buf.writeResourceLocation(id)
            buf.writeEnum(option)
        }
    }

    fun decode(buf: FriendlyByteBuf): Map<ResourceLocation, EffectOption> {
        val size = buf.readVarInt()
        require(size in 0..MAX_ENTRIES) { "Effect settings payload of $size entries is out of range" }

        val settings = HashMap<ResourceLocation, EffectOption>(size)
        repeat(size) {
            val id = buf.readResourceLocation()
            settings[id] = buf.readEnum(EffectOption::class.java)
        }
        return settings
    }
}
