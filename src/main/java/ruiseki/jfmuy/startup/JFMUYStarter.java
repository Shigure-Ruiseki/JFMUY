package ruiseki.jfmuy.startup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;

import cpw.mods.fml.common.ProgressManager;
import ruiseki.jfmuy.Internal;
import ruiseki.jfmuy.api.ICollapsibleGroupRegistry;
import ruiseki.jfmuy.api.IJFMUYRuntime;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.gui.IAdvancedGuiHandler;
import ruiseki.jfmuy.api.gui.IGhostIngredientHandler;
import ruiseki.jfmuy.api.gui.IGlobalGuiHandler;
import ruiseki.jfmuy.api.gui.IGuiScreenHandler;
import ruiseki.jfmuy.api.recipe.IIngredientType;
import ruiseki.jfmuy.autocrafting.favorites.FavoriteRecipes;
import ruiseki.jfmuy.bookmarks.BookmarkList;
import ruiseki.jfmuy.config.Config;
import ruiseki.jfmuy.gui.GuiEventHandler;
import ruiseki.jfmuy.gui.GuiHelper;
import ruiseki.jfmuy.gui.GuiScreenHelper;
import ruiseki.jfmuy.gui.ghost.GhostIngredientDragManager;
import ruiseki.jfmuy.gui.overlay.IngredientListOverlay;
import ruiseki.jfmuy.gui.overlay.bookmarks.BookmarkOverlay;
import ruiseki.jfmuy.gui.overlay.bookmarks.LeftAreaDispatcher;
import ruiseki.jfmuy.gui.recipes.RecipesGui;
import ruiseki.jfmuy.ingredients.CollapsedStack;
import ruiseki.jfmuy.ingredients.CollapsedStackRegistry;
import ruiseki.jfmuy.ingredients.IngredientBlacklistInternal;
import ruiseki.jfmuy.ingredients.IngredientFilter;
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
import ruiseki.jfmuy.util.Translator;

public class JFMUYStarter {

    private boolean started;

    public void start(List<IModPlugin> plugins) {
        load(plugins, false);
    }

    public void load(List<IModPlugin> plugins, boolean recipesOnly) {
        LoggedTimer totalTime = new LoggedTimer();
        totalTime.start("Starting JFMUY");

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
        JFMUYHelpers jeiHelpers = new JFMUYHelpers(guiHelper, ingredientRegistry, blacklist, stackHelper);
        Internal.setHelpers(jeiHelpers);

        ModRegistry modRegistry = new ModRegistry(jeiHelpers, ingredientRegistry);

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

        IngredientFilter ingredientFilter;
        if (recipesOnly && Internal.hasIngredientFilter()) {
            ingredientFilter = Internal.getIngredientFilter();
            ingredientFilter.replaceBlacklist(blacklist);
        } else {
            timer.start("Building ingredient filter and search trees");
            ingredientFilter = new IngredientFilter(
                blacklist,
                IngredientListElementFactory.createBaseList(ingredientRegistry, modIdHelper));
            Internal.setIngredientFilter(ingredientFilter);
            timer.stop();
        }

        registerDefaultCollapsibleGroups();
        registerModCollapsibleGroups(plugins);

        {
            CollapsedStackRegistry registry = Internal.getCollapsedStackRegistry();
            registry.loadCustomGroups();
            registry.syncDisabledGroups();
        }

        BookmarkList bookmarkList = new BookmarkList(ingredientRegistry);
        Internal.setBookmarkList(bookmarkList);

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
        GhostIngredientDragManager ghostIngredientDragManager = new GhostIngredientDragManager(
            guiScreenHelper,
            ingredientRegistry);
        IngredientListOverlay ingredientListOverlay = new IngredientListOverlay(
            ingredientFilter,
            ingredientRegistry,
            guiScreenHelper,
            ghostIngredientDragManager);

        BookmarkOverlay bookmarkOverlay = new BookmarkOverlay(bookmarkList, jeiHelpers.getGuiHelper(), guiScreenHelper);
        RecipesGui recipesGui = new RecipesGui(recipeRegistry, ingredientRegistry);
        JFMUYRuntime jeiRuntime = new JFMUYRuntime(
            recipeRegistry,
            ingredientListOverlay,
            bookmarkOverlay,
            recipesGui,
            ingredientFilter);
        Internal.setRuntime(jeiRuntime);
        timer.stop();

        stackHelper.disableUidCache();

        sendRuntime(plugins, jeiRuntime);

        LeftAreaDispatcher leftAreaDispatcher = new LeftAreaDispatcher(guiScreenHelper);
        leftAreaDispatcher.addContent(bookmarkOverlay);

        timer.start("Building favorites");
        FavoriteRecipes.load();
        timer.stop();

        timer.start("Building bookmarks");
        bookmarkList.loadBookmarks();
        timer.stop();

        GuiEventHandler guiEventHandler = new GuiEventHandler(
            guiScreenHelper,
            leftAreaDispatcher,
            ingredientListOverlay,
            recipeRegistry,
            ghostIngredientDragManager);
        Internal.setGuiEventHandler(guiEventHandler);
        InputHandler inputHandler = new InputHandler(
            jeiRuntime,
            ingredientRegistry,
            ingredientListOverlay,
            guiScreenHelper,
            leftAreaDispatcher,
            bookmarkList,
            ghostIngredientDragManager);
        Internal.setInputHandler(inputHandler);

        Config.checkForModNameFormatOverride();

        started = true;
        totalTime.stop();
    }

    public boolean hasStarted() {
        return started;
    }

    private static void registerItemSubtypes(List<IModPlugin> plugins, SubtypeRegistry subtypeRegistry) {
        if (Config.skipShowingProgressBar()) {
            Iterator<IModPlugin> iterator = plugins.iterator();
            while (iterator.hasNext()) {
                IModPlugin plugin = iterator.next();
                try {
                    plugin.registerSubtypes(subtypeRegistry);
                } catch (RuntimeException | LinkageError e) {
                    Log.get()
                        .error("Failed to register item subtypes for mod plugin: {}", plugin.getClass(), e);
                    iterator.remove();
                }
            }
        } else {
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
    }

    private static ModIngredientRegistration registerIngredients(List<IModPlugin> plugins) {
        ModIngredientRegistration modIngredientRegistry;
        if (Config.skipShowingProgressBar()) {
            modIngredientRegistry = new ModIngredientRegistration();

            Iterator<IModPlugin> iterator = plugins.iterator();
            while (iterator.hasNext()) {
                IModPlugin plugin = iterator.next();
                try {
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
        } else {
            ProgressManager.ProgressBar progressBar = ProgressManager.push("Registering ingredients", plugins.size());
            modIngredientRegistry = new ModIngredientRegistration();

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
        }

        return modIngredientRegistry;
    }

    private static void registerCategories(List<IModPlugin> plugins, ModRegistry modRegistry) {
        if (Config.skipShowingProgressBar()) {
            Iterator<IModPlugin> iterator = plugins.iterator();
            while (iterator.hasNext()) {
                IModPlugin plugin = iterator.next();
                try {
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
        } else {
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

    }

    private static void registerPlugins(List<IModPlugin> plugins, ModRegistry modRegistry) {
        if (Config.skipShowingProgressBar()) {
            Iterator<IModPlugin> iterator = plugins.iterator();
            while (iterator.hasNext()) {
                IModPlugin plugin = iterator.next();
                try {
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
        } else {
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

    }

    private static void sendRuntime(List<IModPlugin> plugins, IJFMUYRuntime jfmuyRuntime) {
        if (Config.skipShowingProgressBar()) {
            Iterator<IModPlugin> iterator = plugins.iterator();
            while (iterator.hasNext()) {
                IModPlugin plugin = iterator.next();
                try {
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
        } else {
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

    private static void registerDefaultCollapsibleGroups() {
        CollapsedStackRegistry registry = Internal.getCollapsedStackRegistry();
        // Enchanted books in JEI are stored as EnchantmentData (VanillaTypes.ENCHANT),
        // not as ItemStacks — IngredientRegistry strips them from the ItemStack list.
        // Match all EnchantmentData directly; every EnchantmentData IS an enchanted book.
        registry.groupForType(
            "enchanted_books",
            "Enchanted Books",
            ingredient -> ingredient instanceof net.minecraft.enchantment.EnchantmentData);
        registry.group("potions", "Potions", stack -> stack.getItem() == Items.potionitem);
        registry.group("spawn_eggs", "Spawn Eggs", stack -> stack.getItem() instanceof ItemMonsterPlacer);
    }

    private static void registerModCollapsibleGroups(List<IModPlugin> plugins) {
        CollapsedStackRegistry registry = Internal.getCollapsedStackRegistry();
        Map<String, ModGroupBuilderState> groupsById = new HashMap<>();

        ICollapsibleGroupRegistry apiRegistry = new ICollapsibleGroupRegistry() {

            @Override
            public CollapsibleGroupBuilder newGroup(String id, String langKey) {
                final ModGroupBuilderState state = groupsById.computeIfAbsent(id, key -> {
                    ModGroupBuilderState created = new ModGroupBuilderState();
                    // No uidMatcher yet — installed after all plugins finish registering.
                    // This avoids IngredientFilter using an empty uid fast-path that would
                    // short-circuit addAny/addAllOf predicates and cause them to never match.
                    created.registeredStack = registry
                        .addModGroup(id, Translator.translateToLocal(langKey), created::matches);
                    return created;
                });

                return new CollapsibleGroupBuilder() {

                    @Override
                    public CollapsibleGroupBuilder add(Object ingredient) {
                        if (ingredient != null) {
                            String uid = CollapsedStack.computeIngredientUid(ingredient);
                            if (uid != null) {
                                state.exactUids.add(uid);
                            }
                        }
                        return this;
                    }

                    @Override
                    public CollapsibleGroupBuilder add(Object... ingredients) {
                        if (ingredients != null) {
                            for (Object ingredient : ingredients) {
                                add(ingredient);
                            }
                        }
                        return this;
                    }

                    @Override
                    public CollapsibleGroupBuilder addAllOf(IIngredientType<?>... types) {
                        if (types != null) {
                            for (IIngredientType<?> type : types) {
                                if (type != null) {
                                    state.allOfTypes.add(type.getIngredientClass());
                                }
                            }
                        }
                        return this;
                    }

                    @Override
                    public <V> ICollapsibleGroupRegistry.CollapsibleGroupBuilder addAny(IIngredientType<V> type,
                        Predicate<V> filter) {
                        if (type != null && filter != null) {
                            Class<? extends V> ingredientClass = type.getIngredientClass();
                            state.typedPredicates.add(ingredient -> {
                                if (!ingredientClass.isInstance(ingredient)) {
                                    return false;
                                }
                                return filter.test(ingredientClass.cast(ingredient));
                            });
                        }
                        return this;
                    }
                };
            }
        };

        if (Config.skipShowingProgressBar()) {
            for (IModPlugin plugin : plugins) {
                try {
                    plugin.registerCollapsibleGroups(apiRegistry);
                } catch (RuntimeException | LinkageError e) {
                    Log.get()
                        .error("Failed to register collapsible groups for plugin: {}", plugin.getClass(), e);
                }
            }
        } else {
            ProgressManager.ProgressBar bar = ProgressManager.push("Registering collapsible groups", plugins.size());
            for (IModPlugin plugin : plugins) {
                try {
                    bar.step(
                        plugin.getClass()
                            .getName());
                    plugin.registerCollapsibleGroups(apiRegistry);
                } catch (RuntimeException | LinkageError e) {
                    Log.get()
                        .error("Failed to register collapsible groups for plugin: {}", plugin.getClass(), e);
                }
            }
            ProgressManager.pop(bar);
        }

        for (ModGroupBuilderState state : groupsById.values()) {
            if (!state.exactUids.isEmpty() && state.registeredStack != null) {
                state.registeredStack.setUidMatcher(state::matchesUid);
            }
        }
    }

    private static class ModGroupBuilderState {

        final Set<String> exactUids = new HashSet<>();
        final Set<Class<?>> allOfTypes = new HashSet<>();
        final List<Predicate<Object>> typedPredicates = new java.util.ArrayList<>();
        /** The CollapsedStack registered in the registry — uid matcher installed post-loop. */
        CollapsedStack registeredStack;

        boolean matchesUid(String uid) {
            return exactUids.contains(uid);
        }

        boolean matches(Object ingredient) {
            String uid = CollapsedStack.computeIngredientUid(ingredient);
            if (uid != null && exactUids.contains(uid)) {
                return true;
            }
            for (Class<?> ingredientClass : allOfTypes) {
                if (ingredientClass.isInstance(ingredient)) {
                    return true;
                }
            }
            for (Predicate<Object> predicate : typedPredicates) {
                if (predicate.test(ingredient)) {
                    return true;
                }
            }
            return false;
        }
    }
}
