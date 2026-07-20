package ruiseki.jfmuy.startup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.discovery.ASMDataTable;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.JFMUYPlugin;
import ruiseki.jfmuy.util.Log;

public class AnnotatedInstanceUtil {

    private AnnotatedInstanceUtil() {

    }

    public static List<IModPlugin> getModPlugins(ASMDataTable asmDataTable) {
        return getInstances(asmDataTable, JFMUYPlugin.class, IModPlugin.class);
    }

    private static <T> List<T> getInstances(ASMDataTable asmDataTable, Class annotationClass, Class<T> instanceClass) {
        String annotationClassName = annotationClass.getCanonicalName();
        Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
        Collection<String> classNames = new ArrayList<>();
        for (ASMDataTable.ASMData asmData : asmDatas) {
            Map<String, Object> annotationInfo = asmData.getAnnotationInfo();
            String modId = "";
            if (annotationInfo != null && annotationInfo.containsKey("value")) {
                modId = (String) annotationInfo.get("value");
            }

            if (!modId.isEmpty() && !Loader.isModLoaded(modId)) {
                Log.get()
                    .info("Skipping plugin {} because required mod '{}' is not loaded.", asmData.getClassName(), modId);
                continue;
            }

            classNames.add(asmData.getClassName());
        }
        return getInstances(classNames, instanceClass);
    }

    public static <T> List<T> getInstances(Iterable<String> classNames, Class<T> instanceClass) {
        List<T> instances = new ArrayList<>();
        for (String className : classNames) {
            try {
                Class<?> asmClass = Class.forName(className);
                Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
                T instance = asmInstanceClass.newInstance();
                instances.add(instance);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | RuntimeException
                | LinkageError e) {
                Log.get()
                    .error("Failed to load: {}", className, e);
            }
        }
        return instances;
    }
}
