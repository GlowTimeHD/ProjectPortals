package com.gmail.trentech.pjp;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.trentech.pjp.commands.CMDBack;
import com.gmail.trentech.pjp.commands.CommandManager;
import com.gmail.trentech.pjp.data.builder.HomeDataManipulatorBuilder;
import com.gmail.trentech.pjp.data.builder.PortalDataManipulatorBuilder;
import com.gmail.trentech.pjp.data.immutable.ImmutableHomeData;
import com.gmail.trentech.pjp.data.immutable.ImmutablePortalData;
import com.gmail.trentech.pjp.data.mutable.HomeData;
import com.gmail.trentech.pjp.data.mutable.PortalData;
import com.gmail.trentech.pjp.listeners.ButtonListener;
import com.gmail.trentech.pjp.listeners.DoorListener;
import com.gmail.trentech.pjp.listeners.LeverListener;
import com.gmail.trentech.pjp.listeners.PlateListener;
import com.gmail.trentech.pjp.listeners.PortalListener;
import com.gmail.trentech.pjp.listeners.SignListener;
import com.gmail.trentech.pjp.listeners.TeleportListener;
import com.gmail.trentech.pjp.listeners.TempListener;
import com.gmail.trentech.pjp.portals.Button;
import com.gmail.trentech.pjp.portals.Door;
import com.gmail.trentech.pjp.portals.Lever;
import com.gmail.trentech.pjp.portals.Plate;
import com.gmail.trentech.pjp.portals.Portal;
import com.gmail.trentech.pjp.portals.Warp;
import com.gmail.trentech.pjp.utils.ConfigManager;
import com.gmail.trentech.pjp.utils.Resource;
import com.gmail.trentech.pjp.utils.SQLUtils;

import me.flibio.updatifier.Updatifier;
import ninja.leaping.configurate.ConfigurationNode;

@Updatifier(repoName = "ProjectPortals", repoOwner = "TrenTech", version = Resource.VERSION)
@Plugin(id = Resource.ID, name = Resource.NAME, version = Resource.VERSION, dependencies = "after: Updatifier")
public class Main {

	private static Game game;
	private static Logger log;	
	private static PluginContainer plugin;

	@Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
		game = Sponge.getGame();
		plugin = getGame().getPluginManager().getPlugin(Resource.ID).get();
		log = getGame().getPluginManager().getLogger(plugin);
    }

    @Listener
    public void onInitialization(GameInitializationEvent event) {
    	ConfigurationNode config = new ConfigManager().getConfig();
    	ConfigurationNode commands = config.getNode("settings", "commands");
    	ConfigurationNode modules = config.getNode("settings", "modules");
    	
    	getGame().getEventManager().registerListeners(this, new TeleportListener());
    	
    	getGame().getCommandManager().register(this, new CMDBack().cmdBack, "back", commands.getNode("back").getString());
    	getGame().getCommandManager().register(this, new CommandManager().cmdPJP, "pjp");
    	
    	getGame().getDataManager().register(PortalData.class, ImmutablePortalData.class, new PortalDataManipulatorBuilder());
    	
    	if(modules.getNode("portals").getBoolean()){  		
    		getGame().getEventManager().registerListeners(this, new PortalListener());
    		getGame().getCommandManager().register(this, new CommandManager().cmdPortal, "portal", commands.getNode("portal").getString());
    		getLog().info("Portal module activated");
    	}
    	if(modules.getNode("buttons").getBoolean()){
    		getGame().getEventManager().registerListeners(this, new ButtonListener());
    		getGame().getCommandManager().register(this, new CommandManager().cmdButton, "button", commands.getNode("button").getString());
    		getLog().info("Button module activated");
    	}
    	if(modules.getNode("doors").getBoolean()){
    		getGame().getEventManager().registerListeners(this, new DoorListener());
    		getGame().getCommandManager().register(this, new CommandManager().cmdDoor, "door", commands.getNode("door").getString());
    		getLog().info("Door module activated");
    	}
    	if(modules.getNode("plates").getBoolean()){
    		getGame().getEventManager().registerListeners(this, new PlateListener());
    		getGame().getCommandManager().register(this, new CommandManager().cmdPlate, "plate", commands.getNode("plate").getString());
    		getLog().info("Plate module activated");
    	}
    	if(modules.getNode("signs").getBoolean()){
    		getGame().getEventManager().registerListeners(this, new SignListener());
    		getGame().getCommandManager().register(this, new CommandManager().cmdSign, "sign", commands.getNode("sign").getString());
    		getLog().info("Sign module activated");
    	}
    	if(modules.getNode("levers").getBoolean()){
    		getGame().getEventManager().registerListeners(this, new LeverListener());
    		getGame().getCommandManager().register(this, new CommandManager().cmdLever, "lever", commands.getNode("lever").getString());
    		getLog().info("Lever module activated");
    	}
    	if(modules.getNode("homes").getBoolean()){
    		getGame().getDataManager().register(HomeData.class, ImmutableHomeData.class, new HomeDataManipulatorBuilder());
    		getGame().getEventManager().registerListeners(this, new TempListener());
    		getGame().getCommandManager().register(this, new CommandManager().cmdHome, "home", commands.getNode("home").getString());
    		getLog().info("Home module activated");
    	}
    	if(modules.getNode("warps").getBoolean()){
    		getGame().getEventManager().registerListeners(this, new SignListener());
    		getGame().getCommandManager().register(this, new CommandManager().cmdWarp, "warp", commands.getNode("warp").getString());
    		getLog().info("Warp module activated");
    	}
    	
    	SQLUtils.createTables();
    	
    	convertWarps();
    	convertPortals();
    }

    @Listener
    public void onStartedServer(GameStartedServerEvent event) {
		if(new ConfigManager().getConfig().getNode("options", "particles").getBoolean()){
	    	for(Portal portal : Portal.list()){
	    		createTask(portal.getName(), portal.getFill());
	    	}
		}
    }

	public static Logger getLog() {
        return log;
    }
    
	public static Game getGame() {
		return game;
	}

	public static PluginContainer getPlugin() {
		return plugin;
	}

	public static void createTask(String name, List<Location<World>> locations){
        Main.getGame().getScheduler().createTaskBuilder().interval(300, TimeUnit.MILLISECONDS).name(name).execute(new Runnable() {
        	ThreadLocalRandom random = ThreadLocalRandom.current();
        	
			@Override
            public void run() {
				for(Location<World> location : locations){
					double v1 = 0.0 + (1 - 0.0) * random.nextDouble();
					double v2 = 0.0 + (1 - 0.0) * random.nextDouble();
					double v3 = 0.0 + (1 - 0.0) * random.nextDouble();

					if(random.nextDouble() < 0.5){
						location.getExtent().spawnParticles(Main.getGame().getRegistry().createBuilder(ParticleEffect.Builder.class)
								.type(ParticleTypes.PORTAL).motion(Vector3d.ZERO).offset(Vector3d.ZERO).count(3).build(), location.getPosition().add(v1,v2,v3));
					}
					if(random.nextDouble() < 0.5){
						location.getExtent().spawnParticles(Main.getGame().getRegistry().createBuilder(ParticleEffect.Builder.class)
								.type(ParticleTypes.PORTAL).motion(Vector3d.ZERO).offset(Vector3d.ZERO).count(3).build(), location.getPosition().add(v1,v3,v2));
					}
					if(random.nextDouble() < 0.5){
						location.getExtent().spawnParticles(Main.getGame().getRegistry().createBuilder(ParticleEffect.Builder.class)
								.type(ParticleTypes.PORTAL).motion(Vector3d.ZERO).offset(Vector3d.ZERO).count(3).build(), location.getPosition().add(v2,v3,v1));
					}
					if(random.nextDouble() < 0.5){
						location.getExtent().spawnParticles(Main.getGame().getRegistry().createBuilder(ParticleEffect.Builder.class)
								.type(ParticleTypes.PORTAL).motion(Vector3d.ZERO).offset(Vector3d.ZERO).count(3).build(), location.getPosition().add(v2,v1,v3));
					}
					if(random.nextDouble() < 0.5){
						location.getExtent().spawnParticles(Main.getGame().getRegistry().createBuilder(ParticleEffect.Builder.class)
								.type(ParticleTypes.PORTAL).motion(Vector3d.ZERO).offset(Vector3d.ZERO).count(3).build(), location.getPosition().add(v3,v1,v3));
					}
					if(random.nextDouble() < 0.5){
						location.getExtent().spawnParticles(Main.getGame().getRegistry().createBuilder(ParticleEffect.Builder.class)
								.type(ParticleTypes.PORTAL).motion(Vector3d.ZERO).offset(Vector3d.ZERO).count(3).build(), location.getPosition().add(v3,v2,v3));
					}
				}

            }
        }).submit(getPlugin());
	}

	public void convertWarps(){
		String folder = "config" + File.separator + "projectportals";
		
        File configFile = new File(folder, "warps.conf");
        
        if(!configFile.exists()){
        	return;
        }
        
		ConfigManager configManager = new ConfigManager("warps.conf");
		ConfigurationNode config = configManager.getConfig();
		
		Map<Object, ? extends ConfigurationNode> warps = config.getNode("Warps").getChildrenMap();

		if(!warps.isEmpty()){
			for(Entry<Object, ? extends ConfigurationNode> warp : warps.entrySet()){
				String name = warp.getKey().toString();
				
				String worldName = config.getNode("Warps", name, "World").getString();
				
				int x = config.getNode("Warps", name, "X").getInt();
				int y = config.getNode("Warps", name, "Y").getInt();
				int z = config.getNode("Warps", name, "Z").getInt();

				String destination = worldName + ":" + x + "." + y + "." + z;
				
				Warp.save(name, destination);
			}
		}
		configFile.delete();
	}
	
	public void convertPortals(){
		String folder = "config" + File.separator + "projectportals";
		
        File configFile = new File(folder, "portals.conf");
        
        if(!configFile.exists()){
        	return;
        }

		String[] p = {"Buttons", "Doors", "Levers", "Plates"};

		ConfigManager configManager = new ConfigManager("portals.conf");
		ConfigurationNode config = configManager.getConfig();
		
		for(String portal : p){
			Map<Object, ? extends ConfigurationNode> nodes = config.getNode(portal).getChildrenMap();

			if(!nodes.isEmpty()){
				for(Entry<Object, ? extends ConfigurationNode> node : nodes.entrySet()){
					String name = node.getKey().toString();
					
					String destination = config.getNode(portal, name).getString();

					switch(portal){
					case "Buttons": Button.save(name, destination); break;
					case "Doors": Door.save(name, destination); break;
					case "Levers": Lever.save(name, destination); break;
					case "Plates": Plate.save(name, destination); break;
					}				
				}
			}
		}
		
		Map<Object, ? extends ConfigurationNode> portals = config.getNode("Portals").getChildrenMap();

		if(!portals.isEmpty()){
			for(Entry<Object, ? extends ConfigurationNode> node : portals.entrySet()){
				String name = node.getKey().toString();
				
				List<String> frame = config.getNode("Portal", name, "Frame").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
				List<String> fill = config.getNode("Portal", name, "Fill").getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
				
				String destination = config.getNode("Portals", name, "Destination").getString();

				Portal.save(new Portal(destination, destination, frame, fill));
			}
		}
		configFile.delete();
	}
}