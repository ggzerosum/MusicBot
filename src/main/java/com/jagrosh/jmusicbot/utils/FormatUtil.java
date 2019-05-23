/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.utils;

import java.util.List;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class FormatUtil {
    
    public static String formatTime(long duration)
    {
        if(duration == Long.MAX_VALUE)
            return "LIVE";
        
        long seconds = Math.round(duration/1000.0);
        long minutes = (seconds/60) % 60;
        long hours = seconds/(60*60);
        String hoursFormat = "";
        
        seconds %= 60;
        
        if(hours > 0)
           hoursFormat = hours + ":";
        
        return String.format("%s%02d:%02d", hoursFormat, minutes, seconds);
    }
        
    public static String progressBar(double percent)
    {
        String str = "▬▬▬▬▬▬▬▬▬▬▬▬";
        
        for(int i=0; i<12; i++) {
           if(i == (int)(percent*12)) {
              str = str.substring(0,i) + "\uD83D\uDD18"  + str.substring(i+1,12); // 🔘
              break;
           }
        }

        return str;
    }
    
    public static String volumeIcon(int volume)
    {
       switch(volume / 10) {
       case 0:
          return "\uD83D\uDD07"; // 🔇
       case 1:
       case 2:
          return "\uD83D\uDD08"; // 🔈
       case 3:
       case 4:
       case 5:
       case 6:
          return "\uD83D\uDD09"; // 🔉
       default:
          return "\uD83D\uDD0A"; // 🔊
       }
    }
    
    public static <T> String listOfChannels(List<T> list, String query, String[] entries) {
    	String channelsListMessage = query;
    	
    	for(int i=0; i<6 && i<list.size(); i++) {
    		channelsListMessage += entries[i];
    	}
    	
        if(list.size()>6)
            channelsListMessage+="\n**And "+(list.size()-6)+" more...**";
        
        return channelsListMessage;
    }
    
    public static String filter(String input)
    {
        return input.replace("@everyone", "@\u0435veryone").replace("@here", "@h\u0435re").trim(); // cyrillic letter e
    }
}