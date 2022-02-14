package io.github.engineeringov.website.api.common.jpa.model.acore;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "characters", schema="acore_characters", catalog = "acore_characters")
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`guid`")
    private int guid;

    @Column(name = "account")
    private int account;

    // Can not be longer than 12 chars
    @Column(name = "`name`")
    private String name;

    @Column(name = "race")
    private short race;

    @Column(name = "`class`")
    private short characterClass;

    @Column(name = "gender")
    private short gender;

    @Column(name = "`level`")
    private short level;

    @Column(name = "xp")
    private long xp;

    @Column(name = "money")
    private int money;

    @Column(name = "skin")
    private short skin;

    @Column(name = "face")
    private short face;

    @Column(name = "hairStyle")
    private short hairStyle;

    @Column(name = "hairColor")
    private short hairColor;

    @Column(name = "facialStyle")
    private short facialStyle;

    @Column(name = "bankSlots")
    private short bankSlots;

    @Column(name = "restState")
    private short restState;

    @Column(name = "playerflags")
    private int playerFlags;

    @Column(name = "position_x")
    private float positionX;

    @Column(name = "position_y")
    private float positionY;

    @Column(name = "position_z")
    private float positionZ;

    @Column(name = "map")
    private short map;

    @Column(name = "instance_id")
    private int instanceId;

    @Column(name = "instance_mode_mask")
    private short instanceModeMask;

    @Column(name = "orientation")
    private float orientation;

    @Column(name = "taximask")
    private String taxiMask;

    @Column(name = "online", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean online;

    @Column(name = "cinematic", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean cinematic;

    @Column(name = "totaltime")
    private int totalTime;

    @Column(name = "leveltime")
    private int levelTime;

    @Column(name = "logout_time")
    private int logoutTime;

    @Column(name = "is_logout_resting", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean isLogoutResting;

    @Column(name = "rest_bonus")
    private float restBonus;

    @Column(name = "resettalents_cost")
    private int resetTalentsCost;

    @Column(name = "resettalents_time")
    private int resetTalentsTime;

    @Column(name = "trans_x")
    private float transX;

    @Column(name = "trans_y")
    private float transY;

    @Column(name = "trans_Z")
    private float transZ;

    @Column(name = "transguid")
    private int transGuid;

    @Column(name = "extra_flags")
    private short extraFlags;

    @Column(name = "stable_slots")
    private byte stableSlots;

    @Column(name = "at_login")
    private short atLogin;

    @Column(name = "`zone`")
    private short zone;

    @Column(name = "death_expire_time")
    private int deathExpireTime;

    @Column(name = "taxi_path")
    private String taxiPath;

    @Column(name = "arenaPoints")
    private int arenaPoints;

    @Column(name = "totalHonorPoints")
    private int totalHonorPoints;

    @Column(name = "todayHonorPoints")
    private int todayHonorPoints;

    @Column(name = "yesterdayHonorPoints")
    private int yesterdayHonorPoints;

    @Column(name = "totalKills")
    private int totalKills;

    @Column(name = "todayKills")
    private short todayKills;

    @Column(name = "yesterdayKills")
    private short yesterdayKills;

    @Column(name = "chosenTitle")
    private int chosenTitle;

    @Column(name = "knownCurrencies")
    private long knownCurrencies;

    @Column(name = "watchedFaction")
    private int watchedFactions;

    @Column(name = "drunk")
    private int drunk;

    @Column(name = "health")
    private int health;

    @Column(name = "power1")
    private int power1;

    @Column(name = "power2")
    private int power2;

    @Column(name = "power3")
    private int power3;

    @Column(name = "power4")
    private int power4;

    @Column(name = "power5")
    private int power5;

    @Column(name = "power6")
    private int power6;

    @Column(name = "power7")
    private int power7;

    @Column(name = "latency")
    private int latency;

    @Column(name = "talentGroupsCount")
    private byte talentGroupsCount;

    @Column(name = "activeTalentsGroup")
    private byte activeTalentsGroup;

    @Column(name = "exploredZones")
    private String exploredZones;

    @Column(name = "equipmentCache")
    private String equipmentCache;

    @Column(name = "ammoId")
    private int ammoId;

    @Column(name = "knownTitles")
    private int knownTitles;

    @Column(name = "actionBars")
    private short actionBars;

    @Column(name = "grantableLevels")
    private short grantableLevels;

    @Column(name = "`order`")
    private short order;

    @Column(name = "creation_date")
    private LocalTime creationDate;

    @Column(name = "deleteInfos_Account")
    private int deleteInfosAccount;

    @Column(name = "deleteInfos_Name")
    private String deleteInfosName;

    @Column(name = "deleteDate")
    private int deleteDate;

    public Character(int account, String name) {
        this.account = account;
        this.name = name;
    }
}
