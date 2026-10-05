package meowtils.extension;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;

final class TeamDetection {
    static boolean isBedwarsGame(String title,List<String> lines){
        if(title==null||!strip(title).toUpperCase(java.util.Locale.ROOT).contains("BED WARS")||lines==null)return false;
        int teams=0;
        for(String line:lines)if(strip(line).trim().matches("(?i)^[RGBYAWPS]\\s+(Red|Blue|Green|Yellow|Aqua|White|Pink|Gray|Grey):.*"))teams++;
        return teams>=2;
    }
    static String strip(String text){return text==null?"":text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]","");}
    static String teamColor(Minecraft mc,EntityPlayer player){
        if(mc.field_71441_e==null||player==null)return "";
        return color(mc,player,mc.field_71441_e.func_96441_U().func_96509_i(player.func_70005_c_()));
    }
    static boolean isTeammate(Minecraft mc,EntityPlayer self,EntityPlayer target){
        ScorePlayerTeam mine=mc.field_71441_e.func_96441_U().func_96509_i(self.func_70005_c_());
        ScorePlayerTeam theirs=mc.field_71441_e.func_96441_U().func_96509_i(target.func_70005_c_());
        if(mine!=null&&theirs!=null&&mine.func_96661_b().equals(theirs.func_96661_b()))return true;
        String a=color(mc,self,mine),b=color(mc,target,theirs);
        return !a.isEmpty()&&a.equals(b);
    }
    private static String color(Minecraft mc,EntityPlayer player,ScorePlayerTeam team){
        String name=player.func_70005_c_();
        // Tab display names may be null. Scoreboard formatting and entity names remain usable.
        if(mc.func_147114_u()!=null){
            NetworkPlayerInfo info=mc.func_147114_u().func_175102_a(player.func_110124_au());
            if(info!=null&&info.func_178854_k()!=null){String c=nameColor(info.func_178854_k().func_150254_d(),name);if(!c.isEmpty())return c;}
        }
        if(team!=null){
            String c=nameColor(team.func_96668_e()+name+team.func_96663_f(),name);
            if(!c.isEmpty())return c;
        }
        return player.func_145748_c_()==null?"":nameColor(player.func_145748_c_().func_150254_d(),name);
    }
    static String nameColor(String formatted,String playerName){
        if(formatted==null||playerName==null||playerName.isEmpty())return "";
        String plain=strip(formatted);
        int at=plain.lastIndexOf(playerName);
        if(at<0)return "";
        char color=0;int visible=0;
        for(int i=0;i<formatted.length()&&visible<=at;i++){
            char c=formatted.charAt(i);
            if(c=='\u00a7'&&i+1<formatted.length()){
                char code=Character.toLowerCase(formatted.charAt(++i));
                if("0123456789abcdef".indexOf(code)>=0)color=code;
                else if(code=='r')color=0;
            }else{if(visible==at)break;visible++;}
        }
        // Gray uses either gray shade across clients. Rank colors are not inferred from the first prefix.
        if(color=='8')color='7';
        return "c9aebfd7".indexOf(color)>=0?String.valueOf(color):"";
    }
}
