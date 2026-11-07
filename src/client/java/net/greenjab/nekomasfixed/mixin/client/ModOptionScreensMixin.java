package net.greenjab.nekomasfixed.mixin.client;

import net.greenjab.nekomasfixed.render.tab.screens.ConfigScreens;
import net.greenjab.nekomasfixed.render.tab.screens.InGameEditScreen;
import net.greenjab.nekomasfixed.screen.config.TabLiterals;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the options button to the pause menu of single player worlds, and the credits page to the credits menu. */
@Mixin({PauseScreen.class, CreditsAndAttributionScreen.class})
public abstract class ModOptionScreensMixin extends Screen {
    protected ModOptionScreensMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void nekomasfixed$addButton(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (self instanceof CreditsAndAttributionScreen) {
            this.addRenderableWidget(Button.builder(TabLiterals.MOD_CREDITS, b -> this.minecraft.setScreen(ConfigScreens.credits(self)))
                    .bounds(10, 10, 100, 20).build());
        } else if (this.minecraft != null && this.minecraft.getSingleplayerServer() != null) {
            this.addRenderableWidget(Button.builder(TabLiterals.IN_GAME, b -> this.minecraft.setScreen(new InGameEditScreen(self)))
                    .bounds(10, 10, 100, 20).build());
        }
    }
}
