package gameincluded.keywhitelister;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public final class KeyWhitelister extends JavaPlugin implements Listener {
    private Connection connection;
    public String host, database, username, password;
    public int port;

    @Override
    public void onEnable() {
        // Plugin startup
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("Has been enabled!");
        Bukkit.setWhitelist(true); //enable whitelist

        mysqlSetup();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("Plugin disabled!");
    }

    public void mysqlSetup() {
        host = "localhost";
        port = 3306;
        database = "keywhitelister";
        username = "root";
        password = "";

        try {
            synchronized (this) {
                if (getConnection() != null && !getConnection().isClosed()) {
                    return;
                }
                Class.forName("com.mysql.jdbc.Driver");
                setConnection(DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password));
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "MYSQL CONNECTED");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @EventHandler
    public void onJoin(PlayerLoginEvent event) {

        //Defines player
        Player p = event.getPlayer();

        //Checks if Ruben/RFlintstone's main account is joining
        if (p.getUniqueId().toString().equals("c043bc1c-771d-4e6e-ad30-f3560127d421")) {
            if (Bukkit.getWhitelistedPlayers().contains(p)) {
                System.out.println("RFlintstone just joined and was already added to the whitelist.");
            } else {
                p.setWhitelisted(true);
                System.out.println("RFlintstone just joined the game and has been added to the whitelist automatically.");
                p.sendMessage("Someone removed you from the whitelist.");
                p.sendMessage("For your convenience I added you back on the whitelist.");
            }
        }


        //Database check here
        try {
            PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM users WHERE uuid=? AND username=?");
//            PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM users WHERE uuid=?");
            statement.setString(1, p.getUniqueId().toString());
            statement.setString(2, p.getName());
            ResultSet results = statement.executeQuery();
            results.next();
            int count = results.getRow();

            System.out.println(results);
            System.out.println("Count: " + count);

            if (count > 0) {
                if (Bukkit.getWhitelistedPlayers().contains(p)) {
                    if (!p.getUniqueId().toString().equals("c043bc1c-771d-4e6e-ad30-f3560127d421")) { //This prevents sending multiple join/console messages when Ruben/RFlintstone's main account joins
                        System.out.println(p.getName() + " just joined and was already added to the whitelist.");
                    }
                    //Do nothing with regular player when whitelisted
                } else {
                    if (!p.getUniqueId().toString().equals("c043bc1c-771d-4e6e-ad30-f3560127d421")) { //This prevents sending multiple join/console messages when Ruben/RFlintstone's main account joins
                        System.out.println(p.getName() + " just joined the game and has been added to the whitelist automatically.");
                    }
                    p.setWhitelisted(true); //whitelist player when in database
                }
                System.out.println("Row count passed");
            }
            if (count <= 0){ //Removes player from whitelist if they are not in the database
                if (Bukkit.getWhitelistedPlayers().contains(p)) {
                    p.setWhitelisted(false);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

//                [whitelist cheat sheet]
//
//                if (Bukkit.getWhitelistedPlayers().contains(player)) {
//                //check if player is on whitelist
//                }
//                player.setWhitelisted(true); //set player on whitelist
//                player.setWhitelisted(false); //remove player from whitelist
//
//                Bukkit.setWhitelist(true); //enable whitelist
//                Bukkit.setWhitelist(false); //disable whitelist
//                Bukkit.reloadWhitelist(); //reload whitelist
