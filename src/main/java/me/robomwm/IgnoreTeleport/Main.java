package me.robomwm.IgnoreTeleport;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Robo on 11/23/2015.
 */
public class Main extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(new CommandListener(), this);
    }
}
