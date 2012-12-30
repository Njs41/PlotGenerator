package no.runsafe.worldgenerator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.Random;

public class PlotChunkGenerator extends ChunkGenerator
{
	public PlotChunkGenerator()
	{
		straight = new StraightRoad();
		intersect = new CrossRoads();
	}

	@Override
	public byte[][] generateBlockSections(World world, Random random, int cx, int cz, BiomeGrid biomes)
	{
		byte result[] = new byte[32768];

		Arrays.fill(result, Blocks.AIR);

		boolean hRoad = cx % plotSize == 0;
		boolean vRoad = cz % plotSize == 0;

		for (int x = 0; x < 16; ++x)
		{
			for (int z = 0; z < 16; ++z)
			{
				int offset = (x * 16 + z) * 128;
				result[offset] = Blocks.BEDROCK;
				Arrays.fill(result, offset + 1, offset + 60, Blocks.STONE);
				Arrays.fill(result, offset + 60, offset + 64, Blocks.DIRT);

				if (hRoad || vRoad)
				{
					for (int y = 0; y < 6; ++y)
					{
						byte what = 0;
						if (hRoad && vRoad)
							what = intersect.getByte(x, y, z);

						else
							what = straight.getByte(x, y, z, hRoad);

						result[offset + 62 + y] = what;
					}
				}
				else
					result[offset + 64] = Blocks.GRASS;
			}
		}

		byte[][] chunk = new byte[8][4096];
		for (int x = 0; x < 16; ++x)
			for (int y = 0; y < 128; ++y)
				for (int z = 0; z < 16; ++z)
					chunk[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = result[(x * 16 + z) * 128 + y];

		return chunk;
	}

	public Location getFixedSpawnLocation(World world, Random random)
	{
		if (!world.isChunkLoaded(0, 0))
			world.loadChunk(0, 0);
		if (world.getHighestBlockYAt(0, 0) <= 0 && world.getBlockAt(0, 0, 0).getType() == Material.AIR)
			return new Location(world, 0.0D, 64D, 0.0D);
		else
			return new Location(world, 0.0D, world.getHighestBlockYAt(0, 0), 0.0D);
	}

	private int plotSize = 3;
	private StraightRoad straight;
	private CrossRoads intersect;
}
