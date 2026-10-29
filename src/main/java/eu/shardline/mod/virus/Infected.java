package eu.shardline.mod.virus;

/**
 * Markiert Bloecke, die vom Virus befallen sind (siehe {@link InfectionMap#isInfected}).
 */
public interface Infected {
    /** Stamm des Befalls; Standard: Shardvirus. */
    default VirusStrain strain() {
        return VirusStrain.SHARD;
    }
}
