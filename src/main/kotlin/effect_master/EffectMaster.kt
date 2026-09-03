package effect_master

import effect_master.network.EffectMasterNetwork
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS

/**
 * MOD のエントリポイント。
 *
 * 設定はワールド（サーバー）単位で 1 つだけ持ち、書き換えられるのは owner だけ。
 * 実際の処理は次の 3 つに分かれている。
 *
 * - [effect_master.config]  設定の保持と JSON への永続化
 * - [effect_master.server]  設定をプレイヤーに反映させるサーバー側の処理とコマンド
 * - [effect_master.client]  Cloth Config による設定画面
 */
@Mod(EffectMaster.ID)
object EffectMaster {
    const val ID = "effect_master"

    /**
     * op されたプレイヤーを owner とみなす権限レベル。バニラで言えばゲームルールを
     * 変えられる立場にあたる。
     *
     * シングルプレイのホストはこの値に関係なく owner として扱う。判定の全体は
     * [effect_master.server.EffectMasterPermissions.isOwner] を参照。
     */
    const val OWNER_PERMISSION_LEVEL = 2

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        MOD_BUS.addListener(::onCommonSetup)
    }

    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        event.enqueueWork(EffectMasterNetwork::register)
    }
}
