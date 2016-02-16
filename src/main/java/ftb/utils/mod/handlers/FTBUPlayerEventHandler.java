package ftb.utils.mod.handlers;

import ftb.lib.*;
import ftb.lib.api.friends.*;
import ftb.lib.notification.Notification;
import ftb.utils.mod.FTBU;
import ftb.utils.mod.config.FTBUConfigGeneral;
import ftb.utils.mod.handlers.ftbl.FTBUWorldData;
import ftb.utils.world.claims.ChunkType;
import latmod.lib.MathHelperLM;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FTBUPlayerEventHandler
{
	@SubscribeEvent
	public void onChunkChanged(EntityEvent.EnteringChunk e)
	{
		if(e.entity.worldObj.isRemote || !(e.entity instanceof EntityPlayerMP)) return;
		
		EntityPlayerMP ep = (EntityPlayerMP) e.entity;
		LMPlayerMP player = LMWorldMP.inst.getPlayer(ep);
		if(player == null || !player.isOnline()) return;
		
		player.lastPos = new EntityPos(ep).toBlockDimPos();
		
		int currentChunkType = FTBUWorldData.serverInstance.getType(ep.dimension, e.newChunkX, e.newChunkZ).ID;
		
		if(player.lastChunkType == -99 || player.lastChunkType != currentChunkType)
		{
			player.lastChunkType = currentChunkType;
			
			ChunkType type = FTBUWorldData.getChunkTypeFromI(currentChunkType);
			IChatComponent msg;
			
			if(type.isClaimed())
				msg = new ChatComponentText(String.valueOf(LMWorldMP.inst.getPlayer(currentChunkType)));
			else msg = new ChatComponentTranslation(FTBU.mod.assets + type.lang);
			
			msg.getChatStyle().setColor(EnumChatFormatting.WHITE);
			msg.getChatStyle().setBold(true);
			
			Notification n = new Notification("chunk_changed", msg, 3000);
			n.setColor(type.getAreaColor(player));
			
			FTBLib.notifyPlayer(ep, n);
		}
	}
	
	@SubscribeEvent
	public void onPlayerAttacked(LivingAttackEvent e)
	{
		if(e.entity.worldObj.isRemote) return;
		
		int dim = e.entity.dimension;
		if(dim != 0 || !(e.entity instanceof EntityPlayerMP) || e.entity instanceof FakePlayer) return;
		
		Entity entity = e.source.getSourceOfDamage();
		
		if(entity != null && (entity instanceof EntityPlayerMP || entity instanceof IMob))
		{
			if(entity instanceof FakePlayer) return;
			else if(entity instanceof EntityPlayerMP && LMWorldMP.inst.getPlayer(entity).allowCreativeInteractSecure())
				return;
			
			int cx = MathHelperLM.chunk(e.entity.posX);
			int cz = MathHelperLM.chunk(e.entity.posZ);
			
			if((FTBUConfigGeneral.safe_spawn.get() && FTBUWorldData.isInSpawn(dim, cx, cz))) e.setCanceled(true);
			/*else
			{
				ClaimedChunk c = Claims.get(dim, cx, cz);
				if(c != null && c.claims.settings.isSafe()) e.setCanceled(true);
			}*/
		}
	}
}