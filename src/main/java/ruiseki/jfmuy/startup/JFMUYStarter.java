package ruiseki.jfmuy.startup;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.ProgressManager;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.IJFMUYRuntime;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;
import ruiseki.jfmuy.api.gui.IGhostIngredientHandler;
import ruiseki.jfmuy.api.gui.IGlobalGuiHandler;
import ruiseki.jfmuy.api.gui.IGuiScreenHandler;
import ruiseki.jfmuy.bookmarks.BookmarkList;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.GuiEventHandler;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.gui.GuiScreenHelper;
import ruiseki.jfmuy.gui.overlay.IngredientListOverlay;
import ruiseki.jfmuy.gui.overlay.bookmarks.BookmarkOverlay;
import ruiseki.jfmuy.gui.overlay.bookmarks.LeftAreaDispatcher;
import ruiseki.jfmuy.gui.recipes.RecipesGui;
import ruiseki.jfmuy.ingredients.IngredientBlacklistInternal;
import ruiseki.jfmuy.ingredients.IngredientFilter;
import ruiseki.jfmuy.ingredients.IngredientListElement;
import ruiseki.jfmuy.ingredients.IngredientListElementFactory;
import ruiseki.jfmuy.ingredients.IngredientRegistry;
import ruiseki.jfmuy.input.InputHandler;
import ruiseki.jfmuy.plugins.vanilla.VanillaPlugin;
import ruiseki.jfmuy.recipes.RecipeRegistry;
import ruiseki.jfmuy.runtime.JFMUYHelpers;
import ruiseki.jfmuy.runtime.JFMUYRuntime;
import ruiseki.jfmuy.runtime.SubtypeRegistry;
import ruiseki.jfmuy.util.ErrorUtil;
import ruiseki.jfmuy.util.Log;
import ruiseki.jfmuy.util.LoggedTimer;

public class JFMUYStarter {

    private boolean started;

    public void start(List<IModPlugin> plugins) {
        LoggedTimer totalTime = new LoggedTimer();
        totalTime.start("Starting JFMUY");

        IngredientListElement.canonicalizedStringArrays = new ObjectOpenHashSet<>();

        IModIdHelper modIdHelper = ForgeModIdHelper.getInstance();
        ErrorUtil.setModIdHelper(modIdHelper);

        SubtypeRegistry subtypeRegistry = new SubtypeRegistry();
        Internal.setSubtypeRegistry(subtypeRegistry);

        registerItemSubtypes(plugins, subtypeRegistry);

        StackHelper stackHelper = new StackHelper(subtypeRegistry);
        stackHelper.enableUidCache();
        Internal.setStackHelper(stackHelper);

        IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
        ModIngredientRegistration modIngredientRegistry = registerIngredients(plugins);
        IngredientRegistry ingredientRegistry = modIngredientRegistry.createIngredientRegistry(modIdHelper, blacklist);
        Internal.setIngredientRegistry(ingredientRegistry);

        GuiHelper guiHelper = new GuiHelper(ingredientRegistry);
        JFMUYHelpers jfmuyHelpers = new JFMUYHelpers(guiHelper, ingredientRegistry, blacklist, stackHelper);
        Internal.setHelpers(jfmuyHelpers);

        ModRegistry modRegistry = new ModRegistry(jfmuyHelpers, ingredientRegistry);

        LoggedTimer timer = new LoggedTimer();
        timer.start("Registering recipe categories");
        registerCategories(plugins, modRegistry);
        timer.stop();

        timer.start("Registering mod plugins");
        registerPlugins(plugins, modRegistry);
        timer.stop();

        timer.start("Building recipe registry");
        RecipeRegistry recipeRegistry = modRegistry.createRecipeRegistry(ingredientRegistry);
        timer.stop();

        timer.start("Building ingredient filter and search trees");
        IngredientFilter ingredientFilter = new IngredientFilter(
            blacklist,
            IngredientListElementFactory.createBaseList(ingredientRegistry, modIdHelper));
        Internal.setIngredientFilter(ingredientFilter);
        timer.stop();

        timer.start("Building bookmarks");
        BookmarkList bookmarkList = new BookmarkList(ingredientRegistry);
        bookmarkList.loadBookmarks();
        timer.stop();

        timer.start("Building runtime");
        List<IAdvancedGuiHandler<?>> advancedGuiHandlers = modRegistry.getAdvancedGuiHandlers();
        List<IGlobalGuiHandler> globalGuiHandlers = modRegistry.getGlobalGuiHandlers();
        Map<Class, IGuiScreenHandler> guiScreenHandlers = modRegistry.getGuiScreenHandlers();
        Map<Class, IGhostIngredientHandler> ghostIngredientHandlers = modRegistry.getGhostIngredientHandlers();
        GuiScreenHelper guiScreenHelper = new GuiScreenHelper(
            ingredientRegistry,
            globalGuiHandlers,
            advancedGuiHandlers,
            ghostIngredientHandlers,
            guiScreenHandlers);
        IngredientListOverlay ingredientListOverlay = new IngredientListOverlay(
            ingredientFilter,
            ingredientRegistry,
            guiScreenHelper);

        BookmarkOverlay bookmarkOverlay = new BookmarkOverlay(
            bookmarkList,
            jfmuyHelpers.getGuiHelper(),
            guiScreenHelper);
        RecipesGui recipesGui = new RecipesGui(recipeRegistry, ingredientRegistry);
        JFMUYRuntime jfmuyRuntime = new JFMUYRuntime(
            recipeRegistry,
            ingredientListOverlay,
            bookmarkOverlay,
            recipesGui,
            ingredientFilter);
        Internal.setRuntime(jfmuyRuntime);
        timer.stop();

        stackHelper.disableUidCache();

        sendRuntime(plugins, jfmuyRuntime);

        LeftAreaDispatcher leftAreaDispatcher = new LeftAreaDispatcher(guiScreenHelper);
        leftAreaDispatcher.addContent(bookmarkOverlay);

        GuiEventHandler guiEventHandler = new GuiEventHandler(
            guiScreenHelper,
            leftAreaDispatcher,
            ingredientListOverlay,
            recipeRegistry);
        Internal.setGuiEventHandler(guiEventHandler);
        InputHandler inputHandler = new InputHandler(
            jfmuyRuntime,
            ingredientRegistry,
            ingredientListOverlay,
            guiScreenHelper,
            leftAreaDispatcher,
            bookmarkList);
        Internal.setInputHandler(inputHandler);

        Config.checkForModNameFormatOverride();

        IngredientListElement.canonicalizedStringArrays = new ObjectOpenHashSet<>();

        started = true;
        totalTime.stop();
    }

    public boolean hasStarted() {
        return started;
    }

    private static void registerItemSubtypes(List<IModPlugin> plugins, SubtypeRegistry subtypeRegistry) {
        ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering item subtypes", plugins.size());
        Iterator<IModPlugin> iterator = plugins.iterator();
        while (iterator.hasNext()) {
            IModPlugin plugin = iterator.next();
            try {
                progressBar.step(
                    plugin.getClass()
                        .getName());
                plugin.registerSubtypes(subtypeRegistry);
            } catch (RuntimeException | LinkageError e) {
                Log.get()
                    .error("Failed to register item subtypes for mod plugin: {}", plugin.getClass(), e);
                iterator.remove();
            }
        }
        ProgressManager.pop(progressBar);
    }

    private static ModIngredientRegistration registerIngredients(List<IModPlugin> plugins) {
        ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering ingredients", plugins.size());
        ModIngredientRegistration modIngredientRegistry = new ModIngredientRegistration();

        Iterator<IModPlugin> iterator = plugins.iterator();
        while (iterator.hasNext()) {
            IModPlugin plugin = iterator.next();
            try {
                progressBar.step(
                    plugin.getClass()
                        .getName());
                plugin.registerIngredients(modIngredientRegistry);
            } catch (RuntimeException | LinkageError e) {
                if (plugin instanceof VanillaPlugin) {
                    throw e;
                } else {
                    Log.get()
                        .error("Failed to register Ingredients for mod plugin: {}", plugin.getClass(), e);
                    iterator.remove();
                }
            }
        }
        ProgressManager.pop(progressBar);

        return modIngredientRegistry;
    }

    private static void registerCategories(List<IModPlugin> plugins, ModRegistry modRegistry) {
        ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering categories", plugins.size());
        Iterator<IModPlugin> iterator = plugins.iterator();
        while (iterator.hasNext()) {
            IModPlugin plugin = iterator.next();
            try {
                progressBar.step(
                    plugin.getClass()
                        .getName());
                long start_time = System.currentTimeMillis();
                Log.get()
                    .debug(
                        "Registering categories: {} ...",
                        plugin.getClass()
                            .getName());
                plugin.registerCategories(modRegistry);
                long timeElapsedMs = System.currentTimeMillis() - start_time;
                Log.get()
                    .debug(
                        "Registered  categories: {} in {} ms",
                        plugin.getClass()
                            .getName(),
                        timeElapsedMs);
            } catch (AbstractMethodError ignored) {
                // legacy plugins do not implement registerCategories
            } catch (RuntimeException | LinkageError e) {
                Log.get()
                    .error("Failed to register mod categories: {}", plugin.getClass(), e);
                iterator.remove();
            }
        }
        ProgressManager.pop(progressBar);
    }

    private static void registerPlugins(List<IModPlugin> plugins, ModRegistry modRegistry) {
        ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering plugins", plugins.size());
        Iterator<IModPlugin> iterator = plugins.iterator();
        while (iterator.hasNext()) {
            IModPlugin plugin = iterator.next();
            try {
                progressBar.step(
                    plugin.getClass()
                        .getName());
                long start_time = System.currentTimeMillis();
                Log.get()
                    .debug(
                        "Registering plugin: {} ...",
                        plugin.getClass()
                            .getName());
                plugin.register(modRegistry);
                long timeElapsedMs = System.currentTimeMillis() - start_time;
                Log.get()
                    .debug(
                        "Registered  plugin: {} in {} ms",
                        plugin.getClass()
                            .getName(),
                        timeElapsedMs);
            } catch (RuntimeException | LinkageError e) {
                Log.get()
                    .error("Failed to register mod plugin: {}", plugin.getClass(), e);
                iterator.remove();
            }
        }
        ProgressManager.pop(progressBar);
    }

    private static void sendRuntime(List<IModPlugin> plugins, IJFMUYRuntime jfmuyRuntime) {
        ProgressManager.ProgressBar progressBar = ProgressManager.push("Sending Runtime", plugins.size());
        Iterator<IModPlugin> iterator = plugins.iterator();
        while (iterator.hasNext()) {
            IModPlugin plugin = iterator.next();
            try {
                progressBar.step(
                    plugin.getClass()
                        .getName());
                long start_time = System.currentTimeMillis();
                Log.get()
                    .debug(
                        "Sending runtime to plugin: {} ...",
                        plugin.getClass()
                            .getName());
                plugin.onRuntimeAvailable(jfmuyRuntime);
                long timeElapsedMs = System.currentTimeMillis() - start_time;
                if (timeElapsedMs > 100) {
                    Log.get()
                        .warn(
                            "Sending runtime to plugin: {} took {} ms",
                            plugin.getClass()
                                .getName(),
                            timeElapsedMs);
                }
            } catch (RuntimeException | LinkageError e) {
                Log.get()
                    .error("Sending runtime to plugin failed: {}", plugin.getClass(), e);
                iterator.remove();
            }
        }
        ProgressManager.pop(progressBar);
    }
}
