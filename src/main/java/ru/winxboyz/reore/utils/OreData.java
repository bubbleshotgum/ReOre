package ru.winxboyz.reore.utils;

import org.bukkit.Material;

public class OreData {
    private long timeToRegenerate;
    private Material oreType;

    public static final OreData EMPTY = new OreData(null, 0L);

    private static final String
        SECONDS_RU = "секунд",
        SECONDS_EN = "second",
        MINUTES_RU = "минут",
        MINUTES_EN = "minute",
        HOURS_RU   = "час",
        HOURS_EN   = "hour";

    private String timeMessage(String timeUnit, int amount) {
        switch (timeUnit) {
            case OreData.SECONDS_RU:
            case OreData.MINUTES_RU:
                if(amount % 10 == 1 && amount % 100 != 11)
                    return amount + " " + timeUnit + "у";
                else if(1 < amount % 10 && amount % 10 < 5 && (amount % 100) / 10 != 1)
                    return amount + " " + timeUnit + "ы";
                return amount + " " + timeUnit;
            case OreData.HOURS_RU:
                if(amount % 10 == 1 && amount % 100 != 11)
                    return amount + " " + timeUnit;
                else if(1 < amount % 10 && amount % 10 < 5 && (amount % 100) / 10 != 1)
                    return amount + " " + timeUnit + "а";
                return amount + " " + timeUnit + "ов";
            case OreData.SECONDS_EN:
            case OreData.MINUTES_EN:
            case OreData.HOURS_EN  :
                return amount + " " + timeUnit + (amount > 1 ? "s" : "");
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

    public String toString(String locale) {
        int time = (int) (timeToRegenerate - System.currentTimeMillis()) / 1000;
        return (time/3600>0 ? timeMessage(locale.equals("ru") ? OreData.HOURS_RU : OreData.HOURS_EN, time/3600) + " " : "")
            +  (time/60  >0 ? timeMessage(locale.equals("ru") ? OreData.MINUTES_RU : OreData.MINUTES_EN, (time/60) % 60) + " " : "")
            +  (time%60  >0 ? timeMessage(locale.equals("ru") ? OreData.SECONDS_RU : OreData.SECONDS_EN, time%60) : "");
    }

    public OreData(Material oreType, long timeToRegenerate) {
        this.timeToRegenerate = timeToRegenerate;
        this.oreType = oreType;
    }
}
