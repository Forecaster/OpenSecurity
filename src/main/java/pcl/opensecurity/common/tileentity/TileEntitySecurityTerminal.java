package pcl.opensecurity.common.tileentity;

import com.mojang.authlib.GameProfile;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Visibility;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;
import pcl.opensecurity.util.UsernameCache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class TileEntitySecurityTerminal extends TileEntityOSBase {
    public void setOwner(String UUID) {
        this.ownerUUID = UUID;
    }

    public String getOwner() {
        return this.ownerUUID;
    }
    String ownerUUID = "";
    ArrayList<String> allowedUsers = new ArrayList<String>();
    private String password = "";
    public Block block;
    private Boolean enabled = false;
    boolean enableParticles = false;
    public int rangeMod = 1;

    public TileEntitySecurityTerminal(){
        node = Network.newNode(this, Visibility.Network).withComponent(getComponentName()).withConnector(128).create();
    }

    public boolean isUserAllowedToBypass(String uuid) {
        return uuid.equals(ownerUUID) || allowedUsers.contains(uuid);
    }

    private static String getComponentName() {
        return "os_securityterminal";
    }

    @Callback(doc = "function():boolean; Returns the status of the block", direct = true)
    public Object[] isEnabled(Context context, Arguments args) {
        return new Object[] { isEnabled() };
    }

    @Callback(doc = "function(String:Username):boolean; Adds the Minecraft User as an allowed user.", direct = true)
    public Object[] addUser(Context context, Arguments args) {
        if (args.checkString(0).equals(getPass())) {
            if (args.checkString(1).matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                allowedUsers.add(args.checkString(1));
            } else {
                GameProfile gameprofile = world.getMinecraftServer().getPlayerProfileCache().getGameProfileForUsername(args.checkString(1));

                if (gameprofile == null)
                {
                    return new Object[] { true, "Failed to get UUID from username" };
                } else {
                    allowedUsers.add(gameprofile.getId().toString());
                }
            }
            return new Object[] { true, "User added" };
        } else {
            return new Object[] { false, "Password was incorrect" };
        }
    }

    @Callback(doc = "function(String:Username):boolean; Removes the Minecraft User as an allowed user.", direct = true)
    public Object[] delUser(Context context, Arguments args) {
        if (args.checkString(0).equals(getPass())) {
            if (args.checkString(1).matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                allowedUsers.remove(args.checkString(1));
            } else {
                GameProfile gameprofile = world.getMinecraftServer().getPlayerProfileCache().getGameProfileForUsername(args.checkString(1));

                if (gameprofile == null)
                {
                    return new Object[] { true, "Failed to get UUID from username" };
                } else {
                    allowedUsers.remove(gameprofile.getId().toString());
                }
            }
            return new Object[] { true, "User removed" };
        } else {
            return new Object[] { false, "Password was incorrect" };
        }
    }

    @Callback(doc = "function(String:password):boolean; Sets the block password, required to enable/disable and other actions", direct = true)
    public Object[] setPassword(Context context, Arguments args) {
            if (getPass().isEmpty()) {
                setPass(args.checkString(0));
                return new Object[] { true, "Password set" };
            } else {
                if (args.checkString(0).equals(getPass())) {
                    setPass(args.checkString(1));
                    return new Object[] { true, "Password Changed" };
                } else {
                    return new Object[] { false, "Password was not changed" };
                }
            }
    }

    @Callback(doc = "function():boolean; Switches particles to show the corners of the protected area", direct = true)
    public Object[] toggleParticle(Context context, Arguments args) {
        if (args.optString(0, "").equals(getPass())) {
            enableParticles = !enableParticles;
            world.markBlockRangeForRenderUpdate(pos, pos);
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
            world.scheduleBlockUpdate(pos,this.getBlockType(),0,0);
            markDirty();
            return new Object[] { enableParticles };
        } else {
            return new Object[] { false, "Password incorrect" };
        }
    }

    @Callback(doc = "function(Int:range):boolean; Sets the range of the protction area 8*range max 4 min 1, increasing range increases energy cost.", direct = true)
    public Object[] setRange(Context context, Arguments args) {
        if (args.optString(0, "").equals(getPass())) {
            if (args.checkInteger(1) >= 1 && args.checkInteger(1) <= 4) {
                rangeMod = args.checkInteger(1);
                world.markBlockRangeForRenderUpdate(pos, pos);
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                world.scheduleBlockUpdate(pos,this.getBlockType(),0,0);
                markDirty();
                return new Object[] { true };
            }
            return new Object[] { false, "Range out of bounds 1-4" };
        } else {
            return new Object[] { false, "Password incorrect" };
        }
    }

    @Callback(doc = "function(String:password):boolean; Enables the block, requires the correct password", direct = true)
    public Object[] enable(Context context, Arguments args) {
        if (args.optString(0, "").equals(getPass())) {
            enabled = true;
            world.markBlockRangeForRenderUpdate(pos, pos);
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
            world.scheduleBlockUpdate(pos,this.getBlockType(),0,0);
            markDirty();
            return new Object[] { true };
        } else {
            return new Object[] { false, "Password incorrect" };
        }
    }

    @Callback(doc = "function(String:password):boolean; Disables the block, requires the correct password", direct = true)
    public Object[] disable(Context context, Arguments args) {
        if (args.optString(0, "").equals(getPass())) {
            enabled = false;
            return new Object[] { true };
        } else {
            return new Object[] { false, "Password incorrect" };
        }
    }

    @Callback(doc = "function(String:password):boolean; returns a comma delimited string of current allowed users.", direct = true)
    public Object[] getAllowedUsers(Context context, Arguments args) {
        if (args.optString(0, "").equals(getPass())) {
            try {
                String users = "";
                for (String user : allowedUsers) {
                    users = UsernameCache.getBlocking(UUID.fromString(user)) + ", ";
                }
                users = users.replaceAll(", $", "");
                return new Object[] {users};
            } catch (IOException e) {
                e.printStackTrace();
                return new Object[] { e.getMessage() };
            }
        } else {
            return new Object[] { false, "Password incorrect" };
        }
    }

    public void setPass(String pass) {
        this.password = pass;
    }

    public String getPass() {
        return this.password;
    }
    int ticksExisted = 0;
    @Override
    public void update() {
        super.update();
        if (node != null && node.network() == null) {
            Network.joinOrCreateNetwork(this);
        }
        if (!world.isRemote && ticksExisted%40==0 && isParticleEnabled()) {
            double motionX = world.rand.nextGaussian() * 0.02D;
            double motionY = world.rand.nextGaussian() * 0.02D;
            double motionZ = world.rand.nextGaussian() * 0.02D;
            WorldServer wServer = (WorldServer) world;
            //1
            wServer.spawnParticle(
                    EnumParticleTypes.BARRIER,
                    pos.getX() + 8 * rangeMod +0.5f,
                    pos.getY() + 8 * rangeMod +0.5f,
                    pos.getZ() + 8 * rangeMod +0.5f,
                    25,
                    motionX,
                    motionY,
                    motionZ,
                    0.5);
            //2
            wServer.spawnParticle(
                    EnumParticleTypes.BARRIER,
                    pos.getX() + 8 * rangeMod +0.5f,
                    pos.getY() + 8 * rangeMod +0.5f,
                    pos.getZ() - 8 * rangeMod +0.5f,
                    25,
                    motionX,
                    motionY,
                    motionZ,
                    0.5);
            //3
            wServer.spawnParticle(
                    EnumParticleTypes.BARRIER,
                    pos.getX() - 8 * rangeMod +0.5f,
                    pos.getY() - 8 * rangeMod +0.5f,
                    pos.getZ() - 8 * rangeMod +0.5f,
                    25,
                    motionX,
                    motionY,
                    motionZ,
                    0.5);
            //4
            wServer.spawnParticle(
                    EnumParticleTypes.BARRIER,
                    pos.getX() - 8 * rangeMod +0.5f,
                    pos.getY() + 8 * rangeMod +0.5f,
                    pos.getZ() + 8 * rangeMod +0.5f,
                    25,
                    motionX,
                    motionY,
                    motionZ,
                    0.5);
            //5
            wServer.spawnParticle(
                    EnumParticleTypes.BARRIER,
                    pos.getX() - 8 * rangeMod +0.5f,
                    pos.getY() + 8 * rangeMod +0.5f,
                    pos.getZ() - 8 * rangeMod +0.5f,
                    25,
                    motionX,
                    motionY,
                    motionZ,
                    0.5);
            //6
            wServer.spawnParticle(
                    EnumParticleTypes.BARRIER,
                    pos.getX() - 8 * rangeMod +0.5f,
                    pos.getY() - 8 * rangeMod +0.5f,
                    pos.getZ() + 8 * rangeMod +0.5f,
                    25,
                    motionX,
                    motionY,
                    motionZ,
                    0.5);
            //7
            wServer.spawnParticle(
                    EnumParticleTypes.BARRIER,
                    pos.getX() + 8 * rangeMod +0.5f,
                    pos.getY() - 8 * rangeMod +0.5f,
                    pos.getZ() - 8 * rangeMod +0.5f,
                    25,
                    motionX,
                    motionY,
                    motionZ,
                    0.5);
            //8
            wServer.spawnParticle(
                    EnumParticleTypes.BARRIER,
                    pos.getX() + 8 * rangeMod +0.5f,
                    pos.getY() - 8 * rangeMod +0.5f,
                    pos.getZ() + 8 * rangeMod +0.5f,
                    25,
                    motionX,
                    motionY,
                    motionZ,
                    0.5);
            ticksExisted = 0;
        }
        ticksExisted++;
    }

    public boolean isParticleEnabled() {
        return enableParticles;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (node != null && node.host() == this) {
            node.load(nbt.getCompoundTag("oc:node"));
        }
        this.ownerUUID = nbt.getString("owner");
        this.password= nbt.getString("password");
        this.enabled=nbt.getBoolean("enabled");
        this.rangeMod=nbt.getInteger("rangeMod");
        this.enableParticles=nbt.getBoolean("particles");
        this.allowedUsers=new ArrayList<String>(Arrays.asList(nbt.getString("allowedUsers").replaceAll(", $", "").split(", ")));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (node != null && node.host() == this) {
            final NBTTagCompound nodeNbt = new NBTTagCompound();
            node.save(nodeNbt);
            nbt.setTag("oc:node", nodeNbt);
        }
        nbt.setString("owner", this.ownerUUID);
        nbt.setString("password", this.password);
        nbt.setBoolean("enabled", this.isEnabled());
        nbt.setInteger("rangeMod", this.rangeMod);
        nbt.setBoolean("particles", this.isParticleEnabled());
        if (this.allowedUsers != null && this.allowedUsers.size() > 0)
            nbt.setString("allowedUsers", String.join(", ", this.allowedUsers).replaceAll(", $", ""));
        return nbt;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(pos, getBlockMetadata(), getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        if(net.getDirection() == EnumPacketDirection.CLIENTBOUND)
        {
            readFromNBT(pkt.getNbtCompound());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Boolean usePower() {
        if (node.tryChangeBuffer(-10 * rangeMod)) {
            return true;
        } else {
            return false;
        }
    }
}