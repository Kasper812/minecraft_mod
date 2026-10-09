package com.atsuishio.superbwarfare.command.builder

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.IdentifierArgument
import net.minecraft.resources.Identifier

class CommandNodeWithResourceLocationArg(builder: ArgumentBuilder<CommandSourceStack, *>, argName: String) :
    CommandNodeWithArg<Identifier>(builder, argName) {

    val CommandContext<CommandSourceStack>.resourceLocationArg
        get() = getArg(this@CommandNodeWithResourceLocationArg)

    override fun CommandContext<CommandSourceStack>.getArg(
        ctx: CommandNodeWithArg<Identifier>
    ): Identifier = IdentifierArgument.getId(this, ctx.name)
}

inline fun CommandNode.resourceLocationArg(
    argName: String = "$name.resourceLocation",
    suggests: SuggestionProvider<CommandSourceStack>? = null,
    builder: CommandNodeWithResourceLocationArg.() -> Unit
) {
    val argument = Commands.argument(argName, IdentifierArgument.id())
    suggests?.let { argument.suggests(it) }
    cmd += CommandNodeWithResourceLocationArg(argument, argName).apply(builder)
}
