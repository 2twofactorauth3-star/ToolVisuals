package ru.he3hauka.toolvisuals.client.utility.packs;

import java.io.*;
import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;

public final class ResourcePacks {
    private ResourcePacks(){}
    public static void apply(Minecraft client,Collection<String> ids) {
        var repository=client.getResourcePackRepository();repository.setSelected(ids.stream().filter(repository::isAvailable).toList());client.options.updateResourcePacks(repository);
    }
    public static InputStream icon(Pack pack) throws IOException {
        var resources=pack.openMetadata();var supplier=resources.getRootResource("pack.png");
        if(supplier==null){resources.close();return null;}
        try {
            var input=supplier.get();return new FilterInputStream(input){@Override public void close() throws IOException{try{super.close();}finally{resources.close();}}};
        }catch(IOException|RuntimeException error){resources.close();throw error;}
    }
}
