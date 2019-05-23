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
import net.dv8tion.jda.core.entities.TextChannel;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SettcCmd extends AdminCommand<TextChannel>
{
    public SettcCmd()
    {
        this.name = "settc";
        this.help = "sets the text channel for music commands";
        this.arguments = "<channel|NONE>";
        this.nullArgsInEventMessage = " Please include a text channel or NONE";
        this.noneArgsInEventMessage = " Music commands can now be used in any channel";
        this.emptyListMessage = " No Text Channels found matching \"";
    }

    protected void setSettingsTonull(Settings settings) {
    	settings.setTextChannel(null);
    }
    
    protected void setList(CommandEvent event) {
    	this.list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
    }
    
    protected void setSettingsToListEntry(Settings settings) {
    	settings.setTextChannel(list.get(0));
    }
    
    protected String executeSuccessMessage() {
    	return " Music commands can now only be used in <#"+list.get(0).getId()+">";
    }
    
    protected String multiChannelsHeader(String eventArgs) {
    	return " Multiple text channels found matching \""+eventArgs+"\":";
    }
    
    protected String[] multiChannelsBody(CommandEvent event) {
    	String temp[] = {};
    	
    	for(int i=0; i<6 && i<list.size(); i++)
    		temp[i] = "\n - "+list.get(i).getName()+" (<#"+list.get(i).getId()+">)";
    	
    	return temp;
    }
}
