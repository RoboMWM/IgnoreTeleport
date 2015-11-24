package me.robomwm.IgnoreTeleport;

import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import static org.bukkit.Bukkit.getServer;

/**
 * Created by Robo on 11/24/2015.
 */
public class CommandListener implements Listener
{
    GriefPrevention gp = (GriefPrevention)getServer().getPluginManager().getPlugin("GriefPrevention");
    DataStore ds = gp.dataStore;
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        String message = event.getMessage();
        String [] args = message.split(" ");
        //We don't care if it's just a command without arguments
        if (args.length < 2)
            return;
        String command = args[0].toLowerCase();
        //Check the command
        if (command.equals("/tpa") || command.equals("/tp") || command.equals("/visit"))
        {
            Player sender = event.getPlayer();
            //First see if sender is ignored/is ignoring target
            if (tryIgnore(sender, args))
            {
                event.setCancelled(true); //Successful? Good, cancel and we're done
                return;
            }

            //Otherwise check if sender is softmuted in GriefPrevention
            else if (ds.isSoftMuted((sender.getUniqueId())))
            {
                if (trySoftmute(sender, args)) //wow my inconsistent code style
                    event.setCancelled(true);
            }
        }
    }

    // ------------ Methods ---------------- //

    public void sendSoftMessage(Player sender)
    {
        sender.sendMessage(ChatColor.RED + "Hmm, the transporter is having issues teleporting you. Maybe try someone else?");
    }
    //Basically a copy of
    //https://github.com/ryanhamshire/GriefPrevention/blob/7067db624de85e153488cbe41afbcc1c8e948754/src/me/ryanhamshire/GriefPrevention/PlayerEventHandler.java#L532
    //As he does not have this as a separate method to API into (would be nice if there was).
    public boolean isIgnored(Player sender, Player target)
    {
        PlayerData playerData = ds.getPlayerData(target.getPlayer().getUniqueId());
        if (playerData.ignoredPlayers.containsKey(sender.getUniqueId()))
            return true;
        playerData = ds.getPlayerData(sender.getPlayer().getUniqueId());
        if (playerData.ignoredPlayers.containsKey(target.getUniqueId()))
        {
            //Send info message if player is ignoring their recipient
            sender.sendMessage(ChatColor.RED + "You need to " + ChatColor.GOLD + "/unignore " + target.getName() +
                    ChatColor.RED + " to be teleported to them.");
            return false;
        }
        else
            return false;
    }

    //Checks if a player qualifies to receive a soft message.
    public boolean trySoftmute(Player sender, String[] args)
    {
        Player target;
        //Check if recipient is online
        if (getServer().getPlayer(args[1]) != null)
        {
            target = getServer().getPlayer(args[1]);
        }
        //Otherwise we don't care
        else
            return false;
        //We don't care if sender is also the recipient
        //or if receiver is also softmuted
        if ((sender != target) && !ds.isSoftMuted(target.getUniqueId()))
        {
            sendSoftMessage(sender);
            return true;
        }
        else
            return false;
    }
    public boolean tryIgnore(Player sender, String[] args)
    {
        Player target;
        //Check if recipient is online
        if (getServer().getPlayer(args[1]) != null)
        {
            target = getServer().getPlayer(args[1]);
        }
        //Otherwise we don't care
        else
            return false;

        //First check if either player is ignoring the other
        if (isIgnored(sender, target))
        {
            sendSoftMessage(sender);
            return true;
        }
        else
            return false;
    }
}
