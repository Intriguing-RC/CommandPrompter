/*
 * MIT License
 *
 * Copyright (c) 2020 Ethan Bacurio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cyr1en.cp.listener;

import com.cyr1en.cp.CommandPrompter;
import com.cyr1en.cp.PromptRegistry;
import com.cyr1en.cp.util.SRegex;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class CommandParser {

  public static void parse(Player player, String command, BiConsumer<Player, String> fallBack) {
    CommandParser.parse(null, player, command, fallBack);
  }

  public static void parse(CommandPrompter plugin, Player player, String command, BiConsumer<Player, String> fallBack) {
    plugin = Objects.isNull(plugin) ? CommandPrompter.getInstance() : plugin;
    if (plugin.getConfiguration().getBoolean("Enable-Permission") && !player.hasPermission("commandprompter.use")) {
      return;
    }
    if (PromptRegistry.inCommandProcess(player.getPlayer())) {
      String prefix = plugin.getConfiguration().getString("Prompt-Prefix");
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getI18N().getProperty("PromptInProgress")));
      fallBack.accept(player, command);
    } else {
      List<String> prompts = findPrompts(command);
      if (prompts.size() > 0) {
        fallBack.accept(player, command);
        PromptRegistry.registerPrompt(new Prompt(plugin, player, new LinkedList<>(prompts), command));
      }
    }
  }

  public static List<String> findPrompts(String command) {
    CommandPrompter plugin = CommandPrompter.getInstance();
    SRegex simpleRegex = new SRegex(command);
    String regex = plugin.getConfiguration().getString("Argument-Regex").trim();
    String parsedEscapedRegex = (String.valueOf(regex.charAt(0))).replaceAll("[^\\w\\s]", "\\\\$0") +
            (regex.substring(1, regex.length() - 1)) +
            (String.valueOf(regex.charAt(regex.length() - 1))).replaceAll("[^\\w\\s]", "\\\\$0");
    simpleRegex.find(Pattern.compile(parsedEscapedRegex));
    return simpleRegex.getResultsList();
  }

  public static boolean prompted(String command) {
    List<String> prompts = CommandParser.findPrompts(command);
    return prompts.size() > 0;
  }
}


