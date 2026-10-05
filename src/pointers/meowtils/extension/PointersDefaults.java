package meowtils.extension;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;

final class PointersDefaults {
    static void apply(PointersModule module){
        try(InputStream in=PointersModule.class.getResourceAsStream("/pointers-defaults.properties")){
            if(in==null)return;Properties p=new Properties();p.load(in);
            apply(module,p);
        }catch(Exception e){Meowtils.error("Pointers defaults could not be read: "+e.getClass().getSimpleName());}
    }
    static void apply(PointersModule module,Properties p)throws Exception{
        for(Field f:PointersModule.class.getFields()){
            if(!f.isAnnotationPresent(Config.class)||!p.containsKey(f.getName()))continue;
            String value=p.getProperty(f.getName());
            if(f.getType()==boolean.class)f.setBoolean(module,Boolean.parseBoolean(value));
            else if(f.getType()==double.class)f.setDouble(module,Double.parseDouble(value));
            else if(f.getType()==float.class)f.setFloat(module,Float.parseFloat(value));
            else if(f.getType()==int.class)f.setInt(module,Integer.parseInt(value));
            else if(f.getType()==String.class)f.set(module,value);
        }
    }
    static Properties snapshot(PointersModule module)throws Exception{
        Properties p=new Properties();
        for(Field f:PointersModule.class.getFields())if(f.isAnnotationPresent(Config.class))p.setProperty(f.getName(),String.valueOf(f.get(module)));
        return p;
    }
    static void exportTo(PointersModule module,Path output)throws Exception{
        Properties settings=snapshot(module);Path temporary=output.resolveSibling(output.getFileName()+".tmp");
        try(InputStream list=PointersModule.class.getResourceAsStream("/pointers-files.txt")){
            if(list==null)throw new IOException("Missing extension file index");
            BufferedReader reader=new BufferedReader(new InputStreamReader(list,"UTF-8"));
            try(ZipOutputStream zip=new ZipOutputStream(Files.newOutputStream(temporary))){
                String name;while((name=reader.readLine())!=null){
                    if(name.isEmpty())continue;zip.putNextEntry(new ZipEntry(name));
                    if(name.equals("pointers-defaults.properties"))settings.store(zip,"Pointers 1.29 - first-run defaults");
                    else try(InputStream in=PointersModule.class.getResourceAsStream("/"+name)){
                        if(in==null)throw new IOException("Missing extension resource: "+name);
                        byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)zip.write(buffer,0,n);
                    }
                    zip.closeEntry();
                }
            }
            try{Files.move(temporary,output,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(AtomicMoveNotSupportedException e){Files.move(temporary,output,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(temporary);}
    }
}
