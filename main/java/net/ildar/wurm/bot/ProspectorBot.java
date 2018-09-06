package net.ildar.wurm.bot;

import com.wurmonline.client.game.inventory.InventoryMetaItem;
import com.wurmonline.client.renderer.PickableUnit;
import com.wurmonline.client.renderer.gui.CreationWindow;
import com.wurmonline.shared.constants.PlayerAction;
import net.ildar.wurm.Mod;
import net.ildar.wurm.Utils;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.util.InputMismatchException;

public class ProspectorBot extends Bot {
    private float staminaThreshold;
    private int clicks;

    public ProspectorBot() {
        registerInputHandler(InputKey.s, this::handleStaminaThresholdChange);
        registerInputHandler(InputKey.c, this::handleClicksChange);
    }

    @Override
    public void work() throws Exception{
        InventoryMetaItem pickaxe = Utils.getInventoryItem("pickaxe");
        long pickaxeId;
        if (pickaxe == null) {
            Utils.consolePrint("You don't have a pickaxe");
            deactivate();
            return;
        } else {
            pickaxeId = pickaxe.getId();
            Utils.consolePrint(this.getClass().getSimpleName() + " will use " + pickaxe.getBaseName());
        }
        PickableUnit pickableUnit = ReflectionUtil.getPrivateField(Mod.hud.getSelectBar(),
                ReflectionUtil.getField(Mod.hud.getSelectBar().getClass(), "selectedUnit"));;
        if (pickableUnit == null) {
            Utils.consolePrint("Select cave wall!");
            deactivate();
            return;
        } else
            Utils.consolePrint(this.getClass().getSimpleName() + " will prospect " + pickableUnit.getHoverName());
        long caveWallId = pickableUnit.getId();
        CreationWindow creationWindow = Mod.hud.getCreationWindow();
        Object progressBar = ReflectionUtil.getPrivateField(creationWindow,
                ReflectionUtil.getField(creationWindow.getClass(), "progressBar"));
        setStaminaThreshold(0.9f);
        setClicks(3);
        while (isActive()) {
            float stamina = Mod.hud.getWorld().getPlayer().getStamina();
            float damage = Mod.hud.getWorld().getPlayer().getDamage();
            float progress = ReflectionUtil.getPrivateField(progressBar,
                    ReflectionUtil.getField(progressBar.getClass(), "progress"));
            if ((stamina+damage) > staminaThreshold && progress == 0f) {
                for(int i = 0; i < clicks; i++)
                    Mod.hud.getWorld().getServerConnection().sendAction(pickaxeId, new long[]{caveWallId}, PlayerAction.PROSPECT);
            }
            sleep(timeout);
        }
    }

    private void handleStaminaThresholdChange(String input[]) {
        if (input == null || input.length != 1)
            printInputKeyUsageString(InputKey.s);
        else {
            try {
                float threshold = Float.parseFloat(input[0]);
                setStaminaThreshold(threshold);
            } catch (Exception e) {
                Utils.consolePrint("Wrong threshold value!");
            }
        }
    }

    private void setStaminaThreshold(float s) {
        staminaThreshold = s;
        Utils.consolePrint("Current threshold for stamina is " + staminaThreshold);
    }

    private void handleClicksChange(String []input) {
        if (input == null || input.length != 1) {
            printInputKeyUsageString(InputKey.c);
            return;
        }
        try {
            setClicks(Integer.parseInt(input[0]));
        } catch (InputMismatchException e) {
            Utils.consolePrint("Bad value!");
        }
    }

    private void setClicks(int n) {
        if (n < 1) n = 1;
        if (n > 10) n = 10;
        clicks = n;
        Utils.consolePrint(getClass().getSimpleName() + " will do " + clicks + " clicks each time");
    }

    private enum InputKey {
        s("Set the stamina threshold. Player will not do any actions if his stamina is lower than specified threshold",
                "threshold(float value between 0 and 1)"),
        c("Change the amount of clicks bot will do each time", "n(integer value)");

        public String description;
        public String usage;
        InputKey(String description, String usage) {
            this.description = description;
            this.usage = usage;
        }
    }
}