package ru.winxboyz.reore.utils;

import org.bukkit.Material;

public class OreData {
    private long timeToRegenerate;
    private Material oreType;

    public static final OreData EMPTY = new OreData(null, 0L);

    private static final String
        SECONDS = "секунд",
        MINUTES = "минут",
        HOURS   = "час";

    private String timeMessage(String timeUnit, int amount) {
        switch (timeUnit) {
            case OreData.SECONDS:
            case OreData.MINUTES:
                if(amount % 10 == 1 && amount % 100 != 11)
                    return amount + " " + timeUnit + "у";
                else if(1 < amount % 10 && amount % 10 < 5 && (amount % 100) / 10 != 1)
                    return amount + " " + timeUnit + "ы";
                return amount + " " + timeUnit;
            case OreData.HOURS:
                if(amount % 10 == 1 && amount % 100 != 11)
                    return amount + " " + timeUnit;
                else if(1 < amount % 10 && amount % 10 < 5 && (amount % 100) / 10 != 1)
                    return amount + " " + timeUnit + "а";
                return amount + " " + timeUnit + "ов";
            default:
                throw new IllegalArgumentException();
        }
    }

    public Material getType() {
        return oreType;
    }
    public long getTimeToRegenerate() {
        return timeToRegenerate;
    }

    public String toString() {
        int time = (int) (timeToRegenerate - System.currentTimeMillis()) / 1000;
        return (time/3600>0 ? timeMessage(OreData.HOURS, time/3600) : "")
            +  (time/60  >0 ? " " + timeMessage(OreData.MINUTES, (time/60) % 60) : "")
            +  (time%60  >0 ? " " + timeMessage(OreData.SECONDS, time%60) : "");
    }

    public OreData(Material oreType, long timeToRegenerate) {
        this.timeToRegenerate = timeToRegenerate;
        this.oreType = oreType;
    }
}
