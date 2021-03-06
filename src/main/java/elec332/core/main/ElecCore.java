package elec332.core.main;

import elec332.core.abstraction.*;
import elec332.core.api.IElecCoreMod;
import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.api.data.IExternalSaveHandler;
import elec332.core.api.module.IModuleController;
import elec332.core.api.network.ModNetworkHandler;
import elec332.core.api.registry.ISingleRegister;
import elec332.core.api.util.IDependencyHandler;
import elec332.core.api.util.IRightClickCancel;
import elec332.core.client.model.loading.INoJsonItem;
import elec332.core.compat.ModNames;
import elec332.core.effects.AbilityHandler;
import elec332.core.grid.internal.GridEventHandler;
import elec332.core.grid.internal.GridEventInputHandler;
import elec332.core.handler.ModEventHandler;
import elec332.core.handler.TickHandler;
import elec332.core.network.IElecNetworkHandler;
import elec332.core.network.packets.PacketReRenderBlock;
import elec332.core.network.packets.PacketSyncWidget;
import elec332.core.network.packets.PacketTileDataToServer;
import elec332.core.network.packets.PacketWidgetDataToServer;
import elec332.core.proxies.CommonProxy;
import elec332.core.server.SaveHandler;
import elec332.core.server.ServerHelper;
import elec332.core.util.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Elec332.
 */
@Mod(modid = ElecCore.MODID, name = ElecCore.MODNAME, dependencies = "after:"+ ModNames.FORESTRY,
acceptedMinecraftVersions = "[1.11,)", version = ElecCore.ElecCoreVersion, useMetadata = true)
public class ElecCore implements IModuleController, IElecCoreMod, IDependencyHandler {

	public static final String ElecCoreVersion = "#ELECCORE_VER#";
	public static final String MODID = "eleccore";
	public static final String MODNAME = "ElecCore";
	public static final String FORGE_VERSION = "13.19.1.2195";

	@SidedProxy(clientSide = "elec332.core.proxies.ClientProxy", serverSide = "elec332.core.proxies.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Instance(MODID)
	public static ElecCore instance;
	@ModNetworkHandler
	public static IElecNetworkHandler networkHandler;
	public static TickHandler tickHandler;
	public static Logger logger;
	protected ElecCoreDiscoverer asmDataProcessor;
	private Configuration config;
	private LoadTimer loadTimer;
	private ModEventHandler modEventHandler;

	public static final boolean developmentEnvironment;
	public static boolean debug = false;
	public static boolean removeJSONErrors = true;

	public static boolean suppressSpongeIssues = false;

	@EventHandler
	public void construction(FMLConstructionEvent event){
		logger = LogManager.getLogger("ElecCore");
        for (ModContainer mc : FMLUtil.getLoader().getActiveModList()){
			if (mc instanceof FMLModContainer){
                ModEventHooks hook = new ModEventHooks((FMLModContainer) mc);
				FMLUtil.registerToModBus((FMLModContainer) mc, hook);
                if (mc.getMod() == this){
                    hook.onConstuct(event);
                }
			}
		}
		asmDataProcessor = new ElecCoreDiscoverer();
		asmDataProcessor.identify(event.getASMHarvestedData());
		ElecModHandler.identifyMods();
		asmDataProcessor.process(LoaderState.CONSTRUCTING);
		/*try {
			Class<? extends Object> clazz = ASMHelper.makeImplementInterfaces(Object.class, de.DEI.class, null);
			System.out.println(clazz);
			System.out.println(Lists.newArrayList(clazz.getInterfaces()));
			System.out.println(((de) clazz.getConstructor(Object.class).newInstance(new de.DEI(new Object()))).jo(2, 3));
		} catch (Exception e){
			logger.info("", e);
		}
		//AbstractionHandler.registerAbstractionObject(new testItem(), new ResourceLocation("nemez", "itemTestert"), ItemType.ITEM);
		AbstractionHandler.registerAbstractionObject(new IItemBlock() {

			@Override
			public Block getBlock() {
				return Blocks.BRICK_BLOCK;
			}

			@Override
			public IItemBlock getFallback() {
				return DefaultInstances.createDefault(this);
			}

		}, new ResourceLocation("bee:p"), ItemType.ITEMBLOCK);*/
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ElecModHandler.initAnnotations(event.getAsmData());
		loadTimer = new LoadTimer(logger, MODNAME);
		loadTimer.startPhase(event);
		this.config = new Configuration(FileHelper.getConfigFileElec(event));
		tickHandler = new TickHandler();
		networkHandler.registerClientPacket(PacketSyncWidget.class);
		networkHandler.registerServerPacket(PacketTileDataToServer.class);
		networkHandler.registerServerPacket(PacketWidgetDataToServer.class);
		networkHandler.registerClientPacket(PacketReRenderBlock.class);

		MinecraftForge.EVENT_BUS.register(tickHandler);
		debug = config.getBoolean("debug", Configuration.CATEGORY_GENERAL, false, "Set to true to print debug info to the log.");
		removeJSONErrors = config.getBoolean("removeJsonExceptions", Configuration.CATEGORY_CLIENT, true, "Set to true to remove all the Json model errors from the log.") && !developmentEnvironment;
		suppressSpongeIssues = config.getBoolean("supressSpongeIssues", Configuration.CATEGORY_GENERAL, false, "Set to true to prevent multiblock crashes when Sponge is installed. WARNING: Unsupported, this may cause unexpected behaviour, use with caution!");
		ServerHelper.instance.load();

		MinecraftForge.EVENT_BUS.register(new GridEventHandler());

		proxy.preInitRendering();
		asmDataProcessor.process(LoaderState.PREINITIALIZATION);

		modEventHandler.postEvent(event);

		loadTimer.endPhase(event);
		MCModInfo.createMCModInfoElec(event, "Provides core functionality for Elec's Mods",
				"-", "assets/elec332/logo.png", new String[]{"Elec332"});
	}


	@EventHandler
	@SuppressWarnings("unchecked")
    public void init(FMLInitializationEvent event) {
		loadTimer.startPhase(event);
		config.load();
		if (config.hasChanged()){
			config.save();
		}
		ElecCoreRegistrar.dummyLoad();
		SaveHandler.INSTANCE.dummyLoad();
		AbilityHandler.instance.init();
		ElecModHandler.init();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
		asmDataProcessor.process(LoaderState.INITIALIZATION);
		OredictHelper.initLists();
		modEventHandler.postEvent(event);
		loadTimer.endPhase(event);
    }

	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		loadTimer.startPhase(event);
		asmDataProcessor.process(LoaderState.POSTINITIALIZATION);
		OredictHelper.initLists();
		proxy.postInitRendering();
		modEventHandler.postEvent(event);
		MinecraftForge.EVENT_BUS.register(new Object(){

			@SubscribeEvent(priority = EventPriority.LOWEST)
			public void onItemRightClick(PlayerInteractEvent.RightClickBlock event){
				ItemStack stack;
				if (event.getHand() == EnumHand.OFF_HAND){
					stack = event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND);
					if (stack.getItem() instanceof IRightClickCancel && ((IRightClickCancel) stack.getItem()).cancelInteraction(stack)){
						event.setCanceled(true);
						return;
					}
				}
				stack = event.getItemStack();
				if (stack.getItem() instanceof IRightClickCancel && ((IRightClickCancel) stack.getItem()).cancelInteraction(stack)) {
					event.setCanceled(true);
					stack.getItem().onItemUse(event.getEntityPlayer(), event.getWorld(), event.getPos(), event.getHand(), event.getFace(), (float) event.getHitVec().xCoord, (float) event.getHitVec().yCoord, (float) event.getHitVec().zCoord);
				}
			}

		});
		loadTimer.endPhase(event);
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent event){
		loadTimer.startPhase(event);
		asmDataProcessor.process(LoaderState.AVAILABLE);
		OredictHelper.initLists();
		modEventHandler.postEvent(event);
		loadTimer.endPhase(event);
	}

	@EventHandler
	public void onServerAboutToStart(FMLServerAboutToStartEvent event){
		GridEventInputHandler.INSTANCE.reloadHandlers();
		modEventHandler.postEvent(event);
		event.getServer().setOnlineMode(false);
	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event){
		modEventHandler.postEvent(event);
	}

	@EventHandler
	public void onServerStarted(FMLServerStartedEvent event){
		modEventHandler.postEvent(event);
	}

	@EventHandler
	public void onServerStopping(FMLServerStoppingEvent event){
		modEventHandler.postEvent(event);
	}

	@EventHandler
	public void onServerStopped(FMLServerStoppedEvent event){
		modEventHandler.postEvent(event);
	}

	@Override
	public void registerSaveHandlers(ISingleRegister<IExternalSaveHandler> saveHandlerRegistry) {
		saveHandlerRegistry.register(ServerHelper.instance);
	}

	public static void systemPrintDebug(Object s){
		if (debug) {
			System.out.println(s);
		}
	}

	@Override
	public boolean isModuleEnabled(String moduleName) {
		return true;
	}

	@Override
	public String getRequiredForgeVersion(String mcVersion) {
		return FORGE_VERSION;
	}

	public void setModEventHandler(ModEventHandler handler){
		if (this.modEventHandler != null){
			throw new IllegalStateException();
		}
		this.modEventHandler = handler;
	}

	private static class testItem implements IItem, INoJsonItem {

		@Override
		public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
			System.out.println("rightClick");
			return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
		}

		@Override
		public void registerTextures(IIconRegistrar iconRegistrar) {
			System.out.println("regTextures");
		}

		@Override
		public IBakedModel getItemModel(ItemStack stack, World world, EntityLivingBase entity) {
			return null;
		}

		@Override
		public void registerModels(IElecQuadBakery quadBakery, IElecModelBakery modelBakery, IElecTemplateBakery templateBakery) {
			System.out.println("regModels");
		}

	}

	static {
		developmentEnvironment = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	}

}
