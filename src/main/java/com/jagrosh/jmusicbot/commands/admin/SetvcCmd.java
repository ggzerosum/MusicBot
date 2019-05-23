/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetvcCmd extends AdminCommand<VoiceChannel>
{
    public SetvcCmd()
    {
        this.name = "setvc";
        this.help = "sets the voice channel for playing music";
        this.arguments = "<channel|NONE>";
        this.nullArgsInEventMessage = " Please include a voice channel or NONE";
        this.noneArgsInEventMessage = " Music can now be played in any channel";
        this.emptyListMessage = " No Voice Channels found matching \"";
    }
    
    protected void setSettingsTonull(Settings settings) {
    	settings.setVoiceChannel(null);
    }
    
    protected void setList(CommandEvent event) {
    	this.list = FinderUtil.findVoiceChannels(event.getArgs(), event.getGuild());
    }
    
    protected void setSettingsToListEntry(Settings settings) {
    	settings.setVoiceChannel(list.get(0));
    }
    
    protected String executeSuccessMessage() {
    	return " Music can now only be played in **"+list.get(0).getName()+"**";
    }
    
    protected String multiChannelsHeader(String eventArgs) {
    	return " Multiple voice channels found matching \""+eventArgs+"\":";
    }
    
    protected String[] multiChannelsBody(CommandEvent event) {
    	String temp[] = {};
    	
    	for(int i=0; i<6 && i<list.size(); i++)
            temp[i] = "\n - "+list.get(i).getName()+" (ID:"+list.get(i).getId()+")";
    	
    	return temp;
    }
}
