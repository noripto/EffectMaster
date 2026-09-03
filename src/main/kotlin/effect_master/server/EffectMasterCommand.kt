package effect_master.server

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import effect_master.config.EffectSettings
import effect_master.network.EffectMasterNetwork
import effect_master.network.OpenConfigScreenPacket
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

/**
 * `/effectconfig` — 設定画面を開く。
 *
 * 誰でも実行できるが、書き換えられるのは owner だけ（[EffectMasterPermissions.isOwner]）。
 * それ以外のプレイヤーには閲覧専用として開かせる。
 */
object EffectMasterCommand {
    private const val NAME = "effectconfig"

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal(NAME).executes(::openConfigScreen))
    }

    private fun openConfigScreen(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source
        val player = source.player

        // コンソールやコマンドブロックには送る画面が無い。
        if (player == null) {
            source.sendFailure(Component.translatable("effect_master.command.player_only"))
            return 0
        }

        val editable = EffectMasterPermissions.isOwner(player)
        if (!editable) {
            source.sendSuccess({ Component.translatable("effect_master.command.read_only") }, false)
        }

        EffectMasterNetwork.sendToPlayer(OpenConfigScreenPacket(EffectSettings.snapshot(), editable), player)
        return Command.SINGLE_SUCCESS
    }
}
