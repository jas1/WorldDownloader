package wdl.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wdl.WDL;
import wdl.WDLPluginChannels;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;

public class GuiWDL extends GuiScreen {
	/**
	 * Tooltip to display on the given frame.
	 */
	private String displayedTooltip = null;
	
	private class GuiWDLButtonList extends GuiListExtended {
		public GuiWDLButtonList() {
			super(GuiWDL.this.mc, GuiWDL.this.width, GuiWDL.this.height, 39,
					GuiWDL.this.height - 32, 20);
		}

		private class ButtonEntry implements IGuiListEntry {
			private final GuiButton button;
			private final GuiScreen toOpen;
			
			private final String tooltip;
			
			public ButtonEntry(String text, GuiScreen toOpen, String tooltip) {
				this.button = new GuiButton(0, 0, 0, text);
				this.toOpen = toOpen;
				
				this.tooltip = tooltip;
			}
			
			@Override
			public void setSelected(int p_178011_1_, int p_178011_2_,
					int p_178011_3_) {
				
			}

			@Override
			public void drawEntry(int slotIndex, int x, int y, int listWidth,
					int slotHeight, int mouseX, int mouseY, boolean isSelected) {
				button.xPosition = GuiWDL.this.width / 2 - 100;
				button.yPosition = y;
				
				button.drawButton(mc, mouseX, mouseY);
				
				if (button.isMouseOver()) {
					displayedTooltip = tooltip;
				}
			}

			@Override
			public boolean mousePressed(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				if (button.mousePressed(mc, x, y)) {
					mc.displayGuiScreen(toOpen);
					
					button.playPressSound(mc.getSoundHandler());
					
					return true;
				}
				
				return false;
			}

			@Override
			public void mouseReleased(int slotIndex, int x, int y,
					int mouseEvent, int relativeX, int relativeY) {
				
			}
		}
		
		private List<IGuiListEntry> entries = new ArrayList<IGuiListEntry>() {{
			// TODO: This might be a performance bottleneck, as a bunch of
			// GUI instances are created.  Although they aren't displayed.
			
			add(new ButtonEntry("World Overrides...", 
					new GuiWDLWorld(GuiWDL.this),
					"Control specific metadata about the saved world, " +
					"such as the spawn point and game mode."));
			add(new ButtonEntry("World Generator Overrides...", 
					new GuiWDLGenerator(GuiWDL.this),
					"Control specific info about how the world is generated, " +
					"such as the seed."));
			add(new ButtonEntry("Player Overrides...", 
					new GuiWDLPlayer(GuiWDL.this),
					"Control specific options about the saved player, " +
					"such as their health and position."));
			add(new ButtonEntry("Entity Options...", 
					new GuiWDLEntities(GuiWDL.this),
					"Control what types of entities to save, and the " +
					"track distances of those entities."));
			add(new ButtonEntry("Backup Options...", 
					new GuiWDLBackup(GuiWDL.this),
					"Control how the world is backed up after saving"));
			add(new ButtonEntry("Message Options...", 
					new GuiWDLMessages(GuiWDL.this),
					"Control what messages appear in the chat."));
			add(new ButtonEntry("Permissions info...", 
					new GuiWDLPermissions(GuiWDL.this),
					"Information about permissions setup on this server, " +
					"and the form for requesting new permissions."));
			add(new ButtonEntry("About World Downloader...",
					new GuiWDLAbout(GuiWDL.this), 
					"Information about your current installation of WDL, " +
					"and any extensions."));
		}};
		
		@Override
		public IGuiListEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}
	}
	
	private String title = "";

	private GuiScreen parent;

	private GuiTextField worldName;
	private GuiButton autoStartBtn;
	private GuiWDLButtonList list;

	public GuiWDL(GuiScreen parent) {
		this.parent = parent;
		System.out.println(net.minecraft.client.resources.I18n.format("test.test"));
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui() {
		if (WDL.isMultiworld && WDL.worldName.isEmpty()) {
			this.mc.displayGuiScreen(new GuiWDLMultiworldSelect(this.parent));
			return;
		}

		if (!WDL.propsFound) {
			this.mc.displayGuiScreen(new GuiWDLMultiworld(this.parent));
			return;
		}

		this.buttonList.clear();
		this.title = "Options for " + WDL.baseFolderName.replace('@', ':');

		if (WDL.baseProps.getProperty("ServerName").isEmpty()) {
			WDL.baseProps.setProperty("ServerName", WDL.getServerName());
		}

		this.worldName = new GuiTextField(42, this.fontRendererObj,
				this.width / 2 - 155, 19, 150, 18);
		this.updateServerName(false);
		this.autoStartBtn = new GuiButton(1, this.width / 2 + 5, 18, 150, 20,
				"Start Download: ERROR");
		this.buttonList.add(this.autoStartBtn);
		this.updateAutoStart(false);

		this.buttonList.add(new GuiButton(100, this.width / 2 - 100,
				this.height - 29, "Done"));
		
		this.list = new GuiWDLButtonList();
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (!guibutton.enabled) {
			return;
		}

		this.updateServerName(true);

		if (guibutton.id == 1) { // Auto start
			this.updateAutoStart(true);
		} else if (guibutton.id == 100) { // Done
			this.mc.displayGuiScreen(this.parent);
		}
	}
	
	@Override
	public void onGuiClosed() {
		WDL.saveProps();
	}

	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	throws IOException {
		list.func_148179_a(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.worldName.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.list.func_178039_p();
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (list.func_148181_b(mouseX, mouseY, state)) {
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}

	/**
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		this.worldName.textboxKeyTyped(typedChar, keyCode);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	@Override
	public void updateScreen() {
		this.worldName.updateCursorCounter(); // updateCursorCounter
		super.updateScreen();
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		
		displayedTooltip = null;
		
		this.list.drawScreen(mouseX, mouseY, partialTicks);
		
		this.drawCenteredString(this.fontRendererObj, this.title,
				this.width / 2, 8, 0xFFFFFF);
		this.drawString(this.fontRendererObj, "Name:", this.worldName.xPosition
				- this.fontRendererObj.getStringWidth("Name: "), 26, 0xFFFFFF);
		this.worldName.drawTextBox();
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		Utils.drawGuiInfoBox(displayedTooltip, width, height);
	}

	public void updateAutoStart(boolean btnClicked) {
		String autoStart = WDL.baseProps.getProperty("AutoStart");

		if (autoStart.equals("true")) {
			if (btnClicked) {
				WDL.baseProps.setProperty("AutoStart", "false");
				this.updateAutoStart(false);
			} else {
				this.autoStartBtn.displayString = "Start Download: Automatically";
			}
		} else if (btnClicked) {
			WDL.baseProps.setProperty("AutoStart", "true");
			this.updateAutoStart(false);
		} else {
			this.autoStartBtn.displayString = "Start Download: Only in menu";
		}
	}

	private void updateServerName(boolean var1) {
		if (var1) {
			WDL.baseProps.setProperty("ServerName", this.worldName.getText());
		} else {
			this.worldName.setText(WDL.baseProps.getProperty("ServerName"));
		}
	}
}
