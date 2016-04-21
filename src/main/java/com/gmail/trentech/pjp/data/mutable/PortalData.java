package com.gmail.trentech.pjp.data.mutable;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.trentech.pjp.Main;
import com.gmail.trentech.pjp.data.PJPKeys;
import com.gmail.trentech.pjp.data.immutable.ImmutablePortalData;
import com.gmail.trentech.pjp.utils.Rotation;
import com.gmail.trentech.pjp.utils.Utils;
import com.google.common.base.Objects;

public class PortalData extends AbstractData<PortalData, ImmutablePortalData> {

	private String name;
	private String destination;
	private double price;
	
	public PortalData() {
		this("","", 0);
	}
	
	public PortalData(String name, World world, boolean random) {
		this.name = name;
		if(random){
			this.destination = world.getName() + ":random";
		}else{
			this.destination = world.getName() + ":spawn";
		}
	}
	
	public PortalData(String name, World world, Rotation rotation) {
		this.name = name;
		this.destination = world.getName() + ":spawn:" + rotation.getName();
	}
	
	public PortalData(String name, Location<World> destination, Rotation rotation) {
		this.name = name;
		this.destination = destination.getExtent().getName() + ":" + destination.getBlockX() + "." + destination.getBlockY() + "." + destination.getBlockZ() + ":" + rotation.getName();
	}
	
	public PortalData(String name, Location<World> destination) {
		this.name = name;
		this.destination = destination.getExtent().getName() + ":" + destination.getBlockX() + "." + destination.getBlockY() + "." + destination.getBlockZ();
	}
	
	public PortalData(String name, String destination, double price) {
		this.name = name;
		this.destination = destination;
		this.price = price;
	}

	public Value<String> name() {
        return Sponge.getRegistry().getValueFactory().createValue(PJPKeys.PORTAL_NAME, this.name);
    }
	
	public Value<String> destination() {
        return Sponge.getRegistry().getValueFactory().createValue(PJPKeys.DESTINATION, this.destination);
    }
	
	public Value<Double> price() {
		return Sponge.getRegistry().getValueFactory().createValue(PJPKeys.PRICE, this.price);
	}
	
	public Optional<Location<World>> getDestination() {
		String[] args = destination.split(":");
		
		if(!Main.getGame().getServer().getWorld(args[0]).isPresent()){
			return Optional.empty();
		}
		World world = Main.getGame().getServer().getWorld(args[0]).get();
		
		if(args[1].equalsIgnoreCase("random")){
			return Optional.of(Utils.getRandomLocation(world));
		}else if(args[1].equalsIgnoreCase("spawn")){
			return Optional.of(world.getSpawnLocation());
		}else{
			String[] coords = args[1].split("\\.");
			int x = Integer.parseInt(coords[0]);
			int y = Integer.parseInt(coords[1]);
			int z = Integer.parseInt(coords[2]);
			
			return Optional.of(world.getLocation(x, y, z));	
		}
	}
	
	public Optional<Vector3d> getRotation(){
		String[] args = destination.split(":");
		
		if(args.length != 3){
			return Optional.empty();
		}
		
		Optional<Rotation> optional = Rotation.get(args[2]);
		
		if(!optional.isPresent()){
			return Optional.empty();
		}
		
		return Optional.of(new Vector3d(0,optional.get().getValue(),0));
	}
	
	@Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(PJPKeys.PORTAL_NAME, () -> this.name);
        registerFieldSetter(PJPKeys.PORTAL_NAME, value -> this.name = value);
        registerKeyValue(PJPKeys.PORTAL_NAME, this::name);

        registerFieldGetter(PJPKeys.DESTINATION, () -> this.destination);
        registerFieldSetter(PJPKeys.DESTINATION, value -> this.destination = value);
        registerKeyValue(PJPKeys.DESTINATION, this::destination);
        
        registerFieldGetter(PJPKeys.PRICE, () -> this.price);
        registerFieldSetter(PJPKeys.PRICE, value -> this.price = value);
        registerKeyValue(PJPKeys.PRICE, this::price);
    }
	
	@Override
    public Optional<PortalData> fill(DataHolder dataHolder, MergeFunction overlap) {
        return Optional.empty();
    }

    @Override
    public Optional<PortalData> from(DataContainer container) {
        if (!container.contains(PJPKeys.PORTAL_NAME.getQuery(), PJPKeys.DESTINATION.getQuery(), PJPKeys.PRICE.getQuery())) {
            return Optional.empty();
        }
        name = container.getString(PJPKeys.PORTAL_NAME.getQuery()).get();
        destination = container.getString(PJPKeys.DESTINATION.getQuery()).get();
        price = container.getDouble(PJPKeys.PRICE.getQuery()).get();
        
        return Optional.of(this);
    }

    @Override
    public PortalData copy() {
        return new PortalData(this.name, this.destination, this.price);
    }

    @Override
    public ImmutablePortalData asImmutable() {
        return new ImmutablePortalData(this.name, this.destination, this.price);
    }

    @Override
    public int compareTo(PortalData o) {
        return 0;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer().set(PJPKeys.PORTAL_NAME, this.name).set(PJPKeys.DESTINATION, this.destination).set(PJPKeys.PRICE, this.price);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", this.name).add("destination", this.destination).add("price", this.price).toString();
    }
}