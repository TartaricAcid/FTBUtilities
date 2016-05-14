package ftb.utils.net;

import ftb.lib.BlockDimPos;
import ftb.lib.ChunkDimPos;
import ftb.lib.api.ForgePlayerMP;
import ftb.lib.api.net.LMNetworkWrapper;
import ftb.lib.api.net.MessageToClient;
import ftb.utils.world.ChunkType;
import ftb.utils.world.FTBUWorldDataMP;
import ftb.utils.world.FTBUWorldDataSP;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class MessageAreaUpdate extends MessageToClient<MessageAreaUpdate>
{
	public DimensionType dim;
	public Map<ChunkDimPos, ChunkType> types;
	
	public MessageAreaUpdate() { }
	
	public MessageAreaUpdate(ForgePlayerMP p, int x, int z, DimensionType d, int sx, int sz)
	{
		dim = d;
		types = FTBUWorldDataMP.get().getChunkTypes(p, x, z, d, sx, sz);
	}
	
	public MessageAreaUpdate(ForgePlayerMP p, BlockDimPos pos, int radius)
	{ this(p, pos.chunkX() - radius, pos.chunkZ() - radius, pos.dim, radius * 2 + 1, radius * 2 + 1); }
	
	@Override
	public LMNetworkWrapper getWrapper()
	{ return FTBUNetHandler.NET; }
	
	@Override
	public void fromBytes(ByteBuf io)
	{
		dim = DimensionType.getById(io.readInt());
		int size = io.readInt();
		types = new HashMap<>(size);
		
		for(int i = 0; i < size; i++)
		{
			int x = io.readInt();
			int z = io.readInt();
			ChunkDimPos pos = new ChunkDimPos(dim, x, z);
			ChunkType type = ChunkType.read(pos, io);
			types.put(pos, type);
		}
	}
	
	@Override
	public void toBytes(ByteBuf io)
	{
		io.writeInt(dim.getId());
		io.writeInt(types.size());
		
		for(Map.Entry<ChunkDimPos, ChunkType> e : types.entrySet())
		{
			io.writeInt(e.getKey().chunkXPos);
			io.writeInt(e.getKey().chunkZPos);
			e.getValue().write(io);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onMessage(final MessageAreaUpdate m, Minecraft mc)
	{
		if(FTBUWorldDataSP.get().isLoaded())
		{
			FTBUWorldDataSP.get().setTypes(m.dim, m.types);
		}
	}
}