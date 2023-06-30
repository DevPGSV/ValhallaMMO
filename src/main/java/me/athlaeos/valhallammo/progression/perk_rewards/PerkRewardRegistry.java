package me.athlaeos.valhallammo.progression.perk_rewards;

import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.playerstats.profiles.ResetType;
import me.athlaeos.valhallammo.progression.perk_rewards.implementations.*;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileManager;
import me.athlaeos.valhallammo.playerstats.profiles.implementations.PowerProfile;
import me.athlaeos.valhallammo.playerstats.profiles.properties.StatProperties;
import me.athlaeos.valhallammo.progression.skills.SkillRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PerkRewardRegistry {
    private static final Map<String, PerkReward> registry = new HashMap<>();

    static {
        for (Profile profile : ProfileManager.getRegisteredProfiles().values()){
            String skill = profile.getSkillType().getSimpleName().toLowerCase().replace("skill", "");
            if (profile.getSkillType() == null) continue;
            for (String s : profile.getAllStatNames()) {
                StatProperties properties = profile.getNumberStatProperties().get(s);
                if (properties != null && properties.generatePerkRewards()) {
                    if (profile.intStatNames().contains(s)) {
                        register(new ProfileIntAdd(skill + "_" + s + "_add", s, profile.getClass()));
                        register(new ProfileIntSet(skill + "_" + s + "_set", s, profile.getClass()));
                    } else if (profile.floatStatNames().contains(s)) {
                        register(new ProfileFloatAdd(skill + "_" + s + "_add", s, profile.getClass()));
                        register(new ProfileFloatSet(skill + "_" + s + "_set", s, profile.getClass()));
                    } else if (profile.doubleStatNames().contains(s)) {
                        register(new ProfileDoubleAdd(skill + "_" + s + "_add", s, profile.getClass()));
                        register(new ProfileDoubleSet(skill + "_" + s + "_set", s, profile.getClass()));
                    }
                }
                if (profile.shouldBooleanStatHavePerkReward(s)){
                    register(new ProfileBooleanSet(skill + "_" + s + "_set", s, profile.getClass()));
                }
            }
        }

        for (ResetType type : ResetType.values()){
            register(new ProgressReset("reset_" + type.toString().toLowerCase(), type));
        }

        register(new ProfileStringListAdd("fake_unlock_perks", "fakeUnlockedPerks", PowerProfile.class));
        register(new ProfileStringListClear("fake_unlock_perks_clear", "fakeUnlockedPerks", PowerProfile.class));
        register(new ProfileStringListAdd("permanently_lock_perks", "permanentlyLockedPerks", PowerProfile.class));
        register(new ProfileStringListClear("permanently_locked_perks_clear", "permanentlyLockedPerks", PowerProfile.class));
        register(new ProfileStringListAdd("unlock_recipes", "unlockedRecipes", PowerProfile.class));
        register(new ProfileStringListRemove("lock_recipes", "unlockedRecipes", PowerProfile.class));
        register(new ProfileStringListFill("unlock_all_recipes", "unlockedRecipes", PowerProfile.class, HashSet::new)); // TODO
        register(new ProfileStringListClear("lock_all_recipes", "unlockedRecipes", PowerProfile.class));
    }

    public static void register(PerkReward reward){
        registry.put(reward.getName(), reward);
    }

    public static PerkReward createReward(String name, Object argument){
        try {
            boolean persist = name.startsWith("p:");
            if (persist) name = name.replaceFirst("p:", "");
            if (registry.get(name) == null) throw new IllegalArgumentException("Perk with name " + name + " is used but does not exist");
            PerkReward modifier = registry.get(name).clone();
            modifier.parseArgument(argument);
            modifier.setPersistent(persist);
            return modifier;
        } catch (CloneNotSupportedException ex){
            ValhallaMMO.logSevere("Could not clone PerkReward, notify plugin author");
            return null;
        }
    }

    public static Map<String, PerkReward> getRegisteredRewards() {
        return registry;
    }
}