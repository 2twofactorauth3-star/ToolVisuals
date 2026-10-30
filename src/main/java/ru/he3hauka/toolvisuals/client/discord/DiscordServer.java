package ru.he3hauka.toolvisuals.client.discord;

import java.net.URI;
import java.util.Locale;

final class DiscordServer {
    private static final String DOMAIN="toolrise.space";
    private DiscordServer(){}

    static String visible(String address){
        if(address==null||address.length()>300)return "";
        try{
            var uri=URI.create("tcp://"+address.strip());
            String host=uri.getHost();int port=uri.getPort();
            if(host==null||uri.getRawUserInfo()!=null||uri.getRawQuery()!=null||uri.getRawFragment()!=null
                    ||!uri.getRawPath().isEmpty()||port==0||port>65535)return "";
            host=host.toLowerCase(Locale.ROOT);if(host.endsWith("."))host=host.substring(0,host.length()-1);
            if(!host.equals(DOMAIN)&&!host.endsWith("."+DOMAIN))return "";
            return host+(port<0?"":":"+port);
        }catch(IllegalArgumentException invalidAddress){return "";}
    }
}
