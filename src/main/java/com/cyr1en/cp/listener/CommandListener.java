package com.cyr1en.cp.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import com.cyr1en.cp.CommandPrompter;
import com.cyr1en.cp.util.SRegex;

import java.util.List;

public class CommandListener implements Listener {

    private CommandPrompter plugin;

    public CommandListener(CommandPrompter plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        SRegex simpleRegex = new SRegex();
        simpleRegex.find(event.getMessage(), "<.*?>");
        List<String> prompts = simpleRegex.getResults();
        if (prompts.size() > 0) {
            event.setCancelled(true);
            Bukkit.getPluginManager().registerEvents(new Prompt(plugin, event.getPlayer(), prompts, event.getMessage()), plugin);
        }
    }

}