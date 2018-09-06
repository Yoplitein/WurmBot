package net.ildar.wurm.bot;

import com.wurmonline.client.game.inventory.InventoryMetaItem;
import net.ildar.wurm.Mod;
import net.ildar.wurm.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ForageStuffMoverBot extends Bot {
    private List<Long> targets = new ArrayList<>();
    private boolean moveRareItems;
    private boolean notMoveRocks;

    public ForageStuffMoverBot() {
        registerInputHandler(InputKey.at, input -> addTarget());
        registerInputHandler(InputKey.r, input -> toggleMovingRareItems());
        registerInputHandler(InputKey.mr, input -> toggleMovingRocks());
    }

    @Override
    public void work() throws Exception{
        while (isActive()) {
            List<InventoryMetaItem> foragables = Utils.getSelectedItems(Mod.hud.getInventoryWindow().getInventoryListComponent(), true, true);
            List<InventoryMetaItem> moveList = foragables.stream()
                    .filter(item -> ForagerBot.isForagable(item) && !(notMoveRocks && item.getBaseName().contains("rock")))
                    .filter(item -> moveRareItems || item.getRarity() == 0)
                    .limit(100)
                    .collect(Collectors.toList());
            if (moveList.size() == 0) {
                Utils.consolePrint("Nothing to move");
            } else if (targets.size() == 0) {
                Utils.consolePrint("No target containers to move to");
            } else {
                long[] moveIds = Utils.getItemIds(moveList);
                for (long target : targets)
                    Mod.hud.getWorld().getServerConnection().sendMoveSomeItems(target, moveIds);
            }
            sleep(timeout);
        }
    }

    private void addTarget() {
        int x = Mod.hud.getWorld().getClient().getXMouse();
        int y = Mod.hud.getWorld().getClient().getYMouse();
        long[] targets = Mod.hud.getCommandTargetsFrom(x, y);
        if (targets != null && targets.length > 0) {
            long target = targets[0];
            this.targets.add(target);
            Utils.consolePrint("New target is " + target);
        } else
            Utils.consolePrint("Can't find the target");
    }

    private void toggleMovingRareItems() {
        moveRareItems = !moveRareItems;
        Utils.consolePrint("Rare items will be " + (moveRareItems?"":"NOT") + " moved");
    }

    private void toggleMovingRocks() {
        notMoveRocks = !notMoveRocks;
        Utils.consolePrint("Rocks will be " + (notMoveRocks?"NOT":"") + " moved");
    }

    enum InputKey {
        at("Add new target item. Foragable and botanizable items will be moved to that destination", ""),
        r("Toggle moving of rare items", ""),
        mr("Toggle moving of rocks", "");

        public String description;

        public String usage;
        InputKey(String description, String usage) {
            this.description = description;
            this.usage = usage;
        }
    }
}