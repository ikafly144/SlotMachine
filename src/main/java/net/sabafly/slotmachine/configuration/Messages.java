package net.sabafly.slotmachine.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class Messages extends ConfigurationPart {
    // スロットマシンのメッセージ
    public String prefix = "<gold>[<aqua>SlotMachine</aqua>]</gold> ";
    public String noPermission = "<red>Permission denied.</red>";
    public String notEnoughMedal = "<red>You don't have enough medals.</red>";
    public String notEnoughMedalForLend = "<red>You don't have enough medals to lend.</red>";
    public String notEnoughMedalForPrize = "<red>You don't have enough medals to get the prize.</red>";
    public String notEnoughMedalForCustomPrize = "<red>You don't have enough medals to get the <prize>.</red>";
    // 景品交換のメッセージ
    public String prizeExchange = "<green>You got <prize>!</green>";
    public String prizePrice = "<green>Price: <price> medals</green>";
    public String itemTakeOut = "<green>Please take out the item.</green>";
    public String insertItem = "<green>Please insert the item.</green>";
    public String payOut = "<green>Pay out <price></green>";
    public String exchangeMenu = "<gold>Exchange Menu</gold>";
    public String prizeMenu = "<gold>Prize Menu</gold>";
    public String insertPaperHere = "<green>Insert the paper here.</green>";
    // プレイヤー
    public String medalCount = "<gold>Medals: <medal> medals</gold>";
}
