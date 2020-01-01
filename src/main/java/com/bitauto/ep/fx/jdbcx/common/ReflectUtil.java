package com.bitauto.ep.fx.jdbcx.common;

//import com.bitauto.ep.fx.utils.common.FileUtil;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Java Class与反射相关的一些工具类
 */
public class ReflectUtil
{

    /**
     * 获取类加载器
     */
    public static ClassLoader overridenClassLoader;

    public static ClassLoader getContextClassLoader() {
        return overridenClassLoader != null ?
                overridenClassLoader : Thread.currentThread().getContextClassLoader();
    }

    /**
     * 获取指定类的全部属性字段
     *
     * @param className    需要获取的类名
     * @param extendsField 是否获取接口或父类中的公共属性
     * @return 属性字段数组
     */
    public final static String[] getField(String className, boolean extendsField) {
        Class classz = loadClass(className);
        Field[] fields = classz.getFields();
        Set<String> set = new LinkedHashSet<>();
        if (fields != null) {
            for (Field f : fields) {
                set.add(f.getName());
            }
        }
        if (extendsField) {
            Field[] fieldz = classz.getDeclaredFields();
            if (fieldz != null) {
                for (Field f : fieldz) {
                    set.add(f.getName());
                }
            }
        }
        return set.toArray(new String[set.size()]);
    }

    /**
     * 获取类中的公共属性
     *
     * @param className    需要获取的类名
     * @param extendsField 是否获取接口或父类中的公共属性
     * @return 属性字段数组
     */
    public final static String[] getPublicField(String className, boolean extendsField) {
        Class classz = loadClass(className);
        Set<String> set = new HashSet<>();
        Field[] fields = classz.getDeclaredFields();
        if (fields != null) {
            for (Field f : fields) {
                String modifier = Modifier.toString(f.getModifiers());
                if (modifier.startsWith("public")) {
                    set.add(f.getName());
                }
            }
        }
        if (extendsField) {
            Field[] fieldz = classz.getFields();
            if (fieldz != null) {
                for (Field f : fieldz) {
                    set.add(f.getName());
                }
            }
        }
        return set.toArray(new String[set.size()]);
    }

    /**
     * 获取类中定义的protected类型的属性字段
     *
     * @param className 需要获取的类名
     * @return protected类型的属性字段数组
     */
    public final static String[] getProtectedField(String className) {
        Class classz = loadClass(className);
        Set<String> set = new HashSet<>();
        Field[] fields = classz.getDeclaredFields();
        if (fields != null) {
            for (Field f : fields) {
                String modifier = Modifier.toString(f.getModifiers());
                if (modifier.startsWith("protected")) {
                    set.add(f.getName());
                }
            }
        }
        return set.toArray(new String[set.size()]);
    }

    /**
     * 获取类中定义的private类型的属性字段
     *
     * @param className 需要获取的类名
     * @return private类型的属性字段数组
     */
    public final static String[] getPrivateField(String className) {
        Class classz = loadClass(className);
        Set<String> set = new HashSet<>();
        Field[] fields = classz.getDeclaredFields();
        if (fields != null) {
            for (Field f : fields) {
                String modifier = Modifier.toString(f.getModifiers());
                if (modifier.startsWith("private")) {
                    set.add(f.getName());
                }
            }
        }
        return set.toArray(new String[set.size()]);
    }

    /**
     * 获取对象的全部public类型方法
     *
     * @param className     需要获取的类名
     * @param extendsMethod 是否获取继承来的方法
     * @return 方法名数组
     */
    public final static String[] getPublicMethod(String className, boolean extendsMethod) {
        Class classz = loadClass(className);
        Method[] methods;
        if (extendsMethod) {
            methods = classz.getMethods();
        } else {
            methods = classz.getDeclaredMethods();
        }
        Set<String> set = new HashSet<>();
        if (methods != null) {
            for (Method f : methods) {
                String modifier = Modifier.toString(f.getModifiers());
                if (modifier.startsWith("public")) {
                    set.add(f.getName());
                }
            }
        }
        return set.toArray(new String[set.size()]);
    }


    /**
     * 获取对象的全部protected类型方法
     *
     * @param className     需要获取的类名
     * @param extendsMethod 是否获取继承来的方法
     * @return 方法名数组
     */
    public final static String[] getProtectedMethod(String className, boolean extendsMethod) {
        Class classz = loadClass(className);
        Method[] methods;
        if (extendsMethod) {
            methods = classz.getMethods();
        } else {
            methods = classz.getDeclaredMethods();
        }
        Set<String> set = new HashSet<>();
        if (methods != null) {
            for (Method f : methods) {
                String modifier = Modifier.toString(f.getModifiers());
                if (modifier.startsWith("protected")) {
                    set.add(f.getName());
                }
            }
        }
        return set.toArray(new String[set.size()]);
    }

    /**
     * 获取对象的全部private类型方法
     *
     * @param className 需要获取的类名
     * @return 方法名数组
     */
    public final static String[] getPrivateMethod(String className) {
        Class classz = loadClass(className);
        Method[] methods = classz.getDeclaredMethods();
        Set<String> set = new HashSet<>();
        if (methods != null) {
            for (Method f : methods) {
                String modifier = Modifier.toString(f.getModifiers());
                if (modifier.startsWith("private")) {
                    set.add(f.getName());
                }
            }
        }
        return set.toArray(new String[set.size()]);
    }

    /**
     * 获取对象的全部方法
     *
     * @param className     需要获取的类名
     * @param extendsMethod 是否获取继承来的方法
     * @return 方法名数组
     */
    public final static String[] getMethod(String className, boolean extendsMethod) {
        Class classz = loadClass(className);
        Method[] methods;
        if (extendsMethod) {
            methods = classz.getMethods();
        } else {
            methods = classz.getDeclaredMethods();
        }
        Set<String> set = new HashSet<>();
        if (methods != null) {
            for (Method f : methods) {
                set.add(f.getName());
            }
        }
        return set.toArray(new String[set.size()]);
    }


    /**
     * 调用对象的setter方法
     *
     * @param obj   对象
     * @param att   属性名
     * @param value 属性值
     * @param type  属性类型
     */
    public final static  void setPropertyValue(Object obj, String att, Object value, Class<?> type)
            throws InvocationTargetException, IllegalAccessException {
        try {
            String name = att.substring(0, 1).toUpperCase() + att.substring(1);
            Method met = obj.getClass().getMethod("set" + name,type);
            met.invoke(obj, value);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    public final static Object getPropertyValue(Object obj, String att)
            throws InvocationTargetException, IllegalAccessException {
        Object result = null;
        try {
            String name = att.substring(0, 1).toUpperCase() + att.substring(1);
            Method met = obj.getClass().getMethod("get" + name);
            result = met.invoke(obj);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取字段值
     * @param obj 对象
     * @param fieldName 字段名
     * @return
     */
    public static Object getFieldValue(Object obj,String fieldName){
        try{
            Object result = null;
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            result = field.get(obj);
            return result;
        }
        catch (Exception ex){
            throw new RuntimeException();
        }
    }

    /**
     * 设置字段值
     * @param propertyName 字段名
     * @param obj          实例对象
     * @param value        新的字段值
     * @return
     */
    public static void setFieldValue(Object obj,String propertyName,Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }



    /**
     * 从jar获取某包下所有类
     *
     * @param jarPath jar文件路径
     * @return 类的完整名称
     */
    public final static List<String> getClassNameByJar(String jarPath) {
        List<String> myClassName = new ArrayList<>();
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements()) {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class")) {
                    entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                    myClassName.add(entryName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myClassName;
    }

    /**
     * 调用类方法
     * @param className        类的全路径名称
     * @param methodName       调用方法名
     * @param parameterTypes   参数类型
     * @param values           参数值
     * @param object           实例对象
     * @return
     */
    public static Object methodInvoke(String className,String methodName,Class [] parameterTypes,Object [] values,Object object) {
        try {
            Method method = Class.forName(className).getDeclaredMethod(methodName,parameterTypes);
            method.setAccessible(true);
            return method.invoke(object,values);
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }

    /**
     *
     * @param obj
     * @param methodName
     * @param parameterValues
     * @return
     */
    public static Object methodInvoke(Object obj, String methodName, Object[] parameterValues){
        try {
            Class ownerClass = obj.getClass();
            Class[] argsClass = new Class[parameterValues.length];
            for (int i = 0, j = parameterValues.length; i < j; i++) {
                argsClass[i] = parameterValues[i].getClass();
            }
            Method method = ownerClass.getMethod(methodName,argsClass);
            return method.invoke(obj, parameterValues);
        }
        catch (Exception ex) {
            throw new RuntimeException();
        }

    }

    /**
     * 查找类型上的注解
     * @param clazz
     * @return
     */
    public static Annotation[] findClassAnnotation(Class<?> clazz) {
        return clazz.getAnnotations();
    }

    /**
     * 查找类型方法上的注解
     * @param clazz
     * @param methodName
     * @return
     */
    public static Annotation[] findMethodAnnotation(Class<?> clazz, String methodName) {

        Annotation[] annotations = null;
        try {
            Class<?>[] params = null;
            Method method = clazz.getDeclaredMethod(methodName, params);
            if (method != null) {
                annotations = method.getAnnotations();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return annotations;
    }

    /**
     * 查找字段上的注解
     * @param clazz
     * @param fieldName
     * @return
     */
    public static Annotation[] findFieldAnnotation(Class<?> clazz, String fieldName) {
        Annotation[] annotations = null;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field != null) {
                annotations = field.getAnnotations();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return annotations;
    }


    /**
     * 通过构造函数实例化对象
     * @param className       类的全路径名称
     * @param parameterTypes  参数类型
     * @param initargs        参数值
     * @return
     */
    public static Object constructorNewInstance(String className,Class [] parameterTypes,Object[] initargs) {
        try {
            Constructor<?> constructor = (Constructor<?>) Class
                    .forName(className).getDeclaredConstructor(parameterTypes);                     //暴力反射
            constructor.setAccessible(true);
            return constructor.newInstance(initargs);
        } catch (Exception ex) {
            throw new RuntimeException();
        }

    }


    /**
     * 加载指定的类
     *
     * @param className 需要加载的类
     * @return 加载后的类
     */
    public final static Class loadClass(String className) {
        Class theClass = null;
        try {
            theClass = Class.forName(className);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        return theClass;
    }


    /**
     * 获取一个类的父类
     *
     * @param className 需要获取的类
     * @return 父类的名称
     */
    public final static String getSuperClass(String className) {
        Class classz = loadClass(className);
        Class superclass = classz.getSuperclass();
        return superclass.getName();
    }

    /**
     * 获取一个雷的继承链
     *
     * @param className 需要获取的类
     * @return 继承类名的数组
     */
    public final static String[] getSuperClassChian(String className) {
        Class classz = loadClass(className);
        List<String> list = new ArrayList<>();
        Class superclass = classz.getSuperclass();
        String superName = superclass.getName();
        if (!"java.lang.Object".equals(superName)) {
            list.add(superName);
            list.addAll(Arrays.asList(getSuperClassChian(superName)));
        } else {
            list.add(superName);
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 获取一类实现的全部接口
     *
     * @param className         需要获取的类
     * @param extendsInterfaces 话说getInterfaces能全部获取到才对，然而测试的时候父类的接口并没有
     *                          因此就多除了这参数
     * @return 实现接口名称的数组
     */
    public final static String[] getInterfaces(String className, boolean extendsInterfaces) {
        Class classz = loadClass(className);
        List<String> list = new ArrayList<>();
        Class[] interfaces = classz.getInterfaces();
        if (interfaces != null) {
            for (Class inter : interfaces) {
                list.add(inter.getName());
            }
        }
        if (extendsInterfaces) {
            String[] superClass = getSuperClassChian(className);
            for (String c : superClass) {
                list.addAll(Arrays.asList(getInterfaces(c, false)));
            }
        }
        return list.toArray(new String[list.size()]);
    }

}
