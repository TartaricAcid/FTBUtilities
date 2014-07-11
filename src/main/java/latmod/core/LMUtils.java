package latmod.core;
import java.io.*;
import java.lang.reflect.Type;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import net.minecraft.block.BlockPistonBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.common.ForgeDirection;

public class LMUtils
{
	public static boolean anyEquals(Object o, Object[] o1)
	{
		for(int i = 0; i < o1.length; i++)
		{
			if(o == null && o1[i] == null) return true;
			if(o != null && o1[i] != null)
			{ if(o == o1[i] || o.equals(o1[i])) return true; }
		}
		
		return false;
	}
	
	public static boolean allEquals(Object o, Object[] o1)
	{
		for(int i = 0; i < o1.length; i++)
		{
			if((o == null && o1[i] != null) || (o != null && o1[i] == null)) return false;
			if(o != o1[i]) { if(!o.equals(o1[i])) return false; }
		}
		
		return true;
	}
	
	public static ForgeDirection get2DRotation(EntityLivingBase el)
	{
		int i = MathHelper.floor_float(el.rotationYaw * 4F / 360F + 0.5F) & 3;
		if(i == 0) return ForgeDirection.NORTH;
		else if(i == 1) return ForgeDirection.EAST;
		else if(i == 2) return ForgeDirection.SOUTH;
		else if(i == 3) return ForgeDirection.WEST;
		return ForgeDirection.UNKNOWN;
	}
	
	public static ForgeDirection get3DRotation(World w, int x, int y, int z, EntityLivingBase el)
	{ return ForgeDirection.values()[BlockPistonBase.determineOrientation(w, x, y, z, el)]; }
	
	public static String getPath(ResourceLocation res)
	{ return "/assets/" + res.getResourceDomain() + "/" + res.getResourcePath(); }
	
	public static String stripe(Object... i)
	{
		StringBuilder s = new StringBuilder();
		for(int j = 0; j < i.length; j++)
		{ s.append(i[j]);
		if(j != i.length - 1) s.append(", "); }
		return s.toString();
	}
	
	public static String stripeD(double... i)
	{
		StringBuilder s = new StringBuilder();
		for(int j = 0; j < i.length; j++)
		{ s.append(((long)(i[j] * 100D)) / 100D);
		if(j != i.length - 1) s.append(", "); }
		return s.toString();
	}
	
	public static String stripeF(float... i)
	{
		StringBuilder s = new StringBuilder();
		for(int j = 0; j < i.length; j++)
		{ s.append(((int)(i[j] * 100F)) / 100F);
		if(j != i.length - 1) s.append(", "); }
		return s.toString();
	}
	
	public static final double[] getMidPoint(double[] pos1, double[] pos2, float p)
	{
		double x = pos2[0] - pos1[0];
		double y = pos2[1] - pos1[1];
		double z = pos2[2] - pos1[2];
		double d = Math.sqrt(x * x + y * y + z * z);
		return new double[] { pos1[0] + (x / d) * (d * p), pos1[1] + (y / d) * (d * p), pos1[2] + (z / d) * (d * p) };
	}
	
	//TODO: Still need to fix this
	@Deprecated
	public static void teleportEntity(Entity e, int dim)
	{
		if ((e.worldObj.isRemote) || (e.isDead) || e.dimension == dim) return;
		
		LatCore.printChat(null, "Teleporting to dimension " + dim);
		
		e.worldObj.theProfiler.startSection("changeDimension");
		MinecraftServer minecraftserver = MinecraftServer.getServer();
		int j = e.dimension;
		WorldServer worldserver = minecraftserver.worldServerForDimension(j);
		WorldServer worldserver1 = minecraftserver.worldServerForDimension(dim);
		e.dimension = dim;
		e.worldObj.removeEntity(e);
		e.isDead = false;
		e.worldObj.theProfiler.startSection("reposition");
		minecraftserver.getConfigurationManager().transferEntityToWorld(e, j, worldserver, worldserver1);
		e.worldObj.theProfiler.endStartSection("reloading");
		Entity entity = EntityList.createEntityByName(EntityList.getEntityString(e), worldserver1);
		if (entity != null)
		{
			entity.copyDataFrom(e, true);
			worldserver1.spawnEntityInWorld(entity);
		}
		e.isDead = true;
		e.worldObj.theProfiler.endSection();
		worldserver.resetUpdateEntityTick();
		worldserver1.resetUpdateEntityTick();
		e.worldObj.theProfiler.endSection();
	}
	
	public static void dropItem(World w, double x, double y, double z, ItemStack is, int delay)
	{
		if(w == null || is == null || is.stackSize == 0) return;
		
		EntityItem ei = new EntityItem(w, x, y, z, is.copy());
		ei.motionX = w.rand.nextGaussian() * 0.07F;
		ei.motionY = w.rand.nextFloat() * 0.05F;
		ei.motionZ = w.rand.nextGaussian() * 0.07F;
		ei.delayBeforeCanPickup = delay;
		w.spawnEntityInWorld(ei);
	}
	
	public static void dropItem(Entity e, ItemStack is)
	{ dropItem(e.worldObj, e.posX, e.posY, e.posZ, is, 0); }
	
	public static <T> T fromJson(String s, Type t)
	{
		if(s == null || s.length() < 2) s = "{}";
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		return gson.fromJson(s, t);
	}
	
	public static <T> T fromJsonFromFile(File f, Type t)
	{
		try
		{
			FileInputStream fis = new FileInputStream(f);
			byte[] b = new byte[fis.available()];
			fis.read(b); fis.close();
			return fromJson(new String(b), t);
		}
		catch(Exception e)
		{ e.printStackTrace(); return null; }
	}
	
	public static String toJson(Object o, boolean asTree)
	{
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		
		if(asTree)
		{
			StringWriter sw = new StringWriter();
			JsonWriter jw = new JsonWriter(sw);
			jw.setIndent("\t");
			gson.toJson(o, o.getClass(), jw);
			return sw.toString();
		}
		
		return gson.toJson(o);
	}
	
	public static void toJsonFile(File f, Object o)
	{
		String s = toJson(o, true);
		
		try
		{
			if(!f.exists())
			{
				File f0 = f.getParentFile();
				if(!f0.exists()) f0.mkdirs();
				f.createNewFile();
			}
			
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(s.getBytes()); fos.close();
		}
		catch(Exception e)
		{ e.printStackTrace(); }
	}

	public static String[] split(String s, String regex)
	{
		String s1[] = s.split(regex);
		return (s1 == null || s1.length == 0) ? new String[] { s } : s1;
	}
	
	public static MovingObjectPosition rayTrace(EntityPlayer ep, double d)
	{
		double oy = 1.62D;// ep.getEyeHeight() - ep.getDefaultEyeHeight();
		Vec3 pos = ep.worldObj.getWorldVec3Pool().getVecFromPool(ep.posX, ep.posY + oy, ep.posZ);
		Vec3 look = ep.getLook(1F);
		Vec3 vec = pos.addVector(look.xCoord * d, look.yCoord * d, look.zCoord * d);
		return ep.worldObj.rayTraceBlocks_do_do(pos, vec, false, true);
	}
	
	public static ItemStack getStackFromUnlocazliedName(String name, int dmg)
	{
		if(name == null) return null;
		
		FastList<ItemStack> temp = new FastList<ItemStack>();
		
		for(int i = 0; i < Item.itemsList.length; i++)
		{
			if(Item.itemsList[i] != null)
			{
				temp.clear();
				Item.itemsList[i].getSubItems(Item.itemsList[i].itemID, CreativeTabs.tabAllSearch, temp);
				
				if(!temp.isEmpty())
				for(ItemStack is : temp)
				{
					if(is != null)
					{
						String s = is.getUnlocalizedName();
						
						if(s != null && s.equals(name))
						{
							if(dmg == LatCore.ANY || dmg == is.getItemDamage())
								return is;
						}
					}
				}
			}
		}
		
		return null;
	}
}