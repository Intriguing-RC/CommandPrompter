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

package com.cyr1en.cp.instrumentation;

import com.cyr1en.cp.listener.CommandParser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;

public class Delegate {

  private static SimpleCommandMap MAP = null;

  static {
    try {
      Field field = SimplePluginManager.class.getDeclaredField("commandMap");
      field.setAccessible(true);
      MAP = (SimpleCommandMap) field.get(Bukkit.getServer().getPluginManager());
    } catch (IllegalAccessException | NoSuchFieldException e) {
      e.printStackTrace();
    }
  }

  public static boolean dispatch(@NotNull CommandSender sender, @NotNull String commandLine) throws CommandException {
    System.out.println("Using delegated dispatch");
    if (sender instanceof Player) {
      if(CommandParser.prompted(commandLine)) {
        CommandParser.parse((Player) sender, commandLine, (s, p) -> {
        });
        return true;
      }
    }

    String[] args = commandLine.split(" ");

    if (args.length == 0) {
      return false;
    }

    String sentCommandLabel = args[0].toLowerCase(java.util.Locale.ENGLISH);
    Command target = MAP.getCommand(sentCommandLabel);

    if (target == null) {
      return false;
    }

    try {
      target.execute(sender, sentCommandLabel, Arrays.copyOfRange(args, 1, args.length));
    } catch (CommandException ex) {
      throw ex;
    } catch (Throwable ex) {
      throw new CommandException("Unhandled exception executing '" + commandLine + "' in " + target, ex);
    }

    return true;
  }
}
